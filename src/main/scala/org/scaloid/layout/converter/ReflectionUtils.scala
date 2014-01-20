package org.scaloid.layout.converter



object ReflectionUtils {
  import StringUtils._
  import scala.reflect.runtime.universe.{Type => _, _} // `type Type` is defined in package object
  import scala.util.Try

  private val layoutElemPostfixes = List("Layout", "Row")

  private val mirror = runtimeMirror(getClass.getClassLoader)

  private val sPrefix = "org.scaloid.common.S"

  def toType(pkg: String, cls: String): Option[Type] =
    toType(pkg +"."+ cls)

  def toType(fqcn: String): Option[Type] =
    Try(mirror.staticClass(fqcn).selfType).toOption


  implicit class ReflectionOps(val tpe: Type) {

    def isView = tpe <:< typeTag[android.view.View].tpe

    def isLayout = // TODO find general solution
      tpe <:< typeTag[android.view.ViewGroup].tpe &&
      layoutElemPostfixes.exists(tpe.typeConstructor.toString.endsWith)

    def isWidget = isView && ! isLayout

    def isNumeric: Boolean = tpe weak_<:< typeTag[Double].tpe

    def isIntegral: Boolean = tpe weak_<:< typeTag[Long].tpe

    def scaloidHelper: Option[Type] =
      toType(sPrefix + tpe.typeConstructor.toString.className) flatMap { sType =>
        if (sType <:< tpe) Some(sType)
        else None
      }

    def settersFor(name: String): List[MethodSymbol] =
      tpe.members.collect {
        case m if m.isMethod && m.asMethod.paramss.nonEmpty && m.name.toString == name => m.asMethod
      }.toList

    def getterFor(name: String) = {
      val g = tpe.member(newTermName(name))
      (g :: g.allOverriddenSymbols).collect {
        case m if m.isMethod && m.asMethod.paramss.isEmpty => m.asMethod
      }.headOption
    }
  }


  implicit class MethodOps(val method: MethodSymbol) {

    private def params = method.paramss.flatten

    def acceptsInt: Boolean =
      params.exists { _.typeSignature =:= typeTag[Int].tpe }

    def acceptsString: Boolean =
      params.exists { _.typeSignature =:= typeTag[String].tpe }

    def acceptsFunction: Boolean = {
      params.exists { _.typeSignature.toString contains "=>" } // TODO find better way
    }


  }
}
