import org.scalatest.{Matchers, BeforeAndAfter, FlatSpec}

class TableAddYearSpec extends FlatSpec with BeforeAndAfter with Matchers {

  import TableAddYear._

  val sm = new TestSecurityManager

  before {
    System.setSecurityManager(sm)
  }

  "TableAddYearSpec" should "Do stuff" in {

  }
}
