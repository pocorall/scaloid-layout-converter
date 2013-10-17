package org.scaloid.layout.converter


object ConstantLookup {
  import scala.collection.JavaConverters._
  import StringUtils._
  import scala.language.existentials

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

  case class AndroidConstant(
    name: String,
    declaringClass: Class[_],
    value: Long,
    isEnum: Boolean
  ) {
    def isConst = ! isEnum
    def fqcn = declaringClass.getName +"."+ name
    override def toString = s"${fqcn.replace("android.", "")}($value)"
  }

  private val consts =
    for {
      cls <- clss
      field <- cls.getDeclaredFields
      if (field.toString.startsWith("public static final int ") ||
        field.toString.startsWith("public static final long ")) &&
        field.getName.isJavaConstFormat
    } yield AndroidConstant(field.getName, cls, field.getLong(null), false)

  val enums =
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


  val byName = consts.groupBy(_.name)

  val byValue = consts.groupBy(_.value)

  val byClass = consts.groupBy(_.declaringClass)

  val byNameValue = consts.groupBy(c => (c.name, c.value))

  val tokenPriority = (_: String) match {
    case "GRAVITY" | "PERSISTENT" | "CACHE" => 10
    case "TYPE" | "MODE" => 1
    case _ => 2
  }

  def findByNameValue(className: String, attrName: String, constName: String, value: Long) = {
    val minorTokens = List(className, attrName).flatMap(_.tokenize)
    val majorTokens = constName.tokenize
    val tokens = minorTokens ++ majorTokens
    val priorities = (minorTokens.map(t => t -> tokenPriority(t)) ++ majorTokens.zip(Stream from 100)).toMap
    val tokensWithCandidates = tokens map (t => t -> reverseIndex(t))

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

}
