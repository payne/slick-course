import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class ActionAddTypesSpec extends FlatSpec with BeforeAndAfter with Matchers {

  val sm = new TestSecurityManager

  before {
    System.setSecurityManager(sm)
  }

  import ActionAddTypes._

  "ActionAddTypes" should "Do stuff" in {
    ??? //deleteAlbumByArtist == deleteAlbumByArtist
  }
}
