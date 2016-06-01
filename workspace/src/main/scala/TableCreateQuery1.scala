import slick.driver.H2Driver.api._

object TableCreateQuery1 {

  import SharedOneTable._
  import SharedOneTable.Rating._

  /*:CODEFROM:*/
//
//  val notBadAlbumsAfter1990ByArtist = ???
//
  /*:CODETO:*/
  /*:SOLUTIONFROM:*/

  val notBadAlbumsAfter1990ByArtist = AlbumTable.filter(r => r.year > 1990 && r.rating === (NotBad: Rating)).sortBy(_.artist)

  /*:SOLUTIONTO:*/

}
