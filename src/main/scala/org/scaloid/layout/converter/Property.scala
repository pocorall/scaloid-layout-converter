package org.scaloid.layout.converter

case class Property(attr: XmlAttribute, value: Property.Value) {
  def render: String = s"${attr.renderName}$renderValue"

  private def renderValue: String = value match {
    case Property.Empty => ""
    case _ => s"(${value.render})"
  }
}

object Property {
  import StringUtils._

  sealed trait Value {
    def render: String
  }

  case class Integral(value: Int) extends Value {
    def render = value.toString
  }

  case class Bool(value: Boolean) extends Value {
    def render = value.toString
  }

  case class Color(value: Int) extends Value {
    def render = "0x%08x".format(value)
  }

  case class Decimal(value: Float) extends Value {
    def render = value.toString.replaceFirst("\\.0$", "")
  }

  case class Dimension(unit: String, value: BigDecimal) extends Value {
    def render = s"$value $unit".trim
  }

  case class Reference(pkg: Option[String], tpe: String, value: String) extends Value {
    def render = s"${pkg.fold("")(_+".")}R.$tpe.$value".trim
  }

  case class Str(value: String) extends Value {
    def render = "\""+ value.escaped +"\""
  }

  case class Listener(value: String) extends Value {
    def render = value.trim
  }

  object Empty extends Value {
    def render = ""
  }

  case class Constants(values: List[AndroidConstant]) extends Value {
    def render = values.map(_.render).mkString(" | ")
  }
  object Constants {
    def apply(value: AndroidConstant, values: AndroidConstant*): Constants = new Constants(value :: values.toList)
  }

  def custom(name: String, value: Value = Empty) = Property(XmlAttribute.custom(name), value)

}
