import scala.reflect.runtime.universe.typeOf

class TableAddYearSpec extends AbstractDBSpec {

  "TableAddYearSpec" should "pass" in {
    import TableAddYear._
    import TestUtil._

    caseClassChecker(typeOf[Album], Seq(
      CheckFieldItem("artist", typeOf[String]),
      CheckFieldItem("title", typeOf[String]),
      CheckFieldItem("year", typeOf[scala.Int], isNew = true),
      CheckFieldItem("id", typeOf[Long])
    ))

//    withTestDatabase { db =>
//
//    }
  }
}
