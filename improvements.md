# Possible improvements

### Use cell numbers in boundaries.ser

Instead of serializing the data of each cell of the raster, first serialize all **unique** cells which then can be referred to by a number  (a `short`). 
When serializing the raster, simply serialize the number for each cell.

This reduces the file size and in-memory size by about one third. However, when zipped, there is not really any difference, actually it is then even slightly larger. 
Also, at least theoretically, it is possible to run out of cell numbers (when there are too many unique cells). The current default data set is far away from that, however.

But anyway, probably not worth changing the serialization format for that.
On loading the data, one could make use of that technique without changing the serialization format in order to achieve one third less memory consumption, but this slows down loading by about one quarter. So, not worth it.

### Use a 16 bit integer for longitude / latitude

...instead of a 32bit integer. This would reduce file size and in-memory size by almost half.

The max number for a 16 bit integer is 65535.
The default source data (JOSM boundaries.osm) has a precision of 5 decimals for positions, which is about 1 meter. 
The coordinates within each cell do not need to be global coordinates (e.g. lon = 65.012345) but could be local to the cell (e.g. lon = 0.0 is the left side of the cell).

To not lose any precision, there thus must be at least `x` cells to be able to save lat/lon as 16bit integers:
```
   360 / (x * 2^16) < 10^-5
-> x > 360 * 10^5 / 2^16
-> x > 550
```
... which means the raster size should have at minimum at least 550x275 which is about 50% larger than the "default" (i.e. used in StreetComplete, used for performance tests shown in readme) of 360x180.
Hence, 
- it is less flexible as grid sizes below 550x275 have caveats in precision that may feel like bugs if not explained properly
- for common values for the grid, the resulting file is larger (due to the new recommended minimum)
- there is a (tiny) calculation overhead to convert global coordinates to cell coordinates
- current serialization format supports precision up to 7 decimals (~1 centimeter) regardless of grid size, hence is more flexible
- but at least in 50% more cases, getting the country id consists of a simple lookup + when point in polygon checks have to be made, they deal with polygons half the size
