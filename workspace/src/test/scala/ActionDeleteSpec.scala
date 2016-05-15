import slick.driver.H2Driver.api._

class ActionDeleteSpec extends AbstractDBSpec {

  "ActionDelete" should "pass" in {
    import SharedOneTable._
    import TestUtil._

    val methodName = "deleteAlbumByArtist"
    val expectedType = "(artist: String)slick.driver.H2Driver.DriverAction[Int,slick.dbio.NoStream,slick.dbio.Effect.Write]"

    checkMethodCorrect[ActionDelete.type](methodName, expectedType, "a delete action")

    val deleteQuery = (ActionDelete.deleteAlbumByArtist _).asInstanceOf[
      String => slick.driver.H2Driver.DriverAction[Int,slick.dbio.NoStream,slick.dbio.Effect.Write]]

    // validate the method is actually implemented
    ensureImplemented(deleteQuery("ABCD"), methodName)

    val createTableAction =
      AlbumTable.schema.create

    val albums = Seq(
      // outside by year
      Album("artist5", "title5",1984, Rating.Aaargh),
      Album("artist3", "title3",1982, Rating.NotBad),
      Album("artist1", "title1",1980, Rating.Awesome),
      Album("artist1", "titleA",1980, Rating.Awesome),
      Album("artist2", "title2",1981, Rating.Good),
      Album("artist4", "title4",1983, Rating.Meh)
    )

    val insertsAction = AlbumTable ++= albums

    withTestDatabase { db =>
      exec(db, createTableAction)
      exec(db, insertsAction)
      val res = exec(db, deleteQuery("XXXXX"))
      assert(res === 0, "TESTFAIL deleteAlbumByArtist deletes row on unknown artist")

      val res2 = exec(db, deleteQuery("artist5"))
      assert(res2 > 0, "TESTFAIL deleteAlbumByArtist does not delete single row")

      val res3 = exec(db, deleteQuery("artist1"))
      assert(res3 > 0, "TESTFAIL deleteAlbumByArtist does not delete multiple matching rows")

      // TODO - Do we need more advanced set based tests for all of these
      // TODO Are there any missing test cases?
    }
  }
}
