package org.scaloid.layout.converter


case class AndroidConstant(name: String, declaringClass: Class[_], value: Long, isEnum: Boolean) {
  def isConst = ! isEnum
  def fqcn = declaringClass.getName +"."+ name
  override def toString = s"${fqcn.replace("android.", "")}($value)"
}


object AndroidConstant {
  import scala.collection.JavaConverters._
  import StringUtils._

  val r = {
    import org.reflections._
    import scanners._
    import util._
    new Reflections(
      new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forPackage("android"))
        .setScanners(new SubTypesScanner(false))
    )
  }

  val clss = r.getSubTypesOf(classOf[java.lang.Object]).asScala.toSet[Class[_]]

  private val consts =
    for {
      cls <- clss
      field <- cls.getDeclaredFields
      if field.toString.startsWith("public static final int ") &&
        field.getName.isJavaConstFormat
    } yield AndroidConstant(field.getName, cls, field.getLong(null), false)

  private val enums =
    clss.flatMap { c =>
      ((if (c.isEnum) List(c) else Nil) ++ c.getClasses.filter(_.isEnum)).flatMap {
        _.getEnumConstants.map(_.asInstanceOf[java.lang.Enum[_]]) map { enum =>
          AndroidConstant(enum.name, enum.getDeclaringClass, enum.ordinal, true)
        }
      }
    }

  val reverseIndex =
    (Map.empty[String, Set[AndroidConstant]].withDefaultValue(Set.empty) /: (consts ++ enums)) { (idx, const) =>
      val tokens = const.declaringClass.getSimpleName.tokenize ++ const.name.tokenize
      (idx /: tokens) { (idx2, token) => idx2 updated (token, idx2(token) + const) }
    }

  def findByNameValue(className: String, attrName: String, constName: String, _value: Long) =
    if (ignored(className, attrName, constName)) None
    else {
      val minorTokens = List(className, attrName).flatMap(_.tokenize)
      val majorTokens = adjustToken(className, attrName, constName)
      val tokens = minorTokens ++ majorTokens
      val priorities = (minorTokens.map(t => t -> adjustPriority(t)) ++ majorTokens.zip(Stream from 100)).toMap
      val tokensWithCandidates = tokens map (t => t -> reverseIndex(t))
      val value = adjustValue(className, attrName, constName) getOrElse _value

      (Map.empty[AndroidConstant, Int].withDefaultValue(0) /: tokensWithCandidates) {
        case (map, (token, consts)) =>
          (map /: consts) { (map2, const) =>
            if (const.isEnum || const.value == value)
              map2 updated (const, map(const) + priorities(token))
            else
              map2
          }

      }.filter(_._2 > 100).toList match {
        case Nil => None
        case cs => Some(cs.maxBy(_._2))
      }
    }


  // TODO organized manual adjustments

  // black magic
  private val adjustPriority = (_: String) match {
    case "GRAVITY" | "PERSISTENT" | "SCROLLBARS" | "FADING" | "CACHE" | "EDGE" | "STREAM" => 10
    case "TYPE" | "MODE" => 1
    case _ => 2
  }

  private val adjustToken = (_: (String, String, String)) match {
    case ("ImageView", "scaleType", "FIT_X_Y") => List("FIT", "XY")
    case ("ProgressBar", "indeterminateBehavior", "REPEAT") => List("Animation", "RESTART")
    case ("ProgressBar", "indeterminateBehavior", "CYCLE") => List("Animation", "REVERSE")
    case (_, _, constName) => constName.tokenize
  }

  private val adjustValue = {
    import android.view.{View => V, ViewGroup => VG}

    (_: (String, String, String)) match {
      case ("View", "visibility", "INVISIBLE") => Some(V.INVISIBLE)
      case ("View", "visibility", "GONE") => Some(V.GONE)
      case ("View", "drawingCacheQuality", "LOW") => Some(V.DRAWING_CACHE_QUALITY_LOW)
      case ("View", "drawingCacheQuality", "HIGH") => Some(V.DRAWING_CACHE_QUALITY_HIGH)
      case ("ViewGroup", "descendantFocusability", "BEFORE_DESCENDANTS") => Some(VG.FOCUS_BEFORE_DESCENDANTS)
      case ("ViewGroup", "descendantFocusability", "AFTER_DESCENDANTS") => Some(VG.FOCUS_AFTER_DESCENDANTS)
      case ("ViewGroup", "descendantFocusability", "BLOCKS_DESCENDANTS") => Some(VG.FOCUS_BLOCK_DESCENDANTS)
      case (_, _, _) => None
    }
  }

  private val ignored = (_: (String, String, String)) match {
    // not declared as public
    case ("View", "scrollbars", _) => true
    case ("View", "fadingEdge", _) => true

    // not defined explicitly
    case ("Searchable", _, _) => true
    case ("TwoLineListItem", _, _) => true
    case ("MenuGroup", _, _) => true
    case ("TextView", "marqueeRepeatLimit", _) => true

    case _ => false
  }

}
