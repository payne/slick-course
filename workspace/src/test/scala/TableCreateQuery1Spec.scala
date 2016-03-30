import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import scala.reflect.runtime.universe._

class TableCreateQuery1Spec extends FlatSpec with BeforeAndAfter with Matchers {

  val sm = new TestSecurityManager

  before {
    System.setSecurityManager(sm)
  }

  "TableCreateQuery1" should "Do stuff" in {
    import TableCreateQuery1._

    val x = notBadAlbumsAfter1990ByArtist
    //val y = x.getTtypeOf[x]
  }
}
