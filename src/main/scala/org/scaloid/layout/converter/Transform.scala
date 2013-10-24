package org.scaloid.layout.converter


object Transform extends (View => View) {
  import org.scaloid.layout.converter.Property._

  // TODO better transform composition
  def apply(view: View): View = {
    val view2 = transformCommon(view)
    val view3 =
      view2 match {
        case w: Widget => transformWidget(w)
        case vg: ViewGroup => transformViewGroup(vg)
        case _ => view2
      }
    view3
  }

  def transformCommon(view: View): View =
    (view.layoutParam("width"), view.layoutParam("height")) match {
      case (Some(w), Some(h)) if isMatchParent(w) =>
        if (isWrapContent(h))
          view.withoutLayoutParams("width", "height")
        else if (isMatchParent(h))
          view.withoutLayoutParams("width", "height").prependLayoutParams(Property.custom("fill"))
        else
          view

      case _ => view
    }

  def transformWidget(widget: Widget): Widget =
    widget.name match {
      case "STextView" =>
        widget.prop("text") match {
          case Some(p) => widget.withoutProps("text").withConstParams(p)
          case None => widget
        }

      case "SButton" =>
        (widget.prop("text"), widget.prop("onClick")) match {
          case (Some(text), Some(onClick)) =>
            widget.withoutProps("text", "onClick").withConstParams(text, onClick)
          case (Some(text), None) =>
            widget.withoutProps("text").withConstParams(text)
          case _ =>
            widget
        }

      case _ => widget
    }


  import android.widget.{ LinearLayout => LL }
  import android.view.ViewGroup.{ LayoutParams => LP }

  val VERTICAL = Constants(AndroidConstant(classOf[LL], "VERTICAL"))
  val HORIZONTAL = Constants(AndroidConstant(classOf[LL], "HORIZONTAL"))
  val MATCH_PARENT = Constants(AndroidConstant(classOf[LP], "MATCH_PARENT"))
  val FILL_PARENT = Constants(AndroidConstant(classOf[LP], "FILL_PARENT"))
  val WRAP_CONTENT = Constants(AndroidConstant(classOf[LP], "WRAP_CONTENT"))

  def transformViewGroup(vg: ViewGroup): ViewGroup =
    vg.name match {
      case "SLinearLayout" =>
        vg.prop("orientation") match {
          case Some(o) if o.value == VERTICAL =>
            vg.withoutProps("orientation").copy(name = "SVerticalLayout")
          case _ => vg
        }
      case _ => vg
    }

  private def isMatchParent(prop: Property) = Seq(MATCH_PARENT, FILL_PARENT) contains prop.value

  private def isWrapContent(prop: Property) = prop.value == WRAP_CONTENT
}
