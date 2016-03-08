import slick.driver.H2Driver.api._

object ActionInsertMultiple {

  import SharedOneTable._

  // used to remove unused imports
  def __hidden__ = {
    AlbumTable.countDistinct.result
  }

  /*:CODEFROM:*/
  def insertAlbumAction(albums: Seq[Album]) = {

  }
  /*:CODETO:*/
}
