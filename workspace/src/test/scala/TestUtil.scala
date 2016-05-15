
import org.scalatest.{Assertions, Matchers}
import slick.dbio.DBIO

import scala.concurrent.Await
import scala.reflect.runtime.universe
import scala.concurrent.duration._
import scala.util.Random

object TestUtil {

  import universe._

  def checkMethodCorrect[T](methodName: String, expectedType: String, defineType: String)(implicit tag: TypeTag[T]): Unit = {
    val actualType = tag.tpe.member(TermName(methodName)).asMethod.typeSignature.toString

    println(actualType)
    assert(expectedType == actualType, s"TESTFAIL function '$methodName' does not define $defineType")
  }

  /**
    * If this method call fails with a NotImplementedError fail with a useful error message
    * @param f The function to call.
    * @param methodName The name of the containing method
    * @tparam T The return type of the call.
    * @return The value returned by the function or an assertion failure.
    */
  def ensureImplemented[T](f: => T, methodName: String): T = {
    try {
      f
    } catch {
      case e: NotImplementedError =>
        Assertions.fail(s"TESTFAIL you have not implemented '$methodName'")
    }
  }


  def randomStringCreator() = Random.alphanumeric.take(10).mkString

  def exec[T](db: slick.driver.H2Driver.backend.DatabaseDef, action: DBIO[T]): T =
    Await.result(db.run(action), 2 seconds)

  case class ColumnData(name: String, sqlType: Int, sqlTypeString: String)

  def assertColumnValid(columns: Map[String, ColumnData], columnName: String, expectedSQLType: String): Unit = {
    columns.get(columnName) match {
      case Some(columnData) =>
        assert(columnData.sqlTypeString == expectedSQLType,s"TESTFAIL Column '$columnName' should be of sql type $expectedSQLType")
      case None =>
        Matchers.fail(s"TESTFAIL Expected column '$columnName' missing")
    }
  }

  case class CheckFieldItem(name: String, expectedType: String, isNew: Boolean)

  object CheckFieldItem {
    def apply(name: String, expectedType: Type, isNew: Boolean = false): CheckFieldItem = {
      CheckFieldItem(name, expectedType.toString, isNew)
    }
  }

  def caseClassChecker(target: universe.Type, expected: Seq[CheckFieldItem]): Unit = {
    // TODO Do we need to make these checks more aggressive.

    val methods = target.decls.filter { case d => !d.isMethod }
    // Why in the hell would m.name have a trailing space???
    val methodsByName: Map[String, universe.Symbol] = methods.map { case m => (m.name.toString.trim, m) }.toMap

    // check the existing fields
    expected.filter(!_.isNew).foreach { old =>
      methodsByName.get(old.name) match {
        case Some(sym) =>
          assert(sym.typeSignature.toString == old.expectedType, s"TESTFAIL You have changed the type of field '${old.name}' from ${old.expectedType} to ${sym.typeSignature}")
        case None =>
          Matchers.fail(s"TESTFFAIL You have removed the provided '${old.name}' field")
      }
    }
    expected.filter(_.isNew).foreach { newField =>
      methodsByName.get(newField.name) match {
        case Some(sym) =>
          assert(sym.typeSignature.toString == newField.expectedType, s"TESTFAIL New field '${newField.name}' has incorrect type expected ${newField.expectedType} found ${sym.typeSignature}")
        case None =>
          Matchers.fail(s"TESTFAIL Required new field '${newField.name}' missing")
      }
    }
  }
}
