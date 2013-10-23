package org.scaloid.layout.converter

import org.scaloid.layout.converter.Property.Constants


object XmlAttributes {
  import scala.xml._
  import XmlAttribute._

  private val apiVersion = 8 // TODO make configurable

  private def xmlUri = s"android-$apiVersion/data/res/values/attrs.xml" // TODO from more versions

  var cnt = 0
  val byName: Map[String, XmlAttribute] = {
    def nodeToAttr(className: String, node: Node): XmlAttribute = {
      val attrName = (node \ "@name").text

      if (node.child.exists(Set("enum", "flag") contains _.label)) {
        println(s"\n[$cnt] $className : $attrName")
        cnt += 1
      }

      // TODO deduplicate
      val enums = node.child.filter(_.label == "enum")
        .map { tag => Enum.find(className, attrName, (tag \ "@name").text, (tag \ "@value").text) }

      val flags = node.child.filter(_.label == "flag")
        .map { tag => Flag.find(className, attrName, (tag \ "@name").text, (tag \ "@value").text) }

      val format = node \ "@format" match {
        case NodeSeq.Empty => Format.ResourceFormat
        case n => Format(n.text)
      }

      new XmlAttribute(attrName, format, enums.toList, flags.toList)
    }

    val attrXml = XML.load(getClass.getClassLoader.getResource(xmlUri))
    val topAttrs = attrXml \ "attr" map ("View" -> _)
    val subAttrs = attrXml \ "declare-styleable" flatMap (ds => ds \ "attr" map ((ds \ "@name").text -> _))

    (topAttrs.iterator ++ subAttrs.iterator)
      .filter { case (_, tag) => (tag \ "@format").nonEmpty || tag.child.nonEmpty }
      .map((nodeToAttr _).tupled)
      .map(a => a.name -> a)
      .toMap
  }
}


class XmlAttribute(val name: String, format: XmlAttribute.Format, enums: List[XmlAttribute.Enum], flags: List[XmlAttribute.Flag]) {
  import StringUtils._

  private val consts: Map[String, AndroidConstant] =
    (enums.map { e => e.name -> e.target } ++ flags.map { e => e.name -> e.target }).toMap

  val renderName: String = if (name.startsWith("layout_")) name.drop(7) else name

  def parse(str: String) =
    if (flags.nonEmpty)
      Constants(str.split('|').toList.map(_.trim.toJavaConstFormat).map(consts.get _).flatten)
    else
      consts.get(str.toJavaConstFormat) match {
        case Some(const) => Constants(const)
        case None => format(str)
      }
}

object XmlAttribute {
  import scala.util.Try
  import StringUtils._
  import Property._
  import Format._

  def custom(name: String, format: Format = ResourceFormat, enums: List[Enum] = Nil, flags: List[Flag] = Nil) =
    new XmlAttribute(name, format, enums, flags)

  var count = 0
  private def lookup(tpe: String, className: String, attrName: String, constName: String, value: String): AndroidConstant = {
    val c = AndroidConstant.findByNameValue(className, attrName, constName, value.parseIntMaybeHex)

    println("    %3d %s %s:%s (%s) -> %s" format (count, tpe, attrName, constName, value, c))
    count += 1

    c match {
      case Some((const, _)) => const
      case None => RawConstantValue(value.parseIntMaybeHex)
    }
  }

  case class Enum(name: String, target: AndroidConstant) {
    def render = target.render
  }

  object Enum {
    def find(className: String, attrName: String, name: String, value: String): Enum = {
      val cName = name.toJavaConstFormat
      val res = lookup("[ENUM]", className, attrName, cName, value)
      Enum(name, res)
    }
  }

  case class Flag(name: String, target: AndroidConstant) {
    def render = target.render
  }

  object Flag {
    def find(className: String, attrName: String, name: String, value: String): Flag = {
      val cName = name.toJavaConstFormat
      val res = lookup("[FLAG]", className, attrName, cName, value)
      Flag(name, res)
    }
  }

  trait Format extends PartialFunction[String, Value] { self =>
    def orElse(that: Format): Format = new Format {
      def isDefinedAt(x: String) = self.isDefinedAt(x) || that.isDefinedAt(x)
      def apply(x: String) = if (self.isDefinedAt(x)) self(x) else that(x)
    }
  }

  object Format {

    def apply(str: String): Format = ResourceFormat orElse str.split('|').map(fromString).reduceLeft(_ orElse _)

    private def fromString(str: String): Format = str.toLowerCase match {
      case "boolean" => BooleanFormat
      case "color" => ColorFormat
      case "dimension" => DimensionFormat
      case "float" => FloatFormat
      case "integer" => IntegerFormat
      case "reference" => ReferenceFormat
      case "string" => ListenerFormat orElse StringFormat
      case "fraction" => FloatFormat // TODO
      case _ => throw new IllegalArgumentException("Unsupported format: "+ str)
    }

    object ResourceFormat extends Format {

      def isDefinedAt(x: String) = x.startsWith("@")

      def apply(str: String) = {
        val Array(prefix, value) = str.drop(1).split('/')
        val (pkg, tpe) = prefix.split(':') match {
          case Array(tpe) => (None, tpe)
          case Array(pkg, tpe) => (Some(pkg), tpe)
        }
        new Reference(pkg, tpe, value)
      }
    }

    object ReferenceFormat extends Format {

      def isDefinedAt(x: String) = x.startsWith("?")

      def apply(str: String) = {
        val Array(prefix, value) = str.drop(1).split('/')
        val (pkg, tpe) = prefix.split(':') match {
          case Array(tpe) => (None, tpe)
          case Array(pkg, tpe) => (Some(pkg), tpe)
        }
        new Reference(pkg, tpe, value)
      }
    }

    object IntegerFormat extends Format {
      def isDefinedAt(x: String) = Try(x.toInt).isSuccess
      def apply(str: String) = new Integral(str.toInt)
    }

    object FloatFormat extends Format {
      def isDefinedAt(x: String) = Try(x.toFloat).isSuccess
      def apply(str: String) = new Decimal(str.toFloat)
    }

    object ListenerFormat extends Format {
      def isDefinedAt(x: String) = x.startsWith("on") && x(2).isUpper
      def apply(str: String) = new Listener(str)
    }

    object StringFormat extends Format {
      def isDefinedAt(x: String) = true
      def apply(str: String) = new Str(str)
    }

    object BooleanFormat extends Format {
      def isDefinedAt(x: String) = Try(x.toBoolean).isSuccess
      def apply(str: String) = new Bool(str.toBoolean)
    }

    object ColorFormat extends Format {
      def isDefinedAt(x: String) = Try(x.parseColor).isSuccess
      def apply(str: String) = new Color(str.parseColor)
    }

    object DimensionFormat extends Format {

      private val units = Map("px" -> "", "dip" -> "dip", "dp" -> "dip", "sp" -> "sp", "sip" -> "sp")

      def isDefinedAt(x: String) = units.keys.exists(x.endsWith)

      def apply(str: String): Property.Value = {
        val x = str.toLowerCase
        val (from, to) = units.filterKeys(x.endsWith).head
        val amount = x.substring(0, x.length - from.length)
        if (amount.isNumeric)
          new Dimension(to, BigDecimal(amount))
        else
          throw new IllegalArgumentException("Illegal dimension format: "+ str)
      }
    }
  }
}


