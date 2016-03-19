
## 0 # Welcome to the Essential Slick Course

*N.b. This course is under development, the text and exercises may be incomplete*

Welcome to the essential slick course.

This course assumes a working knowledge of Scala and at basic database concept.

The course aims to give you working knowledge of the key concepts of Slick by both discussing them and having exercises
 to test your knowledge.

If you want to talk to us about it the course either email Rory (rory@scalanator.io) or come and chat to use on the
 [Gitter channel](https://gitter.im/ScalanatorIO/slick-course) channel.

You can find the full course content and code in the [Github repository]

This course is based upon the Slick workshop from [Scala Exchange 2015].

Copyright 2016 [Dave Gurnell] of [Underscore].

Both the original workshop contents and this course contents licensed under [CC-BY-NC-SA 4.0],

Original workshop code and this course code licensed [Apache 2.0].

Click next to move onto the first lesson.

[Essential Slick]: http://underscore.io/books/essential-slick
[Scala Exchange 2015]: http://scala.exchange
[Dave Gurnell]: http://davegurnell.com
[Underscore]: http://underscore.io
[CC-BY-NC-SA 4.0]: http://creativecommons.org/licenses/by-nc-sa/4.0/
[Apache 2.0]: http://www.apache.org/licenses/LICENSE-2.0
[Underscore newsletter]: http://underscore.io/newsletter.html
[Github repository]: https://github.com/ScalanatorIO/slick-course
[Gitter channel]: https://gitter.im/ScalanatorIO/slick-course

## 100 # What is Slick?

* Slick 3 is the industry standard library for accessing relational databases in Scala
* Unlike a lot of JVM database layers (such as Hibernate), it is *not* an ORM (Object Relational Mapper) 
* Allows explicit communication with the database:
    * Define datatypes
    * Map them onto tables
    * Build queries
    * Issue queries and parse results
* Unlike traditional JDBC drivers it is:
    * Functional
    * Typesafe
    * Queries built using a Scala DSL
    * Fully Asynchronous

## 200 # What are are going to cover

We will cover 5 key concepts

* Tables - Defining relationships between Scala datatypes and the database.
* Queries - A DSL for building SQL 
* Actions - Allow us to sequence queries together and send them to the database
* Joins - Allowing us to build queries that pull data from multiple sources
* Profiles/Drivers - Slicks way of representing different databases and their capabilities.

## 300 # A quick note on imports

To use Slick you will need to import some things:

```scala
import scala.concurrent._
import scala.concurrent.duration._
import slick.driver.H2Driver.api._
```

The ```scala.concurrent``` imports are to import standard Scala Futures.

All of the Slick specific code comes from a single import 

```scala 
import slick.driver.H2Driver.api._
```

As you can see the import contains the specific version of Slick for the H2 database.  This allows the Slick api to
capture the differences in capabilities between different database types.  Most features are common but there are
some specific features available only in certain databases.

When you are using this in your own project you would import the version specific for your database (e.g. MySQL,
PostgreSQL etc). This will be covered in more detail in the Profiles section later.

## 350 # Tables - The Data

Let's start by defining our data.  Here is a case class representing an Album:

```scala
  case class Album(
    artist : String,
    title  : String,
    id     : Long = 0L)
```

The only difference compared to a standard in-memory case class is we have specified an ```id``` field.  It will be
our primary key within the database.  We have given it a default value so we do not need to provide one when
creating Albums.

## 400 # Tables - Mapping

Next we need to define the database table mapping for the Album class:
```scala
  class AlbumTable(tag: Tag) extends Table[Album](tag, "albums") {
    def artist = column[String]("artist")
    def title  = column[String]("title")
    def id     = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def * = (artist, title, id) <> (Album.tupled, Album.unapply)
  }
```
`We define a class that extends the stand Slick ```Table``` class representing an SQL table for storing instances of
Album.  There are a few key points:

* We define ```Table[Album]``` meaning this table is going to store Albums. 
* The name ```"album"``` defines the name of the table within the database.
* We define each column, ```artist``` and ```title``` both define columns that store a ```String``` with a column name that matches the name.
* The mapping for the ```id``` column has two extra flags
    * ```O.PrimaryKey``` - This value represents a unique identifier for a row in the database
    * ```O.AutoInc``` - Means that Slick will use the database to provide the value rather than us.

We will discuss the ```*``` method in the next lesson.

## 450 # Tables - The Star Method

```scala
   def * = (artist, title, id) <> (Album.tupled, Album.unapply)
```

The ```*``` method (known as the 'default projection') for a table tells Slick how to convert between the case class
and the table columns.

The left side is a tuple of columns ```(artist, title, id)``` representing the three columns we want to read
whenever we select data from the database and that we want to write whenever we insert data into the database.

The ```<>``` (diamond) operator is a method which is implicitly added onto tuple (a convenience method) that takes two
function arguments:

* ```Album.tupled``` - A function that converts from the case class to a tuple of three elements (artist, title and id)
* ```Album.unapply``` - A function that converts from a tuple into an instance of Album.

So now we have told Slick about the table name, table structure and how to read and write the ```Album``` case class
into database format.

## 475 # Tables - The Table Query

The last part we need to define in the table query.

It is defined like this:
```scala
lazy val AlbumTable = TableQuery[AlbumTable]
```

It is a value that is instance of ```TableQuery``` (not our AlbumTable class).  This object provides a root object for
creating queries on our table.


## 500 # Tables - Summary

We have created three things to allow storing of our data to a database table.

* A case class ```Album``` representing our table
* A mapping class ```AlbumTable``` which contains:
    * The table name (in the constructor)
    * The table row definitions
    * The ```*``` function for mapping between database columns and case class fields
* A Table Query (val ```AlbumTable```) which forms the root of any queries made against the table.

The code and naming above is a standard Slick convention and all examples and exercises within the course will use them.
 The various options available in table setup are specified in the Slick documentation.


## 600 # Tables - A few extras

So now we have defined a table and all of So before we start writing code, lets briefly review a few more items we
need to actually use Slick.  These are:

* Actions
* The database handle

## 625 # Actions Briefly

Actions represent commands we want to issue to the database.
We will look at three types:

* Create the table
* Insert data
* Select data

Remember Actions are a definition of the work to be performed and are executed
separately from being defined (we will see that later).

## 630 # Actions Briefly - Creating the table

An Action to create the Albums table:

```scala
  val createTableAction = AlbumTable.schema.create
```

## 635 # Actions Briefly - Inserting data

Next we an Action to insert some test data into our table

```scala
  val insertAlbumsAction =
    AlbumTable ++= Seq(
      Album( "Keyboard Cat"  , "Keyboard Cat's Greatest Hits"  ), // released in 2009
      Album( "Spice Girls"   , "Spice"                         ), // released in 1996
      Album( "Rick Astley"   , "Whenever You Need Somebody"    ), // released in 1987
      Album( "Manowar"       , "The Triumph of Steel"          ), // released in 1992
      Album( "Justin Bieber" , "Believe"                       )) // released in 2013
```

## 640 # Actions Briefly - Selecting data

The last Action we are going to look at now is select.  This action is the equivalent of
```SELECT * FROM albums``` in SQL

```scala
  val selectAlbumsAction = AlbumTable.result
```

That is it for Actions right now, we will visit them again later in the tutorial.

We have one last thing to talk about and then it is your turn.

## 645 # The Database handle

To actually perform operations on a database we need a handle to the database.

```scala
  val db = Database.forConfig("dbconfig")
```

Which creates a database handle that implements a ```run``` method for exercising Actions against the database.
The above code uses a helper method to create a handle using a configuration defined in  Typesafe config.  The
 configuration lives in our ```application.conf```:

```
dbconfig = {
  connectionPool      = disabled
  url                 = "jdbc:h2:mem:db"
  driver              = "org.h2.Driver"
  keepAliveConnection = true
}
```

This creates in in-memory H2 database instance.
An in-memory database is great for testing, but must be recreated each time your application starts.  In production you
would point at a standard database instance elsewhere.


## 650 # Executing queries

Now we have a database handle we can execute Actions against the database:

```scala
  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2 seconds)

  exec(createTableAction)
  exec(insertAlbumsAction)
  exec(selectAlbumsAction).foreach(println)
