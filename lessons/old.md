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
