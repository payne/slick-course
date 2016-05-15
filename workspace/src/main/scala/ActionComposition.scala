import slick.driver.H2Driver.api._
import scala.concurrent.ExecutionContext.Implicits.global

object ActionComposition {

  import SharedOneTable._

//  * takes the given artist, title and year of release
//  * Inserts them into the database.
//  * Rates it "Awesome" if it is their first album
//  * Rates it "Meh" otherwise

  // used to remove unused imports
  def __hidden__(): Unit = {
    AlbumTable.countDistinct.result
    println(global)
  }

  /*:CODEFROM:*/
  def insertNewAlbum(artist: String, title: String, year: Int) = {
    ???
  }
  /*:CODETO:*/
  /*:SOLUTIONFROM:*/
//  def insertNewAlbum(artist: String, title: String, year: Int) = {
//    for {
//      existing <- AlbumTable.filter { a => a.artist === artist && a.year < year }.result
//      rating = existing.length match {
//        case 0 => Rating.Awesome
//        case _ => Rating.Meh
//      }
//      _ <- AlbumTable += Album(artist, title, year, rating)
//    } yield ()
//  }
  /*:SOLUTIONTO:*/

}