```


The ```run``` take an action and return a future (they are executed asynchronously).
So for the above example we have created a helper method ```exec``` to run the action and wait for the results.
In normal you are able to handle the results asynchronously as well.

## 675 # Tables - Your turn

The first exercise involves modifying the Album definition we have seen above.

In the editor below add:

* A ```year``` column to Album to type ```Int``` (add it before the ```id``` column)
* A ```year``` column to the database
* A ```year``` to the projection (```*``` method)

@:editor file=TableAddYear.scala

## 700 # Tables Continued - Custom Table Types

One common misconception about Slick is that it can only store tuples of primitive values and case classes.

```scala
    def * = (artist, title, id) <> (Album.tupled, Album.unapply)
```

The default project we have already seen (shown above) uses the convenience methods provided by the case class to
perform the mapping (using ```tupled``` and ```unappy```).  However we can replace these methods with any methods
we want to store any datatype.  To demonstrate this we are going to make ```Album``` a normal class instead of a case
class.

## 710 # Custom Table Types Exercise

* Remove the case class keyword from Album
* Create ```defineAlbum``` and ```extractAlbum``` methods
* Update the projection method to use the methods

@:editor file=TableNoCaseClasses.scala

## 720 # Custom Table Types Exercise review

The exercise demonstrated that you map data from an arbitrary class but at the cost of extra boilerplate code.
Effective we are re-implementing the case class ```tupled``` and ```unapply``` method.

## 750 # Custom Column Types

Something more useful in our day to day coding is how to store a custom type into the database.
For this example we are going to define a Rating class (a nice typesafe enumeration):

```scala
sealed abstract class Rating(val stars: Int)

object Rating {

  case object Awesome extends Rating(5)
  case object Good    extends Rating(4)
  case object NotBad  extends Rating(3)
  case object Meh     extends Rating(2)
  case object Aaargh  extends Rating(1)

  // columnType and to/FromInt methods will go here
}
```

## 760 # Column Type

To defined the mapping we need a ColumnType to provide the mapping between the type and is corresponding
 database type.  To do this we create a type class that maps the Rating class into something Slick can
 already understand and store in a column.  In this case we are going to use an ```Int```.

```scala
  implicit val columnType: BaseColumnType[Rating] =
    MappedColumnType.base[Rating, Int](Rating.toInt, Rating.fromInt)
```

We are using a helper method ```MappedColumnType.base``` to create a type class mapping ```Rating``` to ```Int```.
To use this method we must provide two methods (called ```toInt``` and ```fromInt```) which covert to and from the
database type respectively.

## 770 # Column Types - Conversion Functions

So lets show these two functions, there are no real surprises here:

```scala
  def fromInt(stars: Int): Rating = stars match {
    case 5 => Awesome
    case 4 => Good
    case 3 => NotBad
    case 2 => Meh
    case 1 => Aaargh
    case _ => sys.error("Ratings only apply from 1 to 5")
  }

  def toInt(rating: Rating): Int = rating.stars
