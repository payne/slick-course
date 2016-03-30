import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import slick.driver.H2Driver.api._
import scala.reflect.runtime.universe._
import scala.concurrent.duration._
import scala.concurrent.Await

class TableAddRatingSpec extends FlatSpec with BeforeAndAfter with Matchers {

  import TableAddRating._

    val sm = new TestSecurityManager

    before {
      System.setSecurityManager(sm)
    }

  "TableAddRating" should "Have Ratings column" in {

    import TestUtil._

    caseClassChecker(typeOf[Album], Seq(
      CheckFieldItem("artist", typeOf[String]),
      CheckFieldItem("title", typeOf[String]),
      CheckFieldItem("year", typeOf[scala.Int]),
      CheckFieldItem("rating", typeOf[TableAddRating.Rating], isNew = true),
      CheckFieldItem("id", typeOf[Long])
    ))


    def exec[T](action: DBIO[T]): T =
      Await.result(db.run(action), 2 seconds)

    exec(createTableAction)

    val tables = exec(slick.jdbc.meta.MTable.getTables).map { table =>
      table.name.name -> table
    }.toMap

    assert(tables.size === 1, "TESTFAIL albums table is missing")
    assert(tables.contains("albums"), "TESTFAIL albums table is missing")

    val albumsTable = tables("albums")
    val columns = exec(albumsTable.getColumns).map { c =>
      c.name -> ColumnData(c.name, c.sqlType, c.sqlTypeName.getOrElse(""))
    }.toMap


    assert(columns.size === 5, s"TESTFAIL 5 columns expected saw ${columns.size}")
    assertColumnValid(columns, "artist", "VARCHAR")
    assertColumnValid(columns, "title", "VARCHAR")
    assertColumnValid(columns, "year", "INTEGER")
    assertColumnValid(columns, "id", "BIGINT")
    assertColumnValid(columns, "rating", "INTEGER")

  }
}
