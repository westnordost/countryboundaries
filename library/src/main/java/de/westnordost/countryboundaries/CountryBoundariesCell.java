package de.westnordost.countryboundaries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/** One cell in the country boundaries grid */
class CountryBoundariesCell
{
	final Collection<String> containingIds;
	final Collection<CountryAreas> intersectingCountries;

	CountryBoundariesCell(
			Collection<String> containingIds,
			Collection<CountryAreas> intersectingCountries)
	{
		this.containingIds = containingIds;
		this.intersectingCountries = intersectingCountries;
	}

	/** Returns whether the given point is in any of the given ids */
	boolean isInAny(int x, int y, Collection<String> ids)
	{
		for (String id : containingIds)
		{
			if(ids.contains(id)) return true;
		}
		if (!intersectingCountries.isEmpty())
		{
			Point point = new Point(x, y);
			for (CountryAreas areas : intersectingCountries)
			{
				if (ids.contains(areas.id))
				{
					if (areas.covers(point)) return true;
				}
			}
		}
		return false;
	}

	/** Return all ids that cover the given point */
	List<String> getIds(int x, int y)
	{
		List<String> result = new ArrayList<>(containingIds.size());

		result.addAll(containingIds);
		if (!intersectingCountries.isEmpty())
		{
			Point point = new Point(x, y);
			for (CountryAreas areas : intersectingCountries)
			{
				if (areas.covers(point))
				{
					result.add(areas.id);
				}
			}
		}
		return result;
	}

	/** Return all ids that completely cover or partly cover this cell */
	Collection<String> getAllIds()
	{
		Collection<String> result = new ArrayList<>(containingIds.size() + intersectingCountries.size());
		result.addAll(containingIds);
		for (CountryAreas areas : intersectingCountries)
		{
			result.add(areas.id);
		}
		return result;
	}

	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CountryBoundariesCell that = (CountryBoundariesCell) o;

		return	containingIds.equals(that.containingIds) &&
				intersectingCountries.equals(that.intersectingCountries);
	}

	@Override public int hashCode()
	{
		return 31 * containingIds.hashCode() + intersectingCountries.hashCode();
	}

	@Override public String toString()
	{
		return Arrays.toString(containingIds.toArray()) + " " + Arrays.toString(intersectingCountries.toArray());
	}
}
