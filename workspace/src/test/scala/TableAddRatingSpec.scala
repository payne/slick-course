import org.scalatest.{Matchers, FlatSpec, BeforeAndAfter}
import slick.dbio.Effect.Schema
import slick.driver.H2Driver.api._
import slick.profile.FixedSqlAction
import scala.concurrent.duration._
import scala.concurrent.Await

class TableAddRatingSpec extends FlatSpec with BeforeAndAfter with Matchers {

  import TableAddRating._

    val sm = new TestSecurityManager

    before {
      System.setSecurityManager(sm)
    }

  "TableAddRating" should "Have Ratings column" in {

    def assertColumnValid(columns: Map[String, ColumnData], columnName: String, expectedSQLType: String): Unit = {
      columns.get(columnName) match {
        case Some(columnData) =>
          assert(columnData.sqlTypeString == expectedSQLType,s"TESTFAIL Column '$columnName' should be of sql type $expectedSQLType")
        case None =>
          fail(s"TESTFAIL Expected column '$columnName' missing")
      }
    }


    def exec[T](action: DBIO[T]): T =
      Await.result(db.run(action), 2 seconds)

    println("----------------------------------")
    val x: FixedSqlAction[Unit, NoStream, Schema] = createTableAction
    createTableAction.statements.foreach(println(_))
    println("----------------------------------")
    exec(createTableAction)
    exec(insertAlbumsAction)
    //exec(selectAlbumsAction).foreach(println)

//    println(Await.result(db.run(slick.jdbc.meta.MTable.getTables), Duration.Inf).toList)
    val tables = exec(slick.jdbc.meta.MTable.getTables).map { table =>
      table.name.name -> table
    }.toMap

    assert(tables.size === 1)
    assert(tables.contains("albums"))

    case class ColumnData(name: String, sqlType: Int, sqlTypeString: String)
    val albumsTable = tables("albums")
    val columns = exec(albumsTable.getColumns).map { c =>
      c.name -> ColumnData(c.name, c.sqlType, c.sqlTypeName.getOrElse(""))
    }.toMap


    assert(columns.size === 4, s"TESTFAIL 5 columns expected saw ${columns.size}")
    assertColumnValid(columns, "artist", "VARCHAR")
    assertColumnValid(columns, "title", "VARCHAR")
    assertColumnValid(columns, "year", "INTEGER")
    assertColumnValid(columns, "id", "BIGINT")
//    assertColumnValid(columns, "rating", "INTEGER")

    // TODO How to check the projection
//    columns.foreach((c: MColumn) => c.sqlType)
  }
}
