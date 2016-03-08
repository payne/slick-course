import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class ActionInsertMultipleSpec extends FlatSpec with BeforeAndAfter with Matchers {

  val sm = new TestSecurityManager

  before {
    System.setSecurityManager(sm)
  }

  import ActionInsertMultiple.insertAlbumAction

  "ActionInsertMultiple" should "Do stuff" in {
    insertAlbumAction == insertAlbumAction
  }
}
