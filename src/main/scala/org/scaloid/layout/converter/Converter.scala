package org.scaloid.layout.converter

object Converter {
  import ReflectionUtils._
  import StringUtils._
  import xml.{Node, XML}

  def apply(sourceStr: String): String =
    try renderWithWrappingMethod(convert(XML.loadString(sourceStr)))
    catch {
      case e: Throwable => return e.getClass.getSimpleName + ": " + (e.getMessage)
    }

  def renderWithWrappingMethod(view: View) =
    s"""
       |override def onCreate(savedInstanceState: Bundle) {
       |  super.onCreate(savedInstanceState)
       |
       |${view.render("  ")}
       |}
     """.stripMargin.trim

  def convert(node: Node): View =
    trySkip(node) orElse tryView(node) getOrElse Failed(node.label)

  def tryView(node: Node, parent: Option[Type] = None): Option[View] =
    toType("android.widget", node.label).flatMap { aType =>
      aType.scaloidHelper map { sType =>
        val name = sType.typeConstructor.toString.className
        val (layoutParams, props) = extractProps(node, aType, sType).partition(_.attr.name.startsWith("layout_"))

        if (aType.isLayout)
          ViewGroup(
            name, props, layoutParams,
            node.child.map(tryView(_, Some(aType))).flatten.toList)
        else if (aType.isWidget)
          Widget(name, props, layoutParams)
        else
          Failed(node.label)
      }
    }.map (Transform(_))

  def trySkip(node: Node): Option[View] =
    if (node.label.endsWith("#PCDATA")) Some(Skipped(node.label))
    else None

  def extractProps(node: Node, aType: Type, sType: Type): List[Property] =
    node.attributes.toList.map { nodeAttr =>
      if (nodeAttr.key == "id") None
      else
        XmlAttributes.byName.get(nodeAttr.key) map { attr =>
          val value = attr.parse(nodeAttr.value.text)
          Property(attr, value)
        }
    }.flatten
}
