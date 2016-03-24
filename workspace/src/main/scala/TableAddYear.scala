
import slick.driver.H2Driver.api._

object TableAddYear {

  /*:CODEFROM:*/
  case class Album(
                    artist : String,
                    title  : String,
                    // add year column here
                    id     : Long = 0L)

  class AlbumTable(tag: Tag) extends Table[Album](tag, "albums") {
    def artist = column[String]("artist")
    def title  = column[String]("title")
    // add year column here
    def id     = column[Long]("id", O.PrimaryKey, O.AutoInc)

    // add year to the projection
    def * = (artist, title, id) <> (Album.tupled, Album.unapply)
  }

  lazy val AlbumTable = TableQuery[AlbumTable]
  /*:CODETO:*/
}
