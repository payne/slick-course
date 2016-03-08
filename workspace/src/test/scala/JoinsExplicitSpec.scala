import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class JoinsExplicitSpec extends FlatSpec with BeforeAndAfter with Matchers {

  val sm = new TestSecurityManager

  before {
    System.setSecurityManager(sm)
  }

  import JoinsExplicit.explicitJoin

  "JoinsExplicit" should "Do stuff" in {
    explicitJoin == explicitJoin
  }
}
