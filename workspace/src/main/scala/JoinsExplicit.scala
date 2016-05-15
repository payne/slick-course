import slick.driver.H2Driver.api._

object JoinsExplicit {

  import SharedTwoTables._


  //  * Artists who have released albums
  //  * and the albums they have released
  //  * sorted by artist name

  // used to remove unused imports
  def __hidden__ = {
    AlbumTable.countDistinct.result
  }

  /*:CODEFROM:*/
  val explicitJoin = {
    ???
  }
  /*:CODETO:*/
  /*:SOLUTIONFROM:*/
//  val explicitJoin = {
//    val baseQuery = ArtistTable.join(AlbumTable).on { case (artist, album) => artist.id === album.artistId}
//    val artistAlbumNamesQuery =
//      baseQuery.map { case (artist, album) => (artist.name, album.title)}
//    val sortedQuery = artistAlbumNamesQuery.sortBy(_._1)
//    sortedQuery
//  }
  /*:SOLUTIONTO:*/
}
