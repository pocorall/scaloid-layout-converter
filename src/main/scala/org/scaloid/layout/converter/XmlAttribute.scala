package org.scaloid.layout.converter


object XmlAttributes {
  import scala.xml._
  import XmlAttribute._

  private val apiVersion = 8 // TODO make configurable

  private def xmlUri = s"android-$apiVersion/data/res/values/attrs.xml" // TODO from more versions

  val byName: Map[String, XmlAttribute] = {
    def nodeToAttr(node: Node): XmlAttribute = {
      val name = (node \ "@name").text

      // TODO deduplicate
      val enums = node.nonEmptyChildren.filter(_.label == "enum").map { enumTag => Enum.find(name, (enumTag \ "@name").text) }
      val flags = node.nonEmptyChildren.filter(_.label == "flag") map { flagTag => Flag.find(name, (flagTag \ "@name").text) }

      val format = node \ "@format" match {
        case NodeSeq.Empty => Format.ResourceFormat
        case n => Format(n.text)
      }

      new XmlAttribute(name, format, enums.toList, flags.toList)
    }

    (XML.load(getClass.getClassLoader.getResource(xmlUri)) \\ "attr")
      .view
      .filter(tag => (tag \ "@format").nonEmpty || tag.child.nonEmpty)
      .map(nodeToAttr)
      .map(a => a.name -> a)
      .toMap
  }
}


class XmlAttribute(val name: String, _format: XmlAttribute.Format, enums: List[XmlAttribute.Enum], flags: List[XmlAttribute.Flag]) {
  import XmlAttribute._
  import Format._

  private val format = _format orElse new EnumFormat(enums) orElse new FlagsFormat(flags)

  val renderName: String = if (name.startsWith("layout_")) name.drop(7) else name

  def parse(str: String) = format(str)
}

object XmlAttribute {
  import scala.util.Try
  import StringUtils._
  import Property._
  import Format._

  def custom(name: String, format: Format = ResourceFormat, enums: List[Enum] = Nil, flags: List[Flag] = Nil) =
    new XmlAttribute(name, format, enums, flags)

  case class Enum(name: String, target: String) {
    def render = target
  }

  object Enum {
    def find(attrName: String, name: String): Enum = {
      Enum(name, name.toJavaConstFormat)
    }
  }

  case class Flag(name: String, target: String) {
    def render = target
  }

  object Flag {
    def find(attrName: String, name: String): Flag = {
      Flag(name, name.toJavaConstFormat)
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

    class EnumFormat(enums: List[Enum]) extends Format {
      def isDefinedAt(x: String) = enums exists (_.name == x.toJavaConstFormat)
      def apply(x: String) = Constants(x.toJavaConstFormat)
    }

    class FlagsFormat(enums: List[Flag]) extends Format {
      def isDefinedAt(x: String) = enums exists (_.name == x.toJavaConstFormat)
      def apply(x: String) = Constants(x.split('|').map(_.toJavaConstFormat).toList)
    }
  }
}