```

When imported into the code using it, the implicit type class provides all the information that Slick
needs to manage the database conversions of ```Rating```.

## 780 # Column Types - Where the type class lives

One note about the location of the type class in this example.
For simplicity within our examples we have put the implicit type class and helper methods directly into rating.
You are not obliged to do this and it keeps your model classes simpler if you separate data (the case class) from the
helper methods separate.  The type class implicit simply needs to be in scope for the table definition.

## 790 # Column Types - Your turn

In this exercise we want you to add a ```rating``` of type ```Rating``` to the case class and then add them to the
table column mappings and default projection.  The ```Rating``` class has already been included in scope.

@:editor file=TableAddRating.scala

## 800 # Select Queries

We have covered all the basics of creating tables and mappings.  So now we will move onto queries, specifically SELECT
queries.

Selecting data is 90% of the work we are going to be doing with databases.

### Queries vs Actions

We briefly mentioned Actions earlier - an Action is task to be run on the database with

```scala
db.run(myAction)
```

Queries are simply a type of action:

```scala
AlbumTable.filter(_.artist === "Spice girls")
.result
```

Above is an example Action for selecting all Albums by the Spice Girls, let us dissect it.  The first line creates a
 query on the ```AlbumTable``` table query object.  ```AlbumTable``` represents the query ```select * from albums```.
 Query objects have methods on them for adding clauses to an SQL statement.  The filter method adds a WHERE clause so
 we are filtering down the results to only select albums by the Spice Girls.

The ```.result``` call turns the query into an Action which allows it to be passed to ```db.run``` to be executed.
So why do we have a difference between a query and an action?  Queries have methods for assembling SQL, adding where
clauses, adding ordering clauses etc.   Actions have a similar set of methods but for doing sequencing operations on
queries.  For example 'run this query after that query', chain this queries together etc.

Both queries and actions are monadic so a lot of the method names are the same, ```flatmap```,```map```, ```filter```
and so on, but the meaning of the methods are quite different leading to us having two distinct data types.

So we will always start by building a query and then use some kind of method (in this case ```result``` to turn that
into an action to do a particular type of operation.

## 900 # Select Queries - Combinators

So next we will look at the available combinators on query that allow us to build up SQL.

The simplest query is the ```AlbumTable``` singleton value.

```scala
  val selectAllQuery =
    AlbumTable
```

This represents the SQL query
```sql
SELECT * FROM albums;
```

So when we run this query we get back everything.  Applying combinators modifies the SQL which transforms the
results we are going to get back.


## 910 # Select Queries - Filter

To restrict the results we get back from the database we can use the ```filter``` method.  In this example
we are filtering by the ```artist```
```scala
val selectWhereQuery =
  AlbumTable.filter(_.artist === “Spice Girls”)
```

This is the equivalent SQL:

```sql
SELECT *
FROM albums
WHERE artist = ‘Spice Girls’;
```

## 920 # Select Queries - Filters

An important thing to here in:
```scala
AlbumTable.filter(_.artist === “Spice Girls”)
```

Is that the ```_``` does not represent an `Album` but an ```AlbumTable``` (i.e. the class we defined with the mappings).
This is because we are building a query to run against the database.  we are using ```===``` rather than ```==```.
```==``` in Scala is reserved for comparing two values, here we are not doing that, we are building an SQL expression.

Most operations are the same as the are in Scala ```<```,```>```, ```&&``` except for two:

* ```===``` replaces ```==```
* ```=!=``` replaces ```!=```

It is very similar to filtering a standard list in Scala, but we must keep in mind we are actually building an SQL
expression to filter on.

## 930 # Select Queries - Sorting

A similar principle for filtering goes for sorting. We use the ```sortBy``` method by providing a function to transform
our table into an ascending or descending order clause.  So you can take any column and call ```.asc``` or ```.desc```


```scala
// Sort results
val selectSortedQuery1 =
  AlbumTable.sortBy(_.year.asc)
```

This is the equivalent SQL:

```sql
SELECT *
FROM albums
ORDER BY year ASC;
```

## 940 # Select Queries - Sorting by multiple columns

We can sort by multiple columns by supplying a tuple of the things we want to sort by

```scala
  // Sort results
  val selectSortedQuery2 =
    AlbumTable
      .sortBy(a => (a.year.asc, a.rating.asc))
```

This is the equivalent SQL:

```sql
SELECT *
FROM albums
ORDER BY year ASC, rating ASC;
```

## 950 # Select Queries - Paging results

We can page our results using ```OFFSET``` and ```LIMIT``` clauses simply by using ```drop()``` and ```take()```:

```scala
  // Page results
  val selectPagedQuery =
    AlbumTable
      .drop(2).take(1)
```

This is the equivalent SQL:

```sql
SELECT *
FROM albums
OFFSET 2 LIMIT 1;
```

## 960 # Select Queries - Selecting individual columns

We can select individual columns in our results with a project query using ```map``` so:

```scala
  // project results
  val selectColumnsQuery1 =
    AlbumTable
      .map(_.title)
```

The map function is given the Album table as a parameter and we return the column or an expression involving the column
that we want to select from the database.

This is equivalent of the below SQL:

```sql
SELECT title
FROM albums;
```

## 970 # Select Queries - Selecting two columns

If we want to select two columns we use ```map``` and return a tuple of them:

```scala
  // Project multiple columns
  val selectColumnsQuery2 =
    AlbumTable
      .map(a => (a.artist, a.title))
```

Generates:

```sql
SELECT artist, title
FROM albums;
```

Slick will modify the query to only return the two columns requested.  It will return the expected type, in this case a
 tuple of ```String```,```String``` rather than a complete album.

## 980 # Select Queries - Combining queries

Because each of these combinators returns a new query we can chain them all together so we can chain ```filter``` with
```map``` to select the titles of all Albums by the artist "Keyboard Cat"

```scala
val selectCombinedQuery =
AlbumTable
  .filter(_.artist === "Keyboard Cat")
  .map(_.title)
