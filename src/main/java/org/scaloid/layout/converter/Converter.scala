package org.scaloid.layout.converter

import xml.{Node, XML}
import collection.immutable.HashSet

object Converter {
  def apply(sourceStr: String): String = {
    try {
      new Converter(XML.loadString(sourceStr.trim)).toString()
    } catch {
      case e: Throwable => return e.getClass.getSimpleName + ": " + (e.getMessage)
    }
  }
}

class Converter(root: Node) {
  val out = new StringBuilder

  out.append("  override def onCreate(savedInstanceState: Bundle) {\n    super.onCreate(savedInstanceState)\n\n")
  printNode(root, "    ", true)
  out.append("  }")

  def processResource(text: String): String = {
    if (text equals "") return ""
    val lText = text.toLowerCase
    val units = Map("px" -> "", "dip" -> "dip", "dp" -> "dip", "sp" -> "sp", "sip" -> "sp")
    for ((key, value) <- units) {
      if (lText.endsWith(key)) {
        return lText.substring(0, lText.length - key.length) + " " + value
      }
    }

    if (text.startsWith("@")) {
      "R." + (text drop 1).replaceAll("/", ".")
    } else {
      "\"" + text + "\""
    }
  }

  def prop(node: Node, prop: String) = (node \ ("@{http://schemas.android.com/apk/res/android}" + prop)).text

  def textprop(node: Node, property: String) = processResource(prop(node, property))

  def isMatchParent(property: String) = {
    val prop = property.toLowerCase
    ("match_parent" equals prop) || ("fill_parent" equals prop)
  }

  def isWrapContent(property: String) = {
    val prop = property.toLowerCase
    "wrap_content" equals prop
  }

  def printNode(node: Node, indent: String, firstRun: Boolean = false) {
    val header: String = if (firstRun) "contentView += " else "this += "
    val processed = new HashSet[String]
    val label = node.label
    if (label.endsWith("#PCDATA")) {
      return
    }
    if (label.endsWith("Layout")) {
      out.append(indent + header + "new S" + label + " {\n")
      node.child.foreach(printNode(_, "  " + indent))
      out.append(indent + "}")
    } else {
      out.append(indent + header + "S" + label)

      label match {
        case "Button" =>
          val onClickFunc = prop(node, "onClick")
          val onClick = if (onClickFunc equals "") "" else ", " + onClickFunc
          out.append("(" + textprop(node, "text") + onClick + ")")
        case "TextView" => out.append("(" + textprop(node, "text") + ")")
        case _ =>
      }

    }


    //      node.attributes.foreach(i => out.append(i.key + i.value))
    if (!firstRun) {
      var enteredLayoutContext = false
      val lWidth = textprop(node, "layout_width")
      val lHeight = textprop(node, "layout_height")
      if (lWidth != "" && lHeight != "") {

        if (isMatchParent(lWidth) && isMatchParent(lHeight)) {
          out.append(".matchLayout")
          enteredLayoutContext = true
        } else if (isMatchParent(lWidth) && isWrapContent(lHeight)) {
          out.append(".layout")
          enteredLayoutContext = true
        } else if (isWrapContent(lWidth) && isWrapContent(lHeight)) {
          out.append(".wrapLayout")
          enteredLayoutContext = true
        }

      }
      if (enteredLayoutContext) out.append(".end")
    }

    val simpleConverts = List("textSize", "padding")
    simpleConverts.foreach(propName => {
      val proptext = textprop(node, propName)
      if (proptext != "") {
        out.append("." + propName + "(" + proptext + ")")
      }
    })

    val orientation = prop(node, "orientation")
    if (orientation != "") {
      out.append(".orientation(" + orientation.toUpperCase + ")")
    }

    out.append("\n")

  }

  override def toString() = out.toString

}