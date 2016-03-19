import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class ActionDeleteSpec extends FlatSpec with BeforeAndAfter with Matchers {

  val sm = new TestSecurityManager

  before {
    System.setSecurityManager(sm)
  }

  import ActionDelete.deleteAlbumByArtist

  "ActionDelete" should "Do stuff" in {
    ??? //deleteAlbumByArtist == deleteAlbumByArtist
  }
}
