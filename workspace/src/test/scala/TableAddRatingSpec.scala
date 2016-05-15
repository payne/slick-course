import slick.driver.H2Driver.api._
import scala.reflect.runtime.universe.typeOf

class TableAddRatingSpec extends AbstractDBSpec {

  import TableAddRating._
  "TableAddRating" should "pass" in {

    import TestUtil._

    caseClassChecker(typeOf[Album], Seq(
      CheckFieldItem("artist", typeOf[String]),
      CheckFieldItem("title", typeOf[String]),
      CheckFieldItem("year", typeOf[scala.Int]),
      CheckFieldItem("rating", typeOf[TableAddRating.Rating], isNew = true),
      CheckFieldItem("id", typeOf[Long])
    ))

    val createTableAction =
      AlbumTable.schema.create

    withTestDatabase { db =>

      exec(db, createTableAction)

      val tables = exec(db, slick.jdbc.meta.MTable.getTables).map { table =>
        table.name.name -> table
      }.toMap

      assert(tables.size === 1, "TESTFAIL albums table is missing")
      assert(tables.contains("albums"), "TESTFAIL albums table is missing")

      val albumsTable = tables("albums")
      val columns = exec(db, albumsTable.getColumns).map { c =>
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
}