```

```sql
SELECT title
FROM albums
WHERE artist = 'Keyboard Cat';
```

## 990 # Select Queries - Your turn 1

Update the below query to select all albums release after 1990 with a ```Rating``` of ```NotBad``` or higher
sorted by ```artist```.  This will require two filters and a sortBy

*N.B.* In you query you will have to cast ```NotBad``` to a ```Rating``` using ```(NotBad: Rating)``` as your parameter.
 This is because the type inference cannot work out the lift from NotBad to ```Rep[Rating]``` and you need to give the
 compiler a hint.

@:editor file=TableCreateQuery1.scala

## 995 # Select Queries - Your turn 2

Update the below query to select the titles of all albums in ascending year order.  This will require
a ```map``` and a ```sortBy```

*N.B* The query ordering is important, ```map``` changes the type from ```Album``` to ```String```

@:editor file=TableCreateQuery2.scala


## 1000 # Select Queries - Types

If we look ```Query``` we can see it has 3 type parameters:

```scala
Query[P, U, C]
```

These are:

* *P* - "Packed"" / query type
* *U* - "Unpacked" / result type
* *C* - Collection type

### The Packed parameter
This is the type passed to us whenever we provide a function for filtering or sorting.

### The Unpacked parameter
This represents the type of the results we get back when we run our query.

### The Collection type
This represents the collection type of the results we get back.

The base query type on the Album table is of the type:

```scala
Query[AlbumTable, Album, Seq]
```

So whenever we filter over this query we are passed an ```AlbumTable``` and we get to chose which columns we filter by.
The results are all going to be ```Album```s and the result collection will be a sequence.

## 1010 # Select Queries - Types in action 1


Lets see how Slick keeps these three in sync as we manipulate this query.  We can then uncover the problem we had earlier
in the exercises with the sequencing of ```map``` and ```sortBy```.

So starting with our regular ```AlbumTable```:

```scala
val q0: Query[AlbumTable, Album, Seq] = AlbumTable
```
This has ```AlbumTable```, ```Album``` and ```Seq``` as our three type parameters.

```scala
val q1: Query[AlbumTable, Album, Seq] = q0.filter(_year === 1987)
```
When we filter the argument to the filter function is our first type parameter (in this case ```AlbumTable```) which
gives us access to the ```year``` column.  The result of filtering (if you think about filtering over a normal sequence)
is the same that we started with.  Filtering does not change the type parameters, we will simply have fewer results.

## 1020 # Select Queries - Types in action 2

Given:

```scala
val q1: Query[AlbumTable, Album, Seq] = q0.filter(_year === 1987)
```

Remember when we map on a normal functor we change the type parameter on our collection.

Let's see what happens when we ```map```:

```scala
val q2: Query[Rep[String], Album, Seq] = q1.map(_.title)
```

Notice that the type of the P column has taken the type of the column ```Rep[String]```, and the result type to ```String```.
Slick always keeps these two columns in sync with each other.

To map again we would have to provide something that takes a ```Rep[String]``` rather than ```AlbumTable``` so we can
no longer choose a different column.

## 1030 # Select Queries - So what is Rep\[T\]?

```Rep[T]``` is 'an SQL expression of type ```T```

You will see Reps all over the place. In this query:

```scala
val q =
 AlbumTable
 .filter(t => t.artist === “Keyboard Cat”)
 .map(t => t.id)
```

There are 4 Reps in this code:

* ```t.artist```, the artist column on the table - ```Rep[String]```
* ```t.id```, the unique row id, is a ```Rep[Long]```
* ```“Keyboard Cat”```, although we provide a string literal it is implicitly converted to a ```Rep[String]```
* ```===``` by comparing the ```t.artist``` to ```"Keyboard Cat"``` we are building a bigger ```Rep``` ```Rep[Boolean]``` representing the comparison.

A lot of the Slick methods deal with ```Rep``` types and whenever you see the ```Rep``` type in an error message you are looking at
an expression that calculates at a database level a value of that type.


## 2000 # Action Stations!

Slick Actions are anything that can be executed on a database.

This includes:

* Query actions - Select
* Structural actions - Create/Drop tables
* Modify actions - Insert, Update, Delete

We have already explored query actions in the previous section, now we will cover the remainder.

## 2010 # Actions - Create action

The create action is relatively simply from the Scala side, given the ```AlbumTable``` definition, execute the
appropriate statements to create the corresponding database table.

```scala
val createTableAction =
  AlbumTable.schema.create
```

Produces:

```sql
CREATE TABLE albums (
  artist TEXT, title TEXT,
  year INTEGER, rating INTEGER,
  ...);
```

Note that the exact syntax used depends on the target database (H2, Oracle etc.).  There is a lot of variance in the
exact syntax between the databases.  Slick hides this complexity from you behind a sane facade.

## 2020 # Actions - Drop action

Dropping a database table is as easy as create:

```scala
val dropTableAction =
 AlbumTable.schema.drop
```

produces the expected:

```sql
DROP TABLE albums;
```

## 2030 # Actions - Insert action

To insert a row into the database Slick provides a ```+=``` method on the table class:

```scala
val insertAction =
 AlbumTable += Album(
   “Pink Floyd”,
   “Dark Side of the Moon”,
   1973,
   Rating.Awesome)
```

Which produces the expected SQL:

```sql
INSERT INTO albums
 (artist, title, year, rating)
VALUES (“Pink Floyd”,
 “Dark Side of the Moon”, 1973, 5);
```

Note that Slick will take care of populating autoincrement columns for you.

## 2031 # Actions - Insert - Your turn

Given ```AlbumTable``` complete the function to insert the given ```Album``` into the database.

@:editor file=ActionInsertSingle.scala

## 2035 # Actions - Insert multiple

We can also insert multiple rows at the same time with ```++=```:

```scala
val insertAction2 =
 AlbumTable ++= Seq(
   album1,
   album2,
   album3)
```

Giving:

```sql
INSERT INTO albums
(artist, title, year, rating)
VALUES (...), (...), (...);
```

## 2036 # Actions - Insert multiple - Your turn

Given ```AlbumTable``` complete the function to insert the sequence of ```Album```s into the database.

@:editor file=ActionInsertMultiple.scala


## 2040 # Actions - Delete action

Deleting rows from the database is very similar to a normal select query, except we add ```.delete``` to turn it
into a delete action:

```scala
val deleteAction =
 AlbumTable
 .filter(_.artist === “Keyboard Cat”)
 .delete
