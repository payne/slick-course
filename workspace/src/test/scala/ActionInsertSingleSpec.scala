import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class ActionInsertSingleSpec extends FlatSpec with BeforeAndAfter with Matchers {

  val sm = new TestSecurityManager

  before {
    System.setSecurityManager(sm)
  }

  import ActionInsertSingle.insertAlbumAction

  "ActionInsertSingle" should "Do stuff" in {
    ??? //insertAlbumAction == insertAlbumAction
  }
}
