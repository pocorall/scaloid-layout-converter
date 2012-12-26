package org.scaloid.layout.converter

import xml.{Node, XML}

class Converter(root: Node) {
  def this(sourceStr: String) {
    this(XML.loadString(sourceStr.trim))
  }

  val out = new StringBuilder
  out.append("  override def onCreate(savedInstanceState: Bundle) {\n    super.onCreate(savedInstanceState)\n\n")
  printNode(root, "    ", "contentView += ")
  out.append("  }")

  def printNode(node: Node, indent: String, header: String = "this += ") {
    val label = node.label
    if (label.endsWith("Layout")) {
      out.append(indent + header + "new S" + label + " {\n")
      node.child.foreach(printNode(_, "  " + indent))
      out.append(indent + "}\n")
    } else if (label.endsWith("#PCDATA")) {
      // do nothing
    }
    else {
      out.append(indent + header + "S" + label + "\n")
    }

  }

  override def toString() = out.toString

}