```

Gives:

```sql
DELETE
FROM albums
WHERE artist = “Keyboard Cat”;
```

## 2045 # Actions - delete - your turn

Update the function to return an action to delete all the albums specified by the caller:

@:editor file=ActionDelete.scala


## 2050 # Actions - Update action

Updating records in the database is a little more complex.  We start with a filter to find the correct rows, then map
to the fields we wish to update (in this example ```title```) and then call ```update``` with the new value:

```scala
val updateAction =
  AlbumTable
    .filter(_.artist === “Keyboard Cat”)
    .map(_.title)
    .update(“Even Greater Hits”)
```

Gives:

```sql
UPDATE albums
SET title = “Even Greater Hits”
WHERE artist = “Keyboard Cat”;
```

## 2060 # Actions - Update action 2

To update more than one column simultaneously we simply tuple the fields we wish to update:

```scala
val updateAction2 =
  AlbumTable
    .filter(_.artist === “Keyboard Cat”)
    .map(a => (a.title, a.year))
    .update((“Even Greater Hits”, 2010))
```

Giving:

```sql
UPDATE albums
SET title = “Even Greater Hits”,
 year = 2010
WHERE artist = “Keyboard Cat”;
```

## 2080 # Actions - Update - your turn

Update albums released after a specified year
set their rating to “Meh”

@:editor file=ActionUpdate.scala

## 2100 # Actions - Types

Lets look at the type of these actions and how they work.  Actions fall into a big hierarchy of types most of which
we do not need to know about.  But the root type is:

```
DBIOAction[R, S, E]
```

All actions are a subtype of ```DBIOAction``` - which has three type parameters:

* *R* - Result type - the type of results we get back when we run the action
* *S* - Streaming or not streaming (more on this shortly)
* *E* - Effect - What kind of effect the action has on the database (read, write etc.)

## 2110 # Actions - Types 2

For example this action:

```scala
DBIOAction[Seq[Album], NoStream, Effect.All]
```

* Result type is a sequence of ```Album```s
* It is not a streaming action
* It can have all types of effects on the database.

## 2120 # Actions - Streaming

At the start of the course we mentioned that Slick supports streaming of results.  This allows us to
handle data in a reactive way.  We can pull back results one-by-one as we are ready for them without loading
them all into memory.  This means we can perform operations on huge databases without risk of crashing the application.

All queries can be used in a non-streaming capacity.  But certain queries can be used to either pull back lists of
results or to pull back streams.  This short course does not cover streams, so we are only going to worry about the
phantom type ```NoStream``` but there is also a type parameter ```Streaming``` which has type parameters telling us
what kind of values we are going to get back in our stream.

## 2125 # Actions - Effect type

The effect type allows Slick to do some reasoning about what actions can be used in what contexts.  There are various
different types of effect, all of them phantom types.  We never create instances of phantom types, they are used to
give additional information to the compiler to prevent errors.

We can see the definition of Effect here:

```scala
trait Effect

object Effect {
  trait Read extends Effect
  trait Write extends Effect
  trait Schema extends Effect
  trait Transactional extends Effect
  trait All extends Read with Write with Schema with Transactional
}
```

We generally do not use these ourselves but they allow Slick to reason about when we are allowed to perform a query
and the different conditions in which we are allowed to sequence queries together.


## 2130 # Actions - Effect type

Most of the time we are going to ignore the streaming and effect parameters and just concentrate on the result.
There is a shorthand version of writing:

```scala
DBIOAction[Seq[Album], NoStream, Effect.All]
```

which is:

```scala
DBIO[Seq[Album]]
```

```DBIO``` is just a convenience, it is a type alias for ```DBIOAction``` with no stream or effect tracking.
It can be useful as it saves typing.

## 2140 # Actions - Running

When we run action (whether it is a ```DBIOAction``` or ```DBIO```) we can execute (or run) the action.

So
```scala
myAction: DBIO[Seq[Album]]
```

can be called with

```scala
db.run(myAction)
```

Gives a result of the type:
```scala
Future[Seq[Album]]
```

So even though we are ignoring the streaming and effect type parameters the result type parameter is incredibly
important.

## 2150 # Actions - One last type

Whilst you may never use this directly but it is useful to know that it exists.

```scala
SqlAction[R, S, E]
```

```SqlAction``` is a subtype of ```DBIOAction``` and takes the same type parameters.

It is interesting because it provides a ```.statements``` method which returns a ```Seq[String]``` representing all
of the prepared statements that we are about to issue to the database when we run this action.

Unfortunately not all actions are ```SQLAction```s so we cannot always call this ```statements``` method.  But,
especially for primitive non-composed actions we can call this to get some quick debugging output.

## 2160 # Actions - Statements example

For example given this action:
```scala
val createTableAction = AlbumTable.schema.create
```

Has the type
```scala
slick.driver.H2Driver.DriverAction[Unit, NoStream, slick.dbio.Effect.Schema]
```

and calling ```createTableAction.statements``` gives:

```
create table "albums" (
  "artist" VARCHAR NOT NULL,
  "title" VARCHAR NOT NULL,
  "year" INTEGER NOT NULL,
  "id" BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY)
```

Note that the exact SQL is dependent on the database type.

## 2170 # Actions - Statement types exercise

Add type annotations to the below:

@:editor file=ActionAddTypes.scala

## 2200 # Actions - Combinators

One last thing we need to cover for actions is combinators.  Back when we were looking at queries we stated that
the combinators or queries such as ```filter``` and ```map``` added clauses to a query.  Once you turned it into
an action by calling ```.result``` the same methods had different meanings.  The meaning within an action context all
us to chain actions together.  So we can build one big action consisting of a set of smaller steps.

## 2210 # Actions Combinators example

Here is a standard kind of scenario:

```scala
// Sequencing independent actions
def runManyActions() = {
  exec(action1)
  exec(action2)
  exec(action3)
}
runManyActions()
```

We want to run three actions in sequence.  We have seen some of these in the test setup from our examples.
Create a database, populate it, and then query it returning the result.  ```runManyActions``` in this case achieves
this goal.

## 2220 # Actions Combinators example

Rather than running this as 3 actions, we can run it as one big action:

```scala
// Sequencing independent actions
val oneBigAction =
  action1 andThen
  action2 andThen
  action3
