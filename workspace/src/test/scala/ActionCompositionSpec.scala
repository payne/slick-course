import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class ActionCompositionSpec extends FlatSpec with BeforeAndAfter with Matchers {

  val sm = new TestSecurityManager

  before {
    System.setSecurityManager(sm)
  }

  import ActionComposition.insertNewAlbum

  "ActionDelete" should "Do stuff" in {
    ??? //insertNewAlbum == insertNewAlbum
  }
}
