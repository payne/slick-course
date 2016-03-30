
import slick.driver.H2Driver.api._

object TableAddRating {

  sealed abstract class Rating(val stars: Int)

  object Rating {

    case object Awesome extends Rating(5)
    case object Good    extends Rating(4)
    case object NotBad  extends Rating(3)
    case object Meh     extends Rating(2)
    case object Aaargh  extends Rating(1)

    def fromInt(stars: Int): Rating = stars match {
      case 5 => Awesome
      case 4 => Good
      case 3 => NotBad
      case 2 => Meh
      case 1 => Aaargh
      case _ => sys.error("Ratings only apply from 1 to 5")
    }

    def toInt(rating: Rating): Int = rating.stars
  }

  import Rating._

  /*:CODEFROM:*/
  case class Album(
                    artist : String,
                    title  : String,
                    year   : Int,
                    id     : Long = 0L)

  class AlbumTable(tag: Tag) extends Table[Album](tag, "albums") {
    def artist = column[String]("artist")
    def title  = column[String]("title")
    def year   = column[Int]("year")
    def id     = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (artist, title, year, id) <> (Album.tupled, Album.unapply)
  }


  lazy val AlbumTable = TableQuery[AlbumTable]
  /*:CODETO:*/

  /*:SOLUTIONFROM:*/
//  case class Album(
//                    artist : String,
//                    title  : String,
//                    year   : Int,
//                    rating : Rating,
//                    id     : Long = 0L)
//
//  implicit val columnType: BaseColumnType[Rating] =
//    MappedColumnType.base[Rating, Int](Rating.toInt, Rating.fromInt)
//
//  class AlbumTable(tag: Tag) extends Table[Album](tag, "albums") {
//    def artist = column[String]("artist")
//    def title  = column[String]("title")
//    def year   = column[Int]("year")
//    def rating = column[Rating]("rating")
//    def id     = column[Long]("id", O.PrimaryKey, O.AutoInc)
//
//    def * = (artist, title, year, rating, id) <> (Album.tupled, Album.unapply)
//  }
//
//  lazy val AlbumTable = TableQuery[AlbumTable]
  /*:SOLUTIONTO:*/

  // Database -----------------------------------

  val db = Database.forConfig("dbconfig")

  val createTableAction =
    AlbumTable.schema.create
}
