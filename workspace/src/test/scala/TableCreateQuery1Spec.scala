import slick.driver.H2Driver.api._

class TableCreateQuery1Spec extends AbstractDBSpec {

  "TableCreateQuery1" should "pass" in {
    import SharedOneTable._
    import TableCreateQuery1._
    import TestUtil._

    val methodName = "notBadAlbumsAfter1990ByArtist"
    val expectedType = "=> slick.lifted.Query[SharedOneTable.AlbumTable,SharedOneTable.AlbumTable#TableElementType,Seq]"

    checkMethodCorrect[TableCreateQuery1.type](methodName, expectedType, "a query action")

    val query = notBadAlbumsAfter1990ByArtist.asInstanceOf[slick.lifted.Query[SharedOneTable.AlbumTable,SharedOneTable.AlbumTable#TableElementType,Seq]]

    ensureImplemented(notBadAlbumsAfter1990ByArtist, methodName)

    val createTableAction =
      AlbumTable.schema.create

    val albums = Seq(
      // outside by year
      Album("artist1", "title1",1980, Rating.Awesome),
      Album("artist2", "title2",1981, Rating.Good),
      Album("artist3", "title3",1982, Rating.NotBad),
      Album("artist4", "title4",1983, Rating.Meh),
      Album("artist5", "title5",1984, Rating.Aaargh),

      // valid years
      Album("artist6", "title6",1991, Rating.Awesome),
      Album("artist7", "title7",1992, Rating.Good),
      Album("ZZZZZZ", "title9",1992, Rating.NotBad),
      Album("artist8", "title8",1993, Rating.NotBad),
      Album("artist9", "title9",1994, Rating.NotBad),
      Album("ZZZZZZ2", "title9",1992, Rating.NotBad),
      Album("artist10", "title10",1994, Rating.Meh),
      Album("artist11", "title11",1995, Rating.Aaargh)
    )

    val insertsAction = AlbumTable ++= albums

    val correctQuery = AlbumTable.filter(r => r.year > 1990 && r.rating === (Rating.NotBad: Rating)).sortBy(_.artist)
    val correctYearsQuery = AlbumTable.filter(r => r.year > 1990)
    val correctRatingsQuery = AlbumTable.filter(r => r.rating === (Rating.NotBad: Rating))

    withTestDatabase { db =>
      exec(db, createTableAction)
      exec(db, insertsAction)

      val expectedResults = exec(db, correctQuery.result)
      val actualResults = exec(db, query.result)

      if(expectedResults != actualResults) {
        val expectedResultsSet = expectedResults.toSet
        val actualResultsSet = actualResults.toSet
        if(expectedResultsSet == actualResultsSet)
          fail("TESTFAIL You have not sorted the results by artist")

        val allEntriesSet = exec(db, AlbumTable.result).toSet
        val correctRatingsSet = exec(db, correctRatingsQuery.result).toSet
        val correctYearsSet = exec(db, correctYearsQuery.result).toSet

        if(actualResultsSet == allEntriesSet)
          assert(actualResultsSet != allEntriesSet, "TESTFAIL You have not filtered the results by year or rating")
        if(actualResultsSet == correctYearsSet)
          fail("TESTFAIL You have filtered by year, but not by Rating")
        if(actualResultsSet == correctRatingsSet)
          fail("TESTFAIL You have filtered by Rating, but not by year")

        fail("TESTFAIL Your filter does not match the expected output")
      }
    }
  }
}
