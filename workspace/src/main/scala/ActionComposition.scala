import slick.driver.H2Driver.api._

object ActionComposition {

  import SharedOneTable._

  // used to remove unused imports
  def __hidden__ = {
    AlbumTable.countDistinct.result
  }

  /*:CODEFROM:*/
  def insertNewAlbum(artist: String, title: String, year: Int) = {
    ???
  }
  /*:CODETO:*/

}