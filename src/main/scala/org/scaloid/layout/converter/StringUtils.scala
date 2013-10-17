package org.scaloid.layout.converter

import scala.util.Try
import scala.language.implicitConversions
import org.apache.commons.lang3.StringEscapeUtils

class StringUtils(val str: String) extends AnyVal {

  def isJavaConstFormat = str.matches("[A-Z][A-Z_0-9]*[A-Z0-9]")

  def toJavaConstFormat =
    if (isJavaConstFormat) str
    else {
      val sb = new StringBuilder
      str.foreach { c =>
        if (c.isUpper)
          sb.append('_').append(c)
        else
          sb.append(c.toUpper)
      }
      sb.result
    }

  def underscored =
    if (str.isEmpty) ""
    else {
      val sb = new StringBuilder
      val it = str.iterator

      sb.append(it.next)
      while (it.hasNext) {
        val c = it.next
        if (c.isUpper)
          sb.append('_').append(c.toLower)
        else
          sb.append(c)
      }

      sb.result
    }

  def isNumeric = Try(str.toDouble).isSuccess

  def isIntegral = Try(str.toInt).isSuccess

  def className = str.split('.').last

  // from Android core framework
  def parseColor: Int =
    if (str.head == '#') {
      // Use a long to avoid rollovers on #ffXXXXXX
      val color = java.lang.Long.parseLong(str.drop(1), 16)
      (str.length match {
        case 9 => color
        case 7 => color | 0x00000000ff000000
        case _ => throw new IllegalArgumentException("Unknown color: "+ str)
      }).toInt
    } else
      StringUtils.colorNameMap.getOrElse(
        str.toLowerCase,
        throw new IllegalArgumentException("Unknown color: "+ str))

  def parseLongMaybeHex =
    if (str.startsWith("0x")) java.lang.Long.parseLong(str.drop(2), 16)
    else java.lang.Long.parseLong(str)

  def escaped = StringEscapeUtils.escapeJava(str)

  def tokenize = toJavaConstFormat.split('_').toVector.filter(_.nonEmpty)
}

object StringUtils {
  implicit def str2StringUtil(str: String) = new StringUtils(str)

  // from Android core framework
  private val colorNameMap = {
    import android.graphics.Color._
    Map(
      "black" -> BLACK,
      "darkgray" -> DKGRAY,
      "gray" -> GRAY,
      "lightgray" -> LTGRAY,
      "white" -> WHITE,
      "red" -> RED,
      "green" -> GREEN,
      "blue" -> BLUE,
      "yellow" -> YELLOW,
      "cyan" -> CYAN,
      "magenta" -> MAGENTA,
      "aqua" -> 0x00FFFF,
      "fuchsia" -> 0xFF00FF,
      "darkgrey" -> DKGRAY,
      "grey" -> GRAY,
      "lightgrey" -> LTGRAY,
      "lime" -> 0x00FF00,
      "maroon" -> 0x800000,
      "navy" -> 0x000080,
      "olive" -> 0x808000,
      "purple" -> 0x800080,
      "silver" -> 0xC0C0C0,
      "teal" -> 0x008080
    )
  }
}