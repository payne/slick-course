import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import slick.driver.H2Driver.api._
import slick.driver.H2Driver

abstract class AbstractDBSpec extends FlatSpec with BeforeAndAfter with Matchers {

  val sm = new TestSecurityManager

  before {
    System.setSecurityManager(sm)
  }

  /**
    * Run the given code with a test database instance
    * @param f The functrion to run
    * @tparam T The return type from the user code
    * @return The result returned by the user function.
    */
  def withTestDatabase[T](f: H2Driver.backend.DatabaseDef => T): T = {
    val db: slick.driver.H2Driver.backend.DatabaseDef = Database.forConfig("dbconfig")
    try {
        f(db)
    } finally {
      db.close()
    }
  }
}
