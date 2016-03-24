import org.scalatest.{Matchers, BeforeAndAfter, FlatSpec}
import slick.dbio.DBIO
import slick.driver.H2Driver.api._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.reflect.runtime.universe
import scala.reflect.runtime.universe._

class TableAddYearSpec extends FlatSpec with BeforeAndAfter with Matchers {

  import TableAddYear._

  val sm = new TestSecurityManager

  before {
    System.setSecurityManager(sm)
  }

  case class CheckFieldItem(name: String, expectedType: Type, isNew: Boolean = false)

  def caseClassChecker(target: universe.Type, expected: Seq[CheckFieldItem]): Unit = {
    // TODO Do we need to make these checks more aggressive.

    val methods = target.decls.filter { case d => !d.isMethod }
    // Why in the hell would m.name have a trailing space???
    val methodsByName: Map[String, universe.Symbol] = methods.map { case m => (m.name.toString.trim, m) }.toMap

    println("METHODS BY NAME = " + methodsByName)
    // check the existing fields
    expected.filter(!_.isNew).foreach { old =>
      methodsByName.get(old.name) match {
        case Some(sym) =>
          assert(sym.typeSignature === old.expectedType, s"TESTFAIL You have changed the type of field '${old.name} from ${old.expectedType} to ${sym.typeSignature}")
        case None =>
          fail(s"TESTFFAIL You have removed the existing ${old.name}")
      }
    }
    expected.filter(_.isNew).foreach { newField =>
      methodsByName.get(newField.name) match {
        case Some(sym) =>
          assert(sym.typeSignature === newField.expectedType, s"TESTFAIL New field '${newField.name} has incorrect type expecteed ${newField.expectedType} found ${sym.typeSignature}")
        case None =>
          fail(s"TESTFFAIL Required new field ${newField.name} missing")
      }
    }
  }

  "TableAddYearSpec" should "Do stuff" in {

    val db = Database.forConfig("dbconfig")

    def exec[T](action: DBIO[T]): T =
      Await.result(db.run(action), 2 seconds)

    caseClassChecker(typeOf[Album], Seq(
      CheckFieldItem("artist", typeOf[String]),
      CheckFieldItem("title", typeOf[String]),
      CheckFieldItem("year", typeOf[Int], isNew = true),
      CheckFieldItem("id", typeOf[Long])
    ))


//    typeOf[AlbumTable].me
//    exec(createTableAction)
    db.close()
  }
}
