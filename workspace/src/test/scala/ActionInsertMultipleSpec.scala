import slick.driver.H2Driver.api._
import slick.profile.FixedSqlAction

class ActionInsertMultipleSpec extends AbstractDBSpec {

  "ActionInsertMultiple" should "Do stuff" in {
    import SharedOneTable._
    import TestUtil._

    val methodName = "insertAlbumsAction"
    val expectedType = "(albums: Seq[SharedOneTable.Album])slick.profile.FixedSqlAction[Option[Int],slick.dbio.NoStream,slick.dbio.Effect.Write]"
    checkMethodCorrect[ActionInsertMultiple.type](methodName, expectedType, "an insert action")

    val insertMethod = (ActionInsertMultiple.insertAlbumsAction _).asInstanceOf[
      (Seq[SharedOneTable.Album]) => FixedSqlAction[Option[Int],slick.dbio.NoStream,slick.dbio.Effect.Write]]

    val testAlbum1 = Album(randomStringCreator(),randomStringCreator(), 1996, Rating.Awesome)
    val testAlbum2 = Album(randomStringCreator(),randomStringCreator(), 2001, Rating.Meh)

    val insertAction = ensureImplemented(insertMethod(List(testAlbum1, testAlbum2)), methodName)

    val createTableAction = AlbumTable.schema.create

    withTestDatabase { db =>
      exec(db, createTableAction)
      exec(db, insertAction)

      val rows: List[Album] = exec(db, AlbumTable.result).toList
      val dbRowsWithoutIds = rows.map(_.copy(id = 0)).toSet

      val expectedSet = Set(testAlbum1, testAlbum2)

      val actualSize = rows.size
      val expectedSize = expectedSet.size

      if(dbRowsWithoutIds != expectedSet  || actualSize != expectedSize) {
        if(expectedSize == actualSize) {
          fail("TESTFAIL Albums inserted incorrectly")
        } else {
          if(actualSize == 0)
            fail("TESTFAIL No rows inserted")
          else if(actualSize < expectedSize)
            fail("TESTFAIL Not enough rows inserted")
          else
            fail("TESTFAIL Too many rows inserted")
        }
      }
    }
  }
}
