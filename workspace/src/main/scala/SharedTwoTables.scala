
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import slick.dbio.DBIOAction
import slick.driver.H2Driver.api._

import scala.concurrent.Await

object SharedTwoTables {

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

    private def toInt(rating: Rating): Int = rating match {
      case Awesome => 5
      case Good    => 4
      case NotBad  => 3
      case Meh     => 2
      case Aaargh  => 1
    }
  }

  // Tables -------------------------------------

  case class Artist(
                     name   : String,
                     id     : Long = 0L)

  class ArtistTable(tag: Tag) extends Table[Artist](tag, "artists") {
    def name   = column[String]("name")
    def id     = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (name, id) <> (Artist.tupled, Artist.unapply)
  }

  lazy val ArtistTable = TableQuery[ArtistTable]

  case class Album(
                    artistId : Long,
                    title    : String,
                    year     : Int,
                    rating   : Rating,
                    id       : Long = 0L)

  class AlbumTable(tag: Tag) extends Table[Album](tag, "albums") {
    def artistId = column[Long]("artistId")
    def title    = column[String]("title")
    def year     = column[Int]("year")
    def rating   = column[Rating]("rating")
    def id       = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (artistId, title, year, rating, id) <> (Album.tupled, Album.unapply)
  }

  lazy val AlbumTable = TableQuery[AlbumTable]



  // Setup --------------------------------------

  val createTablesAction =
    ArtistTable.schema.create andThen
      AlbumTable.schema.create

  val dropTablesAction =
    AlbumTable.schema.drop andThen
      ArtistTable.schema.drop

  val insertAllAction: DBIOAction[Unit, NoStream, Effect.Write] =
    for {
      keyboardCatId  <- ArtistTable returning ArtistTable.map(_.id) += Artist( "Keyboard Cat"   )
      spiceGirlsId   <- ArtistTable returning ArtistTable.map(_.id) += Artist( "Spice Girls"    )
      rickAstleyId   <- ArtistTable returning ArtistTable.map(_.id) += Artist( "Rick Astley"    )
      myMatesBandId  <- ArtistTable returning ArtistTable.map(_.id) += Artist( "My Mate's Band" )
      _              <- AlbumTable ++= Seq(
        Album( keyboardCatId , "Keyboard Cat's Greatest Hits" , 2009 , Rating.Awesome ),
        Album( spiceGirlsId  , "Spice"                        , 1996 , Rating.Good    ),
        Album( spiceGirlsId  , "Forever"                      , 2000 , Rating.Meh     ),
        Album( rickAstleyId  , "Whenever You Need Somebody"   , 1987 , Rating.Awesome ),
        Album( rickAstleyId  , "Hold Me in Your Arms"         , 1988 , Rating.Good    ),
        Album( rickAstleyId  , "Free"                         , 1991 , Rating.Meh     ),
        Album( rickAstleyId  , "Body & Soul"                  , 1993 , Rating.Meh     ),
        Album( rickAstleyId  , "Keep It Turned On"            , 2001 , Rating.Meh     ),
        Album( rickAstleyId  , "Portrait"                     , 2005 , Rating.NotBad  ),
        Album( rickAstleyId  , "My Red Book"                  , 2013 , Rating.Meh     ))
    } yield ()



  // Implicit joins -----------------------------

  val implicitInnerJoin: DBIOAction[Seq[(Artist, Album)], NoStream, Effect.Read] = {
    val query = for {
      artist <- ArtistTable
      album  <- AlbumTable if artist.id === album.artistId
    } yield (artist, album)

    query.result
  }



  // Explicit joins -----------------------------

  val explicitInnerJoin: DBIOAction[Seq[(Artist, Album)], NoStream, Effect.Read] =
    ArtistTable.join(AlbumTable)
      .on { case (artist, album) => artist.id === album.artistId }
      .result



  // Database -----------------------------------

  val db = Database.forConfig("dbconfig")



  // Let's go! ----------------------------------

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2 seconds)

}
