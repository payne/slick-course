import org.scalatest.{Matchers, BeforeAndAfter, FlatSpec}

class TableNoCaseClassesSpec extends FlatSpec with BeforeAndAfter with Matchers {

  import TableNoCaseClasses._

  val sm = new TestSecurityManager

  before {
    System.setSecurityManager(sm)
  }

  "TableNoCaseClassesSpec" should "Do stuff" in {

  }
}
