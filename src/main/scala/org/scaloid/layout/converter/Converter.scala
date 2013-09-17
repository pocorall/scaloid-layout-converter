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

  def upperFormat(str: String) = {
    val sb = new StringBuffer
    str.foreach(c => {
      if (c.isUpper) {
        sb.append("_").append(c)
      }
      else {
        sb.append(c.toUpper)
      }
    })
    sb.toString
  }

  def printNode(node: Node, indent: String, firstRun: Boolean = false) {
    val processed = new HashSet[String]
    var label = node.label
    if (label.endsWith("#PCDATA")) {
      return
    }
    if (label.endsWith("Layout")) {
      val header: String = if (firstRun) "contentView = " else "this += "
      if ("LinearLayout".equals(label) && prop(node, "orientation").equalsIgnoreCase("VERTICAL")) {
        label = "VerticalLayout"
      }
      out.append(indent + header + "new S" + label + " {\n")

      node.child.foreach(printNode(_, "  " + indent))
      out.append(indent + "}")
    } else {
      out.append(indent + "S" + label)

      out.append("(")
      label match {
        case "Button" =>
          val onClickFunc = prop(node, "onClick")
          val onClick = if (onClickFunc equals "") "" else ", " + onClickFunc + " _"
          out.append(textprop(node, "text") + onClick)
        case "TextView" => out.append(textprop(node, "text"))
        case _ =>
      }
      out.append(")")
    }

    if (!firstRun) {
      var enteredLayoutContext = false
      def appendLayoutProperty(str: String) {
        if (!enteredLayoutContext) {
          out.append(".<<")
          enteredLayoutContext = true
        }
        out.append(str)
      }
      val lWidth = prop(node, "layout_width")
      val lHeight = prop(node, "layout_height")
      if (lWidth != "" && lHeight != "") {
        if (isMatchParent(lWidth) && isMatchParent(lHeight)) {
          appendLayoutProperty(".fill")
        } else if (isMatchParent(lWidth) && isWrapContent(lHeight)) {
          //          out.append(".layout")
          //          enteredLayoutContext = true
        } else if (isWrapContent(lWidth) && isWrapContent(lHeight)) {
          appendLayoutProperty(".wrap")
        }
      }

      val layoutProps = List("marginBottom", "marginTop", "marginLeft", "marginRight", "margin")
      layoutProps.foreach(propName => {
        val p = textprop(node, "layout_" + propName)
        if (p != "") {
          appendLayoutProperty("." + propName + "(" + p + ")")
        }
      })

      if (enteredLayoutContext) out.append(".>>")
    }

    val simpleConverts = List("textSize", "padding")
    simpleConverts.foreach(propName => {
      val proptext = textprop(node, propName)
      if (proptext != "") {
        out.append("." + propName + "(" + proptext + ")")
      }
    })

    val uppercaseConverts = List("inputType")
    uppercaseConverts.foreach(propName => {
      val proptext = prop(node, propName)
      if (proptext != "") {
        out.append("." + propName + "(" + upperFormat(proptext) + ")")
      }
    })

    out.append("\n")

  }

  override def toString() = out.toString

}