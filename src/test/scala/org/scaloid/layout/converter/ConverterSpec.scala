package org.scaloid.layout.converter

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest._


@RunWith(classOf[JUnitRunner])
class ConverterSpec extends FunSpec with Matchers {
  import Converter._

  describe("convert") {

    it ("should convert an example XML") {
      renderWithWrappingMethod(convert(Fixtures.xml1)) should equal (
        """import android.view.Gravity
          |import android.view.ViewGroup.LayoutParams
          |
          |onCreate {
          |
          |  contentView = new SVerticalLayout {
          |    SButton(R.string.tuner_on, onTunerStart).<<.width(MATCH_PARENT).height(60 dip).>>.textSize(25 sp)
          |    this += new SLinearLayout {
          |      STextView("special: \\&<>").<<.width(WRAP_CONTENT).height(WRAP_CONTENT).>>.textAppearance(android.R.attr.textAppearanceMedium).requestFocus
          |      SButton("-").<<.width(0).height(WRAP_CONTENT).weight(1).>>.textSize(30 sp)
          |      SButton("+").<<.width(0).height(WRAP_CONTENT).weight(1).>>.textSize(30 sp)
          |      STextView(R.string.bps).<<.width(0).height(50 dip).weight(1).gravity(Gravity.CENTER).>>.textAppearance(android.R.attr.textAppearanceLarge).textColor(R.color.yellow).textSize(35 sp).gravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL)
          |    }
          |    this += new SLinearLayout {
          |      STextView(R.string.timesignature).<<.width(WRAP_CONTENT).height(WRAP_CONTENT).>>.textAppearance(android.R.attr.textAppearanceMedium)
          |      SSpinner().<<.width(WRAP_CONTENT).height(WRAP_CONTENT).weight(1).>>
          |    }
          |    this += new SLinearLayout {
          |      STextView(R.string.volume).<<.width(WRAP_CONTENT).height(MATCH_PARENT).>>.textAppearance(android.R.attr.textAppearanceMedium)
          |      SSeekBar().<<.weight(1).>>.padding(10 dip)
          |    }
          |    SButton(R.string.start, onStartStopClick).<<.fill.>>.textColor(0xff6789ab).textSize(70 sp)
          |  }.<<.fill.>>
          |}
        """.stripMargin.trim)

    }
  }

}
