# countryboundaries

Kotlin multiplatform library to find in which region a given geo position is located fast.

## Copyright and License

© 2018-2025 Tobias Zwick. This library is released under the terms of the [GNU Lesser General Public License](http://www.gnu.org/licenses/lgpl-3.0.html) (LGPL).
The default data used is derived from OpenStreetMap and thus © OpenStreetMap contributors and licensed under the [Open Data Commons Open Database License](https://opendatacommons.org/licenses/odbl/) (ODbL).

## Usage

Add `de.westnordost:countryboundaries:3.0.0` as a Maven dependency or download the jar from there.

```kotlin
// load data. You should do this once and use CountryBoundaries as a singleton.
val source = FileSystem.source("boundaries.ser").buffered()
val boundaries = source.use { CountryBoundaries.deserializeFrom(it) }

// get ids of regions at position
boundaries.getIds(
    longitude = -96.7954,
    latitude = 32.7816
) // returns "US-TX","US"

// check if a position is in region with specified id
boundaries.isIn(
    longitude = 8.6910,
    latitude = 47.6973,
    id = "DE"
) // returns true

// get ids of the regions that are present in the given bounds
boundaries.getIntersectingIds(
    minLongitude = 50.6,
    minLatitude = 5.9,
    maxLongitude = 50.8,
    maxLatitude = 6.1
) // returns "NL", "LU", "DE", "BE", "BE-VLG", "BE-WAL"

// get ids of the regions that completely cover the given bounds
boundaries.getContainingIds(
    minLongitude = 50.6,
    minLatitude = 5.9,
    maxLongitude = 50.8,
    maxLatitude = 6.1
) // returns empty list
```

On Java, use this to load the boundaries:

```java
CountryBoundaries boundaries = null;
try (FileInputStream fis = new FileInputStream("boundaries.ser")) {
  boundaries = CountryBoundariesUtils.deserializeFrom(fis);
}
```

The default data file is in `/data/`. Don't forget to give attribution when distributing it. See below.

## Data

What exactly is returned when calling `getIds` is dependent on the source data used. The default data in `/data/` is generated from [this file in the JOSM project](https://josm.openstreetmap.de/export/HEAD/josm/trunk/resources/data/boundaries.osm). It...
- uses ISO 3166-1 alpha-2 country codes where available and otherwise ISO 3166-2 for subdivision codes
- is oblivious of sea borders and will only return correct results for geo positions on land. If you are a pirate and want to know when you reached international waters, don't use this data!
- includes all subdivisions for 🇺🇸 United States, 🇨🇦 Canada, 🇦🇺 Australia, 🇨🇳 China, 🇮🇳 India, 🇪🇸 Spain, 🇬🇧 United Kingdom, 🇧🇪 Belgium, 🇧🇦 Bosnia & Herzegovina, 🇮🇩 Indonesia, 🇫🇲 Micronesia, 🇰🇲 Comoros and [**autonomous** subdivisions of most other countries](https://en.wikipedia.org/wiki/List_of_autonomous_areas_by_country) that have a ISO 3166-2 code, such as the republics within 🇷🇺 Russia, the autonomous regions in 🇮🇹 Italy etc.

You can import own data from a GeoJson or an OSM XML, using the Java application in the `/generator/` folder. This is also useful if you want to have custom raster sizes. What are rasters? See below.

## Speed

Using the default data, on a Samsung S10e (Android phone from 2019), querying a single location takes something between 0.02 to 0.06 milliseconds. Querying 1 million random locations on a single thread takes about 0.5 seconds, with a Ryzen 5700X CPU (still single thread) about one quarter of that.

What makes it that fast is because the boundaries of the source data are split up into a raster. For the above measurements, I used a raster of 360x180 (= one cell is 1° in longitude, 1° in latitude). 
You can choose a smaller raster to have a smaller file or choose a bigger raster to have faster queries. According to my tests, a file with a raster of 60x30 (= one cell is 6° in longitude and latitude) is about 4 times smaller but queries are about 4 times slower.

Files with a raster of 60x30, 180x90 and 360x180 with the default data are supplied in `/data/` but as explained in the above section, you can create files with custom raster sizes.

The reason why the library does not directly consume a GeoJSON or similar but only a file generated from it is so that the slicing of the source geometry into a raster does not need to be done each time the file is loaded but only once before putting the current version of the boundaries into the distribution.
