import slick.driver.H2Driver.api._

class JoinsExplicitSpec extends AbstractDBSpec {

  "JoinsExplicit" should "pass" in {
    import SharedTwoTables._
    import TestUtil._

    val methodName = "explicitJoin"
    val expectedType = "=> slick.lifted.Query[(slick.lifted.Rep[String], slick.lifted.Rep[String]),(String, String),Seq]"

    checkMethodCorrect[JoinsExplicit.type](methodName, expectedType, "a query action")

    val query = JoinsExplicit.explicitJoin.asInstanceOf[slick.lifted.Query[(slick.lifted.Rep[String], slick.lifted.Rep[String]),(String, String),Seq]]

    ensureImplemented(JoinsExplicit.explicitJoin, methodName)

    val explicitJoinCorrect = (for {
      album <- AlbumTable
      artist <- ArtistTable
      if artist.id === album.artistId
    } yield (artist.name, album.title)).sortBy(_._1)

    val failedJoin = for {
      album <- AlbumTable
      artist <- ArtistTable.sortBy(_.name)
//      if artist.id === album.artistId
    } yield (artist.name, album.title)


    withTestDatabase { db =>
      exec(db, createTablesAction)
      exec(db, insertAllAction)

      val expected = exec(db, explicitJoinCorrect.result).toList
      val expectedSet = expected.toSet
      val actual = exec(db, query.result).toList
      val actualSet = actual.toSet

      if(expected != actual) {
        if(expectedSet == actualSet)
          fail("TESTFAIL You have forgotten to sort by artist name")

        val failedJoinSet = exec(db, failedJoin.result).toSet
        if(actualSet == failedJoinSet)
          fail("TESTFAIL You did not join on artist name")

        fail("TESTFAIL Its wrong, but I cannot determine why!")
      }
    }
  }
}
