import slick.driver.H2Driver.api._

object JoinsImplicit {

  import SharedTwoTables._

  // used to remove unused imports
  def __hidden__ = {
    AlbumTable.countDistinct.result
  }

  /*:CODEFROM:*/
  val implicitJoin = {
    ???
  }
  /*:CODETO:*/
  /*:SOLUTIONFROM:*/
//  val implicitJoin = {
//    // get the joined results unsorted
//    val baseQuery = for {
//      album <- AlbumTable
//      artist <- ArtistTable
//      if artist.id === album.artistId
//    } yield (artist.name, album.title)
//    // extend the query to sort the results
//    baseQuery.sortBy(_._1)
//  }
  /*:SOLUTIONTO:*/


}
