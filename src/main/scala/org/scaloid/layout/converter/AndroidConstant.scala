package org.scaloid.layout.converter

import scala.language.existentials


sealed trait AndroidConstant { def render: String }

case class RawConstantValue(value: Any) extends AndroidConstant {
  def render = value.toString
}

case class ConstantRef(name: String, declaringClass: Class[_], value: Any) extends AndroidConstant {
  import ConstantRef._

  def fqcn = declaringClass.getName +"."+ name

  def render =
    if (predefined.get(declaringClass).exists(_.contains(name))) name
    else declaringClass.getSimpleName +"."+ name

  override def toString = s"${fqcn.replace("android.", "")}($value)"

  override def equals(other: Any): Boolean =
    other match {
      case ConstantRef(name, cls, _) => cls == this.declaringClass && name == this.name
      case _ => false
    }
}

object ConstantRef {

  private val predefined: Map[Class[_], Set[String]] = Map(
    classOf[android.view.ViewGroup.LayoutParams] -> Set("FILL_PARENT", "MATCH_PARENT", "WRAP_CONTENT")
  )

}

object AndroidConstant {
  import StringUtils._

  def apply(cls: Class[_], name: String): AndroidConstant = {
    val field = cls.getField(name)
    require(isPublicStaticFinal(field))
    ConstantRef(name, cls, field.get(null))
  }

  def findByNameValue(className: String, attrName: String, constName: String, xmlValue: Int) =
    if (ignored(className, attrName, constName)) None
    else {
      val minorTokens = List(className, attrName).flatMap(_.tokenize)
      val majorTokens = adjustToken(className, attrName, constName)
      val tokens = minorTokens ++ majorTokens
      val tokenPriority = (minorTokens.map(t => t -> adjustPriority(t)) ++ majorTokens.zip(Stream from 100)).toMap

      val tokensWithCandidates = tokens map (t => t -> reverseIndex(t))

      val lastToken = majorTokens.last
      val initialMap: Map[ConstantRef, Int] =
        tokensWithCandidates.last._2.map { const =>
          val initialScore =
            if (const.name endsWith lastToken) 10
            else 0

          const -> initialScore
        }.toMap withDefaultValue 0

      (initialMap /: tokensWithCandidates) { case (map, (token, consts)) =>
        (map /: consts) { (map2, const) =>
          map2 updated (const, map(const) + tokenPriority(token))
        }
      }.filter(_._2 > 100).toList match {
        case Nil => None
        case cs => Some(cs.maxBy { case (const, score) => (score, - const.fqcn.length) })
      }
    }

  private val reverseIndex = {

    val reflections = {
      import org.reflections._
      import scanners._
      import util._
      new Reflections(
        new ConfigurationBuilder()
          .setUrls(ClasspathHelper.forPackage("android"))
          .setScanners(new SubTypesScanner(false))
      )
    }

    // Not `reflections.getSubtypesOf` due to interfaces
    val classLoader = getClass.getClassLoader
    val classes = reflections.getStore.getStoreMap.get("SubTypesScanner")
      .values.toArray.map(name => classLoader.loadClass(name.asInstanceOf[String])).toSet

    val consts =
      for {
        cls <- classes
        field <- cls.getDeclaredFields
        if isPublicStaticFinal(field) && field.getName.isJavaConstFormat
      } yield ConstantRef(field.getName, cls, field.get(null))

    val enums =
      classes.flatMap { c =>
        ((if (c.isEnum) List(c) else Nil) ++ c.getClasses.filter(_.isEnum)).flatMap {
          _.getEnumConstants.map(_.asInstanceOf[java.lang.Enum[_]]) map { enum =>
            ConstantRef(enum.name, enum.getDeclaringClass, enum.ordinal)
          }
        }
      }

    val initialMap = Map.empty[String, Set[ConstantRef]] withDefaultValue Set.empty

    (initialMap /: (consts ++ enums)) { (idx, const) =>
      val tokens = const.declaringClass.getSimpleName.tokenize ++ const.name.tokenize
      (idx /: tokens) { (idx2, token) => idx2 updated (token, idx2(token) + const) }
    }
  }

  private val adjustToken = (_: (String, String, String)) match {
    case (_, "autoLink", word) => List("LINKIFY", word.toJavaConstFormat)
    case (_, "ellipsize", word) => List("TRUNCATE", "AT", word)
    case (_, "imeOptions", "NORMAL") => List("IME", "NULL")
    case (_, "inputType", "NONE") => List("TYPE", "NULL")
    case (_, "orientation", word) => List("LAYOUT", "PARAMS", word.toJavaConstFormat)
    case ("Animation", "zAdjustment", word) => List("ANIMATION", "ZORDER", word.toJavaConstFormat)
    case ("ProgressBar", "indeterminateBehavior", "REPEAT") => List("ANIMATION", "RESTART")
    case ("ProgressBar", "indeterminateBehavior", "CYCLE") => List("ANIMATION", "REVERSE")
    case ("ViewGroup", "persistentDrawingCache", "NONE") => List("NO", "CACHE")
    case (_, _, constName) => constName.tokenize
  }

  private val importantWords = Set(
    "ANIMATION", "CACHE", "DRAWING", "EDGE", "FADING", "GRADIENT", "GRAVITY",
    "ORDER", "PERSISTENT", "RINGTONE", "SCROLLBARS", "STREAM", "STREAM", "TRANSCRIPT"
  )

  // TODO black magic! rework for higher API versions than 8
  private val adjustPriority = (_: String) match {
    case words if importantWords(words) => 12
    case _ => 2
  }

  // TODO add explicit explanation for users
  private val ignored = (_: (String, String, String)) match {
    // not declared as public
    case ("View", "fadingEdge", _) => true
    case ("View", "scrollbars", _) => true

    // compound properties
    case (_, "capitalize", _) => true
    case (_, "textStyle", _) => true

    // not defined explicitly
    case (_, "autoLink", "NONE") => true
    case (_, "ellipsize", "NONE") => true
    case ("BitmapDrawable", "tileMode", "DISABLED") => true
    case ("MenuGroup", _, _) => true
    case ("Searchable", _, _) => true
    case ("TextView", "marqueeRepeatLimit", _) => true
    case ("TextView", "numeric", _) => true
    case ("TwoLineListItem", _, _) => true

    case _ => false
  }

  private def isPublicStaticFinal(field: java.lang.reflect.Field) = {
    import java.lang.reflect.Modifier._
    val psf = PUBLIC | STATIC | FINAL
    (field.getModifiers & psf) == psf
  }
}
