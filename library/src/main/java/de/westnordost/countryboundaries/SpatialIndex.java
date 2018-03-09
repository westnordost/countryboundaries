package de.westnordost.countryboundaries;

public interface SpatialIndex
{
	QueryResult query(double longitude, double latitude);
	QueryResult query(double minLong, double minLat, double maxLong, double maxLat);
}
