package org.scaloid.layout.converter


sealed trait AndroidConstant { def render: String }

case class RawConstantValue(value: Long) extends AndroidConstant {
  def render = value.toString
}
case class ConstantRef(name: String, declaringClass: Class[_], value: Long, isEnum: Boolean) extends AndroidConstant {
  def isConst = ! isEnum
  def fqcn = declaringClass.getName +"."+ name
  override def toString = s"${fqcn.replace("android.", "")}($value)"
  def render = name
}


object AndroidConstant {
  import scala.collection.JavaConverters._
  import StringUtils._

  def findByNameValue(className: String, attrName: String, constName: String, xmlValue: Long) =
    if (ignored(className, attrName, constName)) None
    else {
      val minorTokens = List(className, attrName).flatMap(_.tokenize)
      val majorTokens = adjustToken(className, attrName, constName)
      val tokens = minorTokens ++ majorTokens
      val tokenPriority = (minorTokens.map(t => t -> adjustPriority(t)) ++ majorTokens.zip(Stream from 100)).toMap

      val tokensWithCandidates = tokens map (t => t -> reverseIndex(t))
      val codeValue = adjustValue(className, attrName, constName) getOrElse xmlValue

      val lastToken = majorTokens.last
      val initialMap: Map[ConstantRef, Int] =
        tokensWithCandidates.last._2.map { const =>
          val initialScore =
            if (const.name == constName) 20
            else if (const.name endsWith lastToken) 10
            else 0

          const -> initialScore
        }.toMap withDefaultValue 0

      (initialMap /: tokensWithCandidates) { case (map, (token, consts)) =>
        (map /: consts) { (map2, const) =>
          if (const.isEnum || const.value == codeValue)
            map2 updated (const, map(const) + tokenPriority(token))
          else
            map2
        }
      }.filter(_._2 > 100).toList match {
        case Nil => None
        case cs => Some(cs.maxBy(_._2))
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

    val classes = reflections.getSubTypesOf(classOf[java.lang.Object]).asScala.toSet[Class[_]]

    def isPublicStaticFinalInt(field: java.lang.reflect.Field) = {
      val psf = {
        import java.lang.reflect.Modifier._
        PUBLIC | STATIC | FINAL
      }
      (field.getModifiers & psf) == psf && field.getType == classOf[Int]
    }

    val consts =
      for {
        cls <- classes
        field <- cls.getDeclaredFields
        if isPublicStaticFinalInt(field) && field.getName.isJavaConstFormat
      } yield ConstantRef(field.getName, cls, field.getLong(null), false)

    val enums =
      classes.flatMap { c =>
        ((if (c.isEnum) List(c) else Nil) ++ c.getClasses.filter(_.isEnum)).flatMap {
          _.getEnumConstants.map(_.asInstanceOf[java.lang.Enum[_]]) map { enum =>
            ConstantRef(enum.name, enum.getDeclaringClass, enum.ordinal, true)
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
    case ("ProgressBar", "indeterminateBehavior", "REPEAT") => List("Animation", "RESTART")
    case ("ProgressBar", "indeterminateBehavior", "CYCLE") => List("Animation", "REVERSE")
    case (_, _, constName) => constName.tokenize
  }

  // TODO find more consistent rule
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

  // TODO black magic! rework for higher API versions than 8
  private val adjustPriority = (_: String) match {
    case "GRAVITY" | "PERSISTENT" | "SCROLLBARS" | "FADING" | "CACHE" | "EDGE" | "STREAM" => 10
    case "TYPE" | "MODE" => 1
    case _ => 2
  }

  // TODO add explicit explanation for users
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
