import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class JoinsImplicitSpec extends FlatSpec with BeforeAndAfter with Matchers {

  val sm = new TestSecurityManager

  before {
    System.setSecurityManager(sm)
  }

  import JoinsImplicit.implicitJoin

  "JoinsImplicit" should "Do stuff" in {
    implicitJoin == implicitJoin
  }
}
