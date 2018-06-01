# countryboundaries

Java library to enable fast offline reverse country geocoding: Find out the country / state in which a geo position is located.

It is well tested, does not have any dependencies, works well on Android and most importantly, is very fast.

Requires Java 8.

## Copyright and License

© 2018 Tobias Zwick. This library is released under the terms of the [GNU Lesser General Public License](http://www.gnu.org/licenses/lgpl-3.0.html) (LGPL).
The default data used is derived from OpenStreetMap and thus © OpenStreetMap contributors and licensed under the [Open Data Commons Open Database License](https://opendatacommons.org/licenses/odbl/) (ODbL).

## Usage

Add [`de.westnordost:countryboundaries:1.2`](https://maven-repository.com/artifact/de.westnordost/countryboundaries/1.2) as a Maven dependency or download the jar from there.

```java
// load data. You should do this once and use CountryBoundaries as a singleton.
CountryBoundaries boundaries = CountryBoundaries.load(new FileInputStream("boundaries.ser"));
	
// get country ids by position
boundaries.getIds(-96.7954, 32.7816); // returns "US-TX","US"

// check if a position is in a country
boundaries.isIn(8.6910, 47.6973, "DE"); // returns true

// get which country ids can be found within the given bounding box
boundaries.getIntersectingIds(5.9865, 50.7679, 6.0599, 50.7358) // returns "DE", "BE", "NL, "EU"

// get which country ids completely cover the given bounding box
boundaries.getContainingIds(5.9865, 50.7679, 6.0599, 50.7358) // returns only "EU"
```

The default data file is in `/data/`. Don't forget to give attribution when distributing it.

## Speed

With the default data set, you can expect each call to take something between 0.1 to 0.5 ms and loading the data to take about 1 second - tested on my Sony Xperia Z1 Compact (Android phone from 2014). What makes it that fast is because the boundaries are split up into a raster. In the default data, I used a raster of 180x180 (= one cell is 2° in longitude, 1° in latitude).
If you need it even faster (down to below 0.1 ms), you need to import the data set into a bigger raster, see below. The bigger the raster, the larger the file, of course.

## Data

What exactly is returned when calling `getIds` is dependent on the data set used. The default data set in `/data/` is generated from [this file in the JOSM project](https://josm.openstreetmap.de/export/HEAD/josm/trunk/data/boundaries.osm). It...
- uses ISO 3166-1 alpha-2 country codes where available and otherwise ISO 3166-2 for subdivision codes. The data set includes subdivisions only for the United States, Australia, China and India.
- is oblivious of sea borders and will only return correct results for geo positions on land. If you are a pirate and want to know when you reached international waters, don't use this data!

You can import own data from a GeoJson or an OSM XML, using the Java application in the `/generator/` folder. See the source code there for details, it's not that much.
