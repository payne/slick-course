import slick.driver.H2Driver.api._

object ActionComposition {

  import SharedOneTable._

//  * takes the given artist, title and year of release
//  * Inserts them into the database.
//  * Rates it "Awesome" if it is their first album
//  * Rates it "Meh" otherwise

  // used to remove unused imports
  def __hidden__ = {
    AlbumTable.countDistinct.result
  }

  /*:CODEFROM:*/
  // The exercise is not ready yet
  def insertNewAlbum(artist: String, title: String, year: Int) = {
    ???
  }
  /*:CODETO:*/

}