exec(oneBigAction)
```

We use this ```andThen``` method to chain the actions together with similar actions to a semicolon.  So if you
have ```A andThen B``` you get back an action that runs ```A``` for it's side effects and then runs ```B``` and then
results in the result from ```B```.  So in the above case ```oneBigAction``` would run ```action1``` then ```action2```
then ```action3``` with the result of the exec call being the result of ```action3```

## 2230 # Actions - Combinators advantages

So what are the advantages of using ```andThen``` over for calls to ```exec```?

First of all it is non-blocking.  ```exec``` is a hack.  You could use ```db.run``` and chain together futures with
```onSuccess``` but its messy and error prone.
Chaining together actions is very similar to chaining together futures.  The key difference is that if we chain
together actions Slick can perform certain optimisations about how it sends those actions to the database.
We also get to control things like database transactions that we will see later.

## 2240 # Actions - Interdependent actions

```scala
// Chaining interdependent actions
def runChainOfActions(a: SomeInput) = {
  val b = exec(createAction1(a))
  val c = exec(createAction2(b))
  val d = exec(createAction3(c))
  d
}
runChainOfActions()
```

In this code we are still running a sequence of actions but we are flowing values from action to action.  So our method
```runChainOfActions``` accepts a value ```a``` we use that to create our first action, pass that to ```exec``` and get
back a value ```b```.  And then we use ```b``` to generate the next action.   So in this code the result of each action
is used to determine which action to run next.  This may sound familiar as 'monadic comprehension', this
is ```flatMap``` or ```for``` comprehensions.

## 2250 # Actions - Interdependent actions 2

Actions are monads, they have ```map``` and ```flatMap``` methods and they have almost exactly the same semantics as
```map``` and ```flatMap``` on futures.

```scala
// Chaining interdependent actions
def chainOfActions(a: SomeInput) =
  for {
    b <- createAction1(a)
    c <- createAction2(b)
    d <- createAction3(c)
  } yield d

exec(chainOfActions(someInput))
```

The allows us to create one action that chooses action 1 based on ```a```, gets back a result ```b``` and uses that
to chose action 2 gets back a result ```c``` and so on.  The result of calling chainOfActions is still one action, we
still have not run anything when we call this method, but when we do run this method we will make all the decisions
about which actions to perform in which sequence and we will get back the result of action 3 as the final result.

This is very similar to for comprehensions of futures and you can think of actions and futures of being almost
interchangeable with the exception of the fact that if we use actions to compose things slick can do more for us.


## 2260 # Actions - The payoff

One last interesting thing to notice here,  the big action we are building here involves code that is written in Scala
as code that is written in SQL.  We are sending actions to the database, running queries and getting back results, but
we are also running Scala code to decide which query to run next.  So our actions can be entire scripts, composed of
database level and application level code.  And we run them with a single call to ```db.run```.

The magical payoff for all of this is we have the method ```transactionally```.  Whenever we build an action we can
call ```transactionally``` on it to generate a new action that does everything that that action did within a transaction
block.  If any of the queries we run fail for any reason and if any of our Scala code throws an exception the
transaction will fail, everything will roll back, and we will not have any effect on the database caused by that action.

So we can make any action, no matter how complicated, atomic.  This is really powerful and something you cannot do
with futures.  So when you are sequencing actions together try to use ```map``` and ```flatmap``` on action as well
as ```andThen``` and the other available combinators to create your actions.  Then you can decide whether you
want to run them in a transaction.

```scala
exec(action.transactionally)
```

## 2280 # Actions - Composition - Your turn

Update the below function to create an action which:

* takes the given artist, title and year of release
* Inserts them into the database.
* Rates it "Awesome" if it is their first album
* Rates it "Meh" otherwise

@:editor file=ActionComposition.scala

## 3000 # Joins

We have covered everything we need to with regard to actions and we are on the home stretch now.  Its time to talk
about joining tables together in queries.

## 3010 # Joins - Two tables - Albums

So for the joins section we wil be working with a variant of the Albums table we have seen before.  Instead of an
artist name we now refer to an ```Artist``` held in another table.

So here is our updated ```Album``` table:

```scala
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
```

It is almost exactly the same as before but we have replaced the artist name with an artistId which is a foreign key
into a new table called ```Artist```

## 3020 # Joins - Two tables - Artists

```scala
case class Artist(
  name   : String,
  id     : Long = 0L)

class ArtistTable(tag: Tag) extends Table[Artist](tag, "artists") {
  def name   = column[String]("name")
  def id     = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def * = (name, id) <> (Artist.tupled, Artist.unapply)
}
```

```Artist``` just stores the name and primary key, so really all we are doing here is normalising the data previously
denormalised in one table.  (We would have multiple copies of each artists name, one per album).

## 3030 # Joins - Two tables - The updated queries

So we also needed to update out basic queries.
```scala
 val createTablesAction =
   ArtistTable.schema.create andThen
   AlbumTable.schema.create
```

The createTablesAction now uses ```andThen``` to create two tables.

```scala
 val dropTablesAction =
   AlbumTable.schema.drop andThen
   ArtistTable.schema.drop
