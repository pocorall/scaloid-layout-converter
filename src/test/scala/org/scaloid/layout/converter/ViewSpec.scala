package org.scaloid.layout.converter

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest._


@RunWith(classOf[JUnitRunner])
class ViewSpec extends FunSpec with Matchers {

  describe("View") {

    describe("Widget") {

      it ("should render Scaloid widget properly") {
        Widget("SButton").render() should equal ("SButton()")
      }
    }

    describe("ViewGroup") {

      it ("should render flat Scaloid layouts properly") {
        ViewGroup("SLinearLayout", children = List(
          Widget("SButton")
        )).render() should equal (
          """
            |contentView = new SLinearLayout {
            |  SButton()
            |}
          """.stripMargin.trim)
      }

      it ("should render nested Scaloid layouts properly") {
        ViewGroup("SLinearLayout", children = List(
          Widget("SButton"),
          ViewGroup("SRelativeLayout")
        )).render() should equal (
          """
            |contentView = new SLinearLayout {
            |  SButton()
            |  this += new SRelativeLayout {}
            |}
          """.stripMargin.trim)
      }
    }
  }
}
