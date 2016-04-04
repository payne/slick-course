import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import scala.reflect.runtime.{universe => ru}
import slick.driver.H2Driver.api._

import scala.util.Random


class ActionInsertSingleSpec extends FlatSpec with BeforeAndAfter with Matchers {

  val sm = new TestSecurityManager

  before {
    System.setSecurityManager(sm)
  }


  "ActionInsertSingle" should "Do stuff" in {
    import SharedOneTable._
    import TestUtil._

    def randomStringCreator() = Random.alphanumeric.take(10).mkString
    val testAlbum = Album(randomStringCreator(),randomStringCreator(), 1996, Rating.Awesome)

    val testAction = try {
      import ru._
      val actualType = ru.typeOf[ActionInsertSingle.type].member("insertAlbumAction": TermName).asMethod.typeSignature.toString

      val expectedType = "(album: SharedOneTable.Album)slick.profile.FixedSqlAction[Int,slick.dbio.NoStream,slick.dbio.Effect.Write]"
      assert(expectedType == actualType, "TESTFAIL function 'insertAlbumAction' does not define an insert action")

      // case needed incase user changes type
      val insertAction = (ActionInsertSingle.insertAlbumAction _).asInstanceOf[(Album) => slick.profile.FixedSqlAction[Int, slick.dbio.NoStream, slick.dbio.Effect.Write]]
      insertAction(testAlbum)
    } catch {
      case e: NotImplementedError =>
        fail("TESTFAIL you have not updated 'insertAlbumAction'")
    }

    val createTableAction = AlbumTable.schema.create
    val db: slick.driver.H2Driver.backend.DatabaseDef = Database.forConfig("dbconfig")

    try {

      exec(db, createTableAction)
        exec(db, testAction)

        val rows: List[Album] = exec(db, AlbumTable.result).toList
        rows match {
          case album :: Nil =>
            assert(album.copy(id = 0) == testAlbum, "TESTFAIL Inserted album does not match passed album")
          case Nil => fail("TESTFAIL No album inserted by action")
          case _ => fail("TESTFAIL Multiple rows inserted by action")
        }
    } finally {
      db.close()
    }
  }
}