```

Similar for our ```dropTablesAction```

## 3040 # Joins - Two tables - The updated queries 2

The ```insertAllAction``` is a little more involved:

```scala
val insertAllAction: DBIOAction[Unit, NoStream, Effect.Write] =
  for {
   keyboardCatId <- ArtistTable returning ArtistTable.map(_.id) += Artist("Keyboard Cat"  )
   spiceGirlsId  <- ArtistTable returning ArtistTable.map(_.id) += Artist("Spice Girls"   )
   rickAstleyId  <- ArtistTable returning ArtistTable.map(_.id) += Artist("Rick Astley"   )
   myMatesBandId <- ArtistTable returning ArtistTable.map(_.id) += Artist("My Mate's Band")
   _             <- AlbumTable ++= Seq(
                 Album(keyboardCatId, "Keyboard Cat's Greatest Hits", 2009, Rating.Awesome),
                 Album(spiceGirlsId, "Spice"                        , 1996, Rating.Good   ),
                 Album(spiceGirlsId, "Forever"                      , 2000, Rating.Meh    ),
                 Album(rickAstleyId, "Whenever You Need Somebody"   , 1987, Rating.Awesome),
                 Album(rickAstleyId, "Hold Me in Your Arms"         , 1988, Rating.Good   ),
                 Album(rickAstleyId, "Free"                         , 1991, Rating.Meh    ),
  } yield ()
```

We use a ```for``` comprehension to first insert all of the artists, using ```returning``` (which we have not covered).
to get back the primary key rather than the row counts each time.  We Then use them to insert all of the albums.

## 3050 # Joins - Two types

There are two types of joins we are going to discuss:

* Implicit joins
* Explicit joins

The difference here is at the SQL level.  Implicit and explicit mean different types of SQL query.  These are not
Scala or Slick terms,  we are not talking about implicit parameters or type classes.  We are talking about implicit
SQL joins and explicit SQL joins.


## 3060 # Joins - Implicit joins

So what does an implicit SQL join look like?

```sql
SELECT *
FROM album, artist
WHERE album.id = artist.albumId;
```

In this example we are selecting data from two different tables ```album``` and ```artist``` but we are not
explicitly telling the database how to join those things together.  It is left up to the database to determine how to
get hold of the tables, which indexes to use and how to draw the relationship between the two tables.

The database can get a hint from the ```WHERE``` clause because we have a join condition (or a where condition that
filters out all rows where the ```album.id``` and the ```artist.albumId``` do not match).  But, the database has to do
this heuristically and in some circumstances, some databases will get confused by this and make sub-optimal choices.

## 3070 # Joins - Implicit joins 2

Implicit joins are typically they way we write joins in SQL, typically database can sort it out, but in some cases they
result in less than optimal query performance.  But nevertheless Slick provides a nice simple syntax for this.
So let us see how this works.

```scala
val query =
  for {
    album <- AlbumTable
    artist <- ArtistTable
    if album.id === artist.albumId
  } yield (album, artist)
```

We are back in the land of the query type rather than the action type.  We are using the one remaining query combinator
that we did not cover previously which is the ```flatMap``` method.  ```flatMap``` on a query allows us to join one
table onto another (or one query onto another).

## 3080 # Joins - Implicit joins 3

In this example we start with ```AlbumTable``` and ```flatMap```ing that, joining with the ```Artist``` table with a
join condition (the ```if``` statement).  If you are aware how for comprehensions are compiled you will know that the
```if``` guard is compiled to a ```filter``` or ```withFilter``` statement.  So if you think about it it ```filter``` is
pretty much the same as ```WHERE```.  We saw previously that filter conditions expand to ```WHERE``` clauses.  And that
is exactly what is happening here.

```sql
SELECT *
FROM album, artist
WHERE album.id = artist.albumId;
```

We are selecting ```*``` by selecting both tables as our result of this query.  And if you remember the types from the
earlier section you will realise our packed and unpacked types for this query are actually tuples of tables and albums
and artists.

## 3090 # Joins - Implicit joins - Your turn

Update the ```implicitJoin``` in the code below to create an implicit join that finds:

* Artists who have released albums
* and the albums they released (tuples)
* sort by the artist name

@:editor file=JoinsImplicit.scala

*N.b.* This is an exercise in combining the ```for``` comprehension syntax with the existing combinators we have used.
Notice we have asked for only artists who have released albums because there may be artists in the database who have
not released any albums.  To select that artist we would need a left-join which we have not covered yet.

## 3100 # Joins - Explicit joins

```sql
SELECT *
FROM artist INNER JOIN album
 ON artist.id = album.artistId;
```

An explicit join is exactly that.  Where we specify exactly what kind of join we want to perform.  In this case it is
an ```INNER JOIN``` and we specify exactly what the join criteria should be with the ```ON``` clause.  Everything from
the ```FROM``` to the semi-colon is part of the ```FROM``` clause so we can still provide wheres and sort clauses.

When are are saying what tables we want to select from we are giving the database no choice as to how to implement this.
This is a good thing in that it gives up complete control over the join semantics and a bad thing in that we do not
give the database any control.  So if we know exactly what we are doing and we are right about it this is a great way
of writing a query.

## 3110 # Joins - Explicit joins - In Slick

To perform the same explicit join in Slick we can do this:

```scala
val query =
  ArtistTable join AlbumTable
  on {
   (artist, album) =>
     artist.id === album.artistId
  }
```

There is a ```join``` method on the table query that allows us to join it onto another table query which produces
 an intermediate object which has an ```on``` method that allows us to specify the join criteria.  We provide a binary
 function that takes the two tables as parameters and returns our usual boolean ```Rep```

## 3120 # Joins - Explicit joins - your turn

Update the below code to complete the explicitJoin definition with an explicit join that finds:

* Artists who have released albums
* and the albums they have released
* sorted by artist name

(This is basically the same query you wrote with an implicit join rewritten as an explicit join.)

@:editor file=JoinsExplicit.scala


## 3130 # Joins - Summary

We have covered all of the basics of implicit and explicit joins and with that we now have a wide set of verbs for
producing SQL queries.  We know how to filter, select columns, sort, page and join tables together.


This brings us to the end of the discussions on queries and actions and sending commands to the database.

## 4000 # Profiles - Introduction

In the final section of this course we will discuss database profiles or drivers.

This is largely because the import at the top of every file:

```scala
import slick.driver.H2Driver.api._
```

Imports the entire Slick API for a specific driver (H2 in this case).  So we need to talk about how to work around that.

## 4010 # Profiles - Getting Database Profiles

Out of the box Slick supports six databases:

* Apache Derby
* H2
* HyperSQL
* MySQL
* PostgresSQL
* SQLite

There are open source drivers available from the [freeslick project](https://github.com/smootoo/freeslick) for:

* Oracle
* DB2
* MSSQL

There is some history behind this, but Lightbend (formally Typesafe) have recently released their commercial
drivers as open source for the same databases under the slick-extensions package.

## 4020 # Profiles - Supporting multiple profiles

There is one other aspect of working with profiles that we need to cover.  How do we write code that is generic across
different databases?

Rather than import the entire api we need to import
```scala
import slick.driver.{JdbcProfile, H2Driver}
```

```H2Driver``` is the driver we are going to work with and the abstract driver ```JDBCProfile```.  ```JDBCProfile```
is the supertype of all of the other drivers.  Most of the api provided the the actual drivers is actually provided
at the ```JDBCProfile``` level.

We can build code that depends on any JDBCProfile and then at the last minute inject a particular subclass to bind that
code to a particular type of database.

## 4030 # Profiles - Supporting multiple profiles 2

To do this we organise our code into a number of different traits representing modules in a database layer.  We mix
all of the traits together in the right order then at the end we have a class we can instantiate that has a
constructor parameter where we feed in the relevant driver.

At the root of our module class hierarchy we have ```DatabaseProfile```:

```scala
trait DatabaseProfile {
  val profile: JdbcProfile
}
```

Which simply provides a variable ```profile``` of type ```JDBCProfile```

## 4035 # Profiles - Supporting multiple profiles 3

Our table definitions is now in its own trait.


```scala
trait ArtistDatabaseModule {
  self: DatabaseProfile =>

  import profile.api._

  case class Artist(
    name   : String,
    id     : Long = 0L)

  class ArtistTable(tag: Tag) extends Table[Artist](tag, "artists") {
    def name   = column[String]("name")
    def id     = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def * = (name, id) <> (Artist.tupled, Artist.unapply)
  }

  lazy val ArtistTable = TableQuery[ArtistTable]
}
```

The key thing to notice is that this trait has a self type of ```DatabaseProfile```.  This
means we can only use this trait in a class which also extends ```DatabaseProfile```.    Because we know that
```DatabaseProfile``` is going to be mixed in to the instantiation class we can import the entire api from
```JDBCProfile``` simply by doing ```import profile.api._``` for use in declaring and tables and queries.

## 4040 # Profiles - Supporting multiple profiles 4

Here is the corresponding module for ```Album```.  One thing to note is that the self type includes both
```DatabaseProfile``` and ```ArtistDatabaseModule```.  This is so we can refer to ```Artist``` within the
```selectAllAction``` join.

```scala
trait AlbumDatabaseModule {
  self: DatabaseProfile with ArtistDatabaseModule =>

  import profile.api._

  case class Album(
    artistId : Long,
    title    : String,
    id       : Long = 0L)

  class AlbumTable(tag: Tag) extends Table[Album](tag, "albums") {
    def artistId = column[Long]("artistId")
    def title    = column[String]("title")
    def id       = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def * = (artistId, title, id) <> (Album.tupled, Album.unapply)
  }

  lazy val AlbumTable = TableQuery[AlbumTable]

  val selectAllAction: DBIO[Seq[(Artist, Album)]] =
    ArtistTable.join(AlbumTable)
      .on     { case (artist, album) => artist.id === album.artistId }
      .sortBy { case (artist, album) => artist.name.asc }
      .result
}
```

## 4045 # Profiles - Supporting multiple profiles 5

One last module.  The ```TestDatabaseModule``` relies on all three previous modules to
allow creating of a single create and drop actions that depend on both tables.

```scala
trait TestDataModule {
  self: DatabaseProfile with ArtistDatabaseModule with AlbumDatabaseModule =>

  import profile.api._

  val createTablesAction =
    ArtistTable.schema.create andThen
    AlbumTable.schema.create

  val dropTablesAction =
    AlbumTable.schema.drop andThen
    ArtistTable.schema.drop
}
```

## 4050 # Profiles - Supporting multiple profiles - Mixing it up

Finally we can mix them all together to produce a ```DatabaseLayer```.  It is a class, we can actually
instantiate it and we provide a constructor parameter which concretely instantiates the ```profile```:

```scala
class DatabaseLayer[A <: JdbcProfile](val profile: JdbcProfile) extends DatabaseProfile
  with ArtistDatabaseModule
  with AlbumDatabaseModule
  with TestDataModule {

  import profile.api._

  val db = Database.forConfig("dbconfig")

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2 seconds)
}
```

## 4060 # Profiles - Supporting multiple profiles - In use

When we actually run our made code we build a database layer by creating an instance of this class and passing in the
driver we care about.  All of the other stuff like ```db``` and ```db.run``` is provided by that class.  So we are
stacking together traits that each represent different modules and basing them all off a tiny trait that simply
says this is the profile I'm going to inject at runtime.

```
object Main {
  val databaselayer = new DatabaseLayer(H2Driver)

  import databaselayer._

  def main(args: Array[String]): Unit = {

    exec(doEverythingAction).foreach(println)
  }
}
```

## 4500 # Summary

This brings us to end of the course.  We hope you have found it useful.  We have covered all sorts of aspects of Slick.

* Defining tables
* Running single queries
* Sequencing queries
* Joins

There is a whole bunch of stuff we did not cover.

* Aggregate functions
* Streaming queries
* Plain SQL queries
* Query compilation

Everything in the course is covered in the book
[EssentialSlick](http://underscore.io/books/essential-slick).  Which provided the source material for this course and
covers everything seen and more in more detail.

One last thing...

## 9999 # The End

We would like to thank Dave Gurnell for use of his material from the ScalaX Slick workshop.

*We would love to hear what you think* - what does and does not work, what areas need tuning - rory@scalanator.io

The contents of this course are available at [slick-course](https://github.com/ScalanatorIO/slick-course)




