import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class ActionUpdateSpec extends FlatSpec with BeforeAndAfter with Matchers {

  val sm = new TestSecurityManager

  before {
    System.setSecurityManager(sm)
  }

  import ActionUpdate.updateToMehAfterYear

  "ActionUpdate" should "Do stuff" in {
    ??? //updateToMehAfterYear == updateToMehAfterYear
  }
}
