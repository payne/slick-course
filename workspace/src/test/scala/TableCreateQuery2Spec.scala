import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import scala.reflect.runtime.{universe=>ru}
import slick.driver.H2Driver.api._

class TableCreateQuery2Spec extends FlatSpec with BeforeAndAfter with Matchers {

  val sm = new TestSecurityManager

  before {
    System.setSecurityManager(sm)
  }

  "TableCreateQuery12" should "pass" in {
    import SharedOneTable._
    import TableCreateQuery2._
    import TestUtil._

    def getTypeTag[T: ru.TypeTag](obj: T) = ru.typeTag[T]

    try {
      val queryType = getTypeTag(albumTitlesOrderedByYear).tpe

      val expectedType = "slick.lifted.Query[slick.lifted.Rep[java.lang.String],java.lang.String,Seq]"
      assert(expectedType == queryType.toString, "TESTFAIL 'notBadAlbumsAfter1990ByArtist' is not a AlbumTable query")

      // force the value now we have checked the type
    } catch {
      case e: NotImplementedError =>
        fail("TESTFAIL you have not updated 'notBadAlbumsAfter1990ByArtist'")
    }
    val query = albumTitlesOrderedByYear.asInstanceOf[slick.lifted.Query[slick.lifted.Rep[java.lang.String],java.lang.String,Seq]]


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

    val db: slick.driver.H2Driver.backend.DatabaseDef = Database.forConfig("dbconfig")

    try {
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
    } finally {
      db.close()
    }
  }
}
