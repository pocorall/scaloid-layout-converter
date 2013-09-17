package org.scaloid.layout.converter

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest._


@RunWith(classOf[JUnitRunner])
class ReflectionUtilsSpec extends FunSpec with Matchers {

  import ReflectionUtils._
  import reflect.runtime.universe.typeTag

  describe("ReflectionUtils") {

    describe ("toType") {

      it ("should reify a Type from a FQCN") {
        toType("android.widget.Button") should equal (Some(typeTag[android.widget.Button].tpe))
        toType("org.scaloid.common.TraitView") should equal (Some(typeTag[org.scaloid.common.TraitView[_]].tpe.typeConstructor))
      }

      it ("should return None from a non-existing FQCN") {
        toType("android.widget.non_existing_class") should equal (None)
      }
    }

    describe ("ReflectionOps") {

      it ("should determine android class types") {
        val button = toType("android.widget.Button").get
        button.isView should be (true)
        button.isWidget should be (true)
        button.isLayout should be (false)

        val linearLayout = toType("android.widget.LinearLayout").get
        linearLayout.isView should be (true)
        linearLayout.isWidget should be (false)
        linearLayout.isLayout should be (true)

        val activity = toType("android.app.Activity").get
        activity.isView should be (false)
        activity.isWidget should be (false)
        activity.isLayout should be (false)
      }

    }

    describe("findScaloidHelper") {

      it ("should be able to find existing Scaloid helpers") {
        val button = toType("android.widget.Button").get
        button.scaloidHelper should equal (Some(typeTag[org.scaloid.common.SButton].tpe))

        val linearLayout = toType("android.widget.LinearLayout").get
        linearLayout.scaloidHelper should equal (Some(typeTag[org.scaloid.common.SLinearLayout].tpe))

        val list = toType("scala.collection.immutable.List").get
        list.scaloidHelper should equal (None)
      }

    }
  }

}
