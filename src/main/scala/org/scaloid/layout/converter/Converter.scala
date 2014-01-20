package org.scaloid.layout.converter

import scala.xml.NodeSeq
import org.scaloid.layout.converter.Property.Constants

object Converter {
  import ReflectionUtils._
  import StringUtils._
  import xml.{Node, XML}

  def apply(sourceStr: String): String =
    try renderWithWrappingMethod(convert(XML.loadString(sourceStr)))
    catch {
      case e: Throwable => return e.getClass.getSimpleName + ": " + e.getMessage
    }

  def renderWithWrappingMethod(view: View) = {
    val importClause =
      collectImports(view)
        .map(_.getName.replace("$", "."))
        .map("import " + _)
        .toList.sorted.mkString("\n")

    s"""$importClause
       |
       |onCreate {
       |
       |${view.render("  ")}
       |}
     """.stripMargin.trim
  }

  def convert(node: Node): View =
    trySkip(node) orElse tryView(node) getOrElse Failed(node.label)

  def tryView(node: Node, parent: Option[Type] = None): Option[View] =
    toType("android.widget", node.label).flatMap { aType =>
      aType.scaloidHelper map { sType =>
        val name = sType.typeConstructor.toString.className
        val (layoutParams, _props) = extractProps(node, aType, sType).partition(_.attr.name.startsWith("layout_"))
        val props = node \ "requestFocus" match {
          case NodeSeq.Empty => _props
          case _ => _props :+ Property.custom("requestFocus")
        }

        if (aType.isLayout)
          ViewGroup(
            name, props, layoutParams,
            node.child.map(tryView(_, Some(aType))).flatten.toList)
        else if (aType.isWidget)
          Widget(name, props, layoutParams)
        else
          Failed(node.label)
      }
    }.map(Transform)

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

  // TODO remove classes already in scope
  def collectImports(view: View) = {

    def run(view: View, acc: Set[Class[_]]): Set[Class[_]] = {
      val classes =
        acc ++
          (view.props ++ view.layoutParams).map(_.value).collect {
            case Constants(cs) => cs.collect {
              case ConstantRef(name, cls, _) if ! ConstantRef.isPredefined(cls, name) => cls
            }
          }.flatten

      view match {
        case vg: ViewGroup => vg.children.flatMap(run(_, classes)).toSet
        case _ => classes
      }
    }

    run(view, Set.empty)
  }
}
