import slick.driver.H2Driver.api._

object ActionInsertMultiple {

  import SharedOneTable._

  // used to remove unused imports
  def __hidden__ = {
    AlbumTable.countDistinct.result
  }

  /*:CODEFROM:*/
  // The exercise is not ready yet
  def insertAlbumsAction(albums: Seq[Album]) = {

  }
  /*:CODETO:*/
  /*:SOLUTIONFROM:*/
//  def insertAlbumsAction(albums: Seq[Album]) = {
//    AlbumTable ++= albums
//  }
  /*:SOLUTIONTO:*/
}
