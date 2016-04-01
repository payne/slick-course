import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import scala.reflect.runtime.{universe=>ru}
import slick.driver.H2Driver.api._

class TableCreateQuery1Spec extends FlatSpec with BeforeAndAfter with Matchers {

  val sm = new TestSecurityManager

  before {
    System.setSecurityManager(sm)
  }

  "TableCreateQuery1" should "pass" in {
    import SharedOneTable._
    import TableCreateQuery1._
    import TestUtil._

    def getTypeTag[T: ru.TypeTag](obj: T) = ru.typeTag[T]

    try {
      val queryType = getTypeTag(notBadAlbumsAfter1990ByArtist).tpe

      val expectedType = "slick.lifted.Query[SharedOneTable.AlbumTable,SharedOneTable.AlbumTable#TableElementType,Seq]"
      assert(expectedType == queryType.toString, "TESTFAIL 'notBadAlbumsAfter1990ByArtist' is not a AlbumTable query")

      // force the value now we have checked the type
    } catch {
      case e: NotImplementedError =>
        fail("TESTFAIL you have not updated 'notBadAlbumsAfter1990ByArtist'")
    }
    val query = notBadAlbumsAfter1990ByArtist.asInstanceOf[slick.lifted.Query[SharedOneTable.AlbumTable,SharedOneTable.AlbumTable#TableElementType,Seq]]


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

    val db: slick.driver.H2Driver.backend.DatabaseDef = Database.forConfig("dbconfig")

    try {
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
    } finally {
      db.close()
    }
  }
}
