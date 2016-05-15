import slick.driver.H2Driver.api._

class TableCreateQuery2Spec extends AbstractDBSpec {

  "TableCreateQuery2" should "pass" in {
    import SharedOneTable._
    import TableCreateQuery2._
    import TestUtil._


    val methodName = "albumTitlesOrderedByYear"
    val expectedType = "=> slick.lifted.Query[slick.lifted.Rep[String],String,Seq]"

    checkMethodCorrect[TableCreateQuery2.type](methodName, expectedType, "a query action")

    val query = albumTitlesOrderedByYear.asInstanceOf[slick.lifted.Query[slick.lifted.Rep[java.lang.String],java.lang.String,Seq]]

    ensureImplemented(query, methodName)

    val createTableAction =
      AlbumTable.schema.create

    val albums = Seq(
      // outside by year
      Album("artist5", "title5",1984, Rating.Aaargh),
      Album("artist3", "title3",1982, Rating.NotBad),
      Album("artist1", "title1",1980, Rating.Awesome),
      Album("artist2", "title2",1981, Rating.Good),
      Album("artist4", "title4",1983, Rating.Meh)
    )

    val insertsAction = AlbumTable ++= albums

    val correctQuery = AlbumTable.sortBy(_.year).map(_.title)

    withTestDatabase { db =>
      exec(db, createTableAction)
      exec(db, insertsAction)

      val expectedResults = exec(db, correctQuery.result)
      val actualResults = exec(db, query.result)

      if(expectedResults != actualResults) {
        val expectedResultsSet = expectedResults.toSet
        val actualResultsSet = actualResults.toSet
        if(expectedResultsSet != actualResultsSet)
          fail("TESTFAIL You have added removed or filtered results")
        else
          fail("TESTFAIL You have not ordered the titles by year")
      }
    }
  }
}
