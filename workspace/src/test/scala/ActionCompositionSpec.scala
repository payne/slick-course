import slick.driver.H2Driver.api._

class ActionCompositionSpec extends AbstractDBSpec {

  "ActionComposition" should "pass" in {
    import SharedOneTable._
    import TestUtil._

    val artist = randomStringCreator()
    val album1Title = randomStringCreator()
    val album2Title = randomStringCreator()


    val methodName = "insertNewAlbum"
    val expectedType = "(artist: String, title: String, year: Int)slick.dbio.DBIOAction[Unit,slick.dbio.NoStream,slick.dbio.Effect.Read with slick.dbio.Effect.Write]"

    checkMethodCorrect[ActionComposition.type](methodName, expectedType, "an insert action")

    val insertMethod = (ActionComposition.insertNewAlbum _).asInstanceOf[
      (String, String, Int) => slick.dbio.DBIOAction[Unit,slick.dbio.NoStream,slick.dbio.Effect.Read with slick.dbio.Effect.Write]]

    val insertAction1 = ensureImplemented(insertMethod(artist, album1Title, 1996), methodName)
    val insertAction2 = ensureImplemented(insertMethod(artist, album2Title, 1997), methodName)

    val createTableAction = AlbumTable.schema.create

    withTestDatabase { db =>
      exec(db, createTableAction)
      exec(db, insertAction1)
      exec(db, insertAction2)

      val entries: Set[Album] = exec(db, AlbumTable.result).toSet
      assert(entries.size === 2, "TESTFAIL Album count does not match the number inserted")
      entries.find(a => a.artist == artist && a.title == album1Title) match {
        case Some(album) =>
          assert(album.rating === Rating.Awesome, "TESTFAIL First album for artist should be rated Awesome")
        case None =>
          fail("TESTFAIL First inserted album not found, did you set the artist and title correctly?")
      }

      entries.find(a => a.artist == artist && a.title == album2Title) match {
        case Some(album) =>
          assert(album.rating === Rating.Meh, "TESTFAIL Second or subsequent albums should be rated Meh")
        case None =>
          fail("TESTFAIL Second inserted album not found, did you set the artist and title correctly?")
      }
    }
  }
}
