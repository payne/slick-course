import slick.driver.H2Driver.api._

class ActionUpdateSpec extends AbstractDBSpec {

  val methodName = "updateToMehAfterYear"

  val targetYear = 2000
  "ActionUpdate" should "pass" in {
    import SharedOneTable._
    import TestUtil._

    val allAlbums : Seq[Album] = for {
      year <- 1990 until 2010
      rating <- Seq(Rating.Awesome, Rating.Good, Rating.Meh, Rating.NotBad, Rating.Aaargh)
    } yield Album(randomStringCreator(), randomStringCreator(), year, rating)


    val toUpdateAlbums = allAlbums.filter(a => a.year > 2000 && a.rating != Rating.Meh).toSet
    val toMaintainAlbums = allAlbums.toSet -- toUpdateAlbums

    val updatedAlbums = toUpdateAlbums.map(_.copy(rating = Rating.Meh))

    val expectedFullSet = toMaintainAlbums ++ updatedAlbums

    //    val expectedAlbums = testAlbums.
    val expectedType = "(year: Int)slick.driver.H2Driver.DriverAction[Int,slick.dbio.NoStream,slick.dbio.Effect.Write]"
    checkMethodCorrect[ActionUpdate.type](methodName, expectedType, "an update action")

    val updateMethod = (ActionUpdate.updateToMehAfterYear _).asInstanceOf[
      (Int) => slick.driver.H2Driver.DriverAction[Int,slick.dbio.NoStream,slick.dbio.Effect.Write]]

    val updateAction = ensureImplemented(updateMethod(targetYear), methodName)

    val createTableAction = AlbumTable.schema.create

    withTestDatabase { db =>

      exec(db, createTableAction)
      exec(db, AlbumTable ++= allAlbums)
      exec(db, updateAction)
      val records = exec(db, AlbumTable.result).map(_.copy(id = 0)).toSet

      if(records != expectedFullSet) {
        assert(records.size == expectedFullSet.size, "TESTFAIL Number of database entries changed")
        assert(toMaintainAlbums.subsetOf(records), "TESTFAIL You have modified albums that should have stayed unchanged")
        assert(records.forall(r => r.year < targetYear || r.rating == Rating.Meh), "TESTFAIL not all records were updated")
        fail("TESTFAIL The results are not whats expected, what are you doing?")
      }
    }
  }
}
