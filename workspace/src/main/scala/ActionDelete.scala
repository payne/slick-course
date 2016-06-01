import slick.driver.H2Driver.api._

object ActionDelete {

  import SharedOneTable._

  // used to remove unused imports
  def __hidden__ = {
    AlbumTable.countDistinct.result
  }

  /*:CODEFROM:*/
  def deleteAlbumByArtist(artist: String) = {
    AlbumTable
  }
  /*:CODETO:*/
  /*:SOLUTIONFROM:*/
//  def deleteAlbumByArtist(artist: String) = {
//    AlbumTable.filter(_.artist === artist).delete
//  }
  /*:SOLUTIONTO:*/

}
