import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class TableCreateQuery1Spec extends FlatSpec with BeforeAndAfter with Matchers {

  val sm = new TestSecurityManager

  before {
    System.setSecurityManager(sm)
  }

  "TableCreateQuery1" should "Do stuff" in {

  }
}
