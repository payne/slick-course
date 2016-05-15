import slick.driver.H2Driver.api._

class JoinsImplicitSpec extends AbstractDBSpec {

  "JoinsImplicit" should "pass" in {
    import SharedTwoTables._
    import TestUtil._

    val methodName = "implicitJoin"
    val expectedType = "=> slick.lifted.Query[(slick.lifted.Rep[String], slick.lifted.Rep[String]),(String, String),Seq]"

    checkMethodCorrect[JoinsImplicit.type](methodName, expectedType, "a query action")

    val query = JoinsImplicit.implicitJoin.asInstanceOf[slick.lifted.Query[(slick.lifted.Rep[String], slick.lifted.Rep[String]),(String, String),Seq]]

    ensureImplemented(JoinsImplicit.implicitJoin, methodName)

    val implicitJoinCorrect = (for {
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

      val expected = exec(db, implicitJoinCorrect.result).toList
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
      else {
        println("expected:")
        expected.foreach { r => println("  " + r)}
        println("actual:")
        actual.foreach { r => println("  " + r)}

      }
    }
  }

}
