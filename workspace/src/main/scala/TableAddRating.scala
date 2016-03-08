
import slick.driver.H2Driver.api._

object TableAddRating {

  sealed abstract class Rating(val stars: Int)

  object Rating {

    case object Awesome extends Rating(5)
    case object Good    extends Rating(4)
    case object NotBad  extends Rating(3)
    case object Meh     extends Rating(2)
    case object Aaargh  extends Rating(1)

    implicit val columnType: BaseColumnType[Rating] =
      MappedColumnType.base[Rating, Int](Rating.toInt, Rating.fromInt)

    private def fromInt(stars: Int): Rating = stars match {
      case 5 => Awesome
      case 4 => Good
      case 3 => NotBad
      case 2 => Meh
      case 1 => Aaargh
      case _ => sys.error("Ratings only apply from 1 to 5")
    }

    private def toInt(rating: Rating): Int = rating.stars
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

  val createTableAction =
    AlbumTable.schema.create

  val insertAlbumsAction =
    AlbumTable ++= Seq(
      Album( "Keyboard Cat"  , "Keyboard Cat's Greatest Hits", 2009),
      Album( "Spice Girls"   , "Spice"                       , 1996),
      Album( "Rick Astley"   , "Whenever You Need Somebody"  , 1987),
      Album( "Manowar"       , "The Triumph of Steel"        , 1992),
      Album( "Justin Bieber" , "Believe"                     , 2013))

  val selectAlbumsAction =
    AlbumTable.result

  // Database -----------------------------------

  val db = Database.forConfig("dbconfig")

  /*:CODETO:*/

  /*:SOLUTIONFROM:*/
  // solution
//
//  case class Album(
//                    artist : String,
//                    title  : String,
//                    year   : Int,
//                    rating : Rating,
//                    id     : Long = 0L)
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
}
