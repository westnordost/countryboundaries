package de.westnordost.countryboundaries;

public interface CountryBoundariesIndex
{
	CountryQueryResult query(double longitude, double latitude);
	CountryQueryResult query(double minLong, double minLat, double maxLong, double maxLat);
}
