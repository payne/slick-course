import org.scalatest.{Matchers, BeforeAndAfter, FlatSpec}
import slick.driver.H2Driver.api._
import scala.reflect.runtime.universe._

class TableAddYearSpec extends FlatSpec with BeforeAndAfter with Matchers {

  import TableAddYear._

  val sm = new TestSecurityManager

  before {
    System.setSecurityManager(sm)
  }

  "TableAddYearSpec" should "Do stuff" in {
    import TestUtil._
    val db = Database.forConfig("dbconfig")

    caseClassChecker(typeOf[Album], Seq(
      CheckFieldItem("artist", typeOf[String]),
      CheckFieldItem("title", typeOf[String]),
      CheckFieldItem("year", typeOf[scala.Int], isNew = true),
      CheckFieldItem("id", typeOf[Long])
    ))

    db.close()
  }
}
