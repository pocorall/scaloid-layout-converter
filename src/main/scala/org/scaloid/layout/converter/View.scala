package org.scaloid.layout.converter


sealed trait View {
  def name: String
  def props: List[Property]
  def layoutParams: List[Property]
  def render(indent: String = "", parent: Option[View] = None): String

  def hasProp(name: String) = props.exists(_.attr.name == name)

  def hasLayoutParam(name: String) = layoutParams.exists(_.attr.name == name)

  def prop(name: String) = props.find(_.attr.name == name)

  def layoutParam(name: String) = layoutParams.find(_.attr.renderName == name)

  protected def renderProps: String =
    if (props.isEmpty) ""
    else "."+ props.map(_.render).mkString(".")

  protected def renderLayoutParams: String =
    if (layoutParams.isEmpty) ""
    else layoutParams.map(_.render).mkString(".<<.", ".", ".>>")

  protected def propsWithoutNames(ps: List[Property], names: Seq[String]) =
    ps.filterNot(p => names contains p.attr.name)

  def prependProps(ps: Property*) = this
  def prependLayoutParams(lps: Property*) = this
  def withoutProps(ps: String*) = this
  def withoutLayoutParams(lps: String*) = this
}

case class Widget(name: String,
                  props: List[Property] = Nil,
                  layoutParams: List[Property] = Nil,
                  constParams: List[Property] = Nil) extends View {

  def render(indent: String = "", parent: Option[View] = None) =
    s"$indent$name($renderConstParams)" + renderLayoutParams + renderProps

  private def renderConstParams = constParams.map(_.value.render).mkString(", ")

  override def prependProps(newProps: Property*) = copy(props = newProps ++: props)

  override def prependLayoutParams(newLPs: Property*) = copy(layoutParams = newLPs ++: layoutParams)

  def withConstParams(params: Property*) = copy(constParams = params.toList)

  override def withoutProps(names: String*) =
    copy(props = propsWithoutNames(props, names))

  override def withoutLayoutParams(names: String*) =
    copy(layoutParams = propsWithoutNames(layoutParams, names.map("layout_"+_)))
}

case class ViewGroup(name: String,
                     props: List[Property] = Nil,
                     layoutParams: List[Property] = Nil,
                     children: List[View] = Nil) extends View {

  def render(indent: String = "", parent: Option[View] = None) =
    s"$indent${ parent.fold("contentView =")(_ => "this +=") } new $name {" +
      renderChildren(indent) +
      "}" + renderLayoutParams + renderProps

  private def renderChildren(indent: String) =
    if (children.isEmpty) ""
    else children.map(_.render(indent + "  ", Some(this))).mkString("\n", "\n", "\n"+ indent)

  override def prependProps(newProps: Property*) = copy(props = props ++ newProps)

  override def prependLayoutParams(newLPs: Property*) = copy(layoutParams = layoutParams ++ newLPs)

  override def withoutProps(names: String*) =
    copy(props = propsWithoutNames(props, names))

  override def withoutLayoutParams(names: String*) =
    copy(layoutParams = propsWithoutNames(layoutParams, names.map("layout_"+_)))
}


trait NonView extends View {
  val props = Nil
  val layoutParams = Nil
  val parent = None
}

case class Failed(name: String) extends NonView {
  def render(indent: String, parent: Option[View]) = s"$indent/* Failed to recognize '$name'. */"
}

case class Skipped(name: String) extends NonView {
  def render(indent: String, parent: Option[View]) = indent
}
