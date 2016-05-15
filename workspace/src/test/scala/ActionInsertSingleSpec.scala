import slick.driver.H2Driver.api._

class ActionInsertSingleSpec extends AbstractDBSpec {

  "ActionInsertSingle" should "pass" in {
    import SharedOneTable._
    import TestUtil._

    val testAlbum = Album(randomStringCreator(), randomStringCreator(), 1996, Rating.Awesome)

    val methodName = "insertAlbumAction"
    val expectedType = "(album: SharedOneTable.Album)slick.profile.FixedSqlAction[Int,slick.dbio.NoStream,slick.dbio.Effect.Write]"

    checkMethodCorrect[ActionInsertSingle.type](methodName, expectedType, "an insert action")

    val insertMethod = (ActionInsertSingle.insertAlbumAction _).asInstanceOf[
      (Album) => slick.profile.FixedSqlAction[Int, slick.dbio.NoStream, slick.dbio.Effect.Write]]

    val insertAction = ensureImplemented(insertMethod(testAlbum), methodName)

    val createTableAction = AlbumTable.schema.create

    withTestDatabase { db =>
      exec(db, createTableAction)
      exec(db, insertAction)

      val rows: List[Album] = exec(db, AlbumTable.result).toList
      rows match {
        case album :: Nil =>
          assert(album.copy(id = 0) == testAlbum, "TESTFAIL Inserted album does not match passed album")
        case Nil => fail("TESTFAIL No album inserted by action")
        case _ => fail("TESTFAIL Multiple rows inserted by action")
      }
    }
  }
}
