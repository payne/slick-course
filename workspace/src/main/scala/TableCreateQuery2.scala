import slick.driver.H2Driver.api._

object TableCreateQuery2 {

  import SharedOneTable._

  /*:CODEFROM:*/
//
//  val albumTitlesOrderedByYear = ???
//
  /*:CODETO:*/
  /*:SOLUTIONFROM:*/

    val albumTitlesOrderedByYear = AlbumTable.sortBy(_.year).map(_.title)

  /*:SOLUTIONTO:*/

}
