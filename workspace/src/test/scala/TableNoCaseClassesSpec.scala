import org.scalatest.{Matchers, BeforeAndAfter, FlatSpec}

// N.B. This has been removed from the course for now.

class TableNoCaseClassesSpec extends FlatSpec with BeforeAndAfter with Matchers {

  import TableNoCaseClasses._

  val sm = new TestSecurityManager

  before {
    System.setSecurityManager(sm)
  }

  "TableNoCaseClassesSpec" should "pass" in {

  }
}
