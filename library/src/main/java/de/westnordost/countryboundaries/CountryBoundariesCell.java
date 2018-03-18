package de.westnordost.countryboundaries;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** One cell in the country boundaries grid */
class CountryBoundariesCell
{
	private final Collection<String> containingIds;
	private final Collection<CountryAreas> intersectingCountries;

	CountryBoundariesCell(
			Collection<String> containingIds,
			Collection<CountryAreas> intersectingCountries)
	{
		this.containingIds = containingIds;
		this.intersectingCountries = intersectingCountries;
	}

	boolean isInAny(double longitude, double latitude, Collection<String> ids)
	{
		for (String id : containingIds)
		{
			if(ids.contains(id)) return true;
		}
		Point point = new Point(Fixed1E7.doubleToFixed(longitude), Fixed1E7.doubleToFixed(latitude));
		for (CountryAreas areas : intersectingCountries)
		{
			if(ids.contains(areas.id))
			{
				if(areas.covers(point)) return true;
			}
		}
		return false;
	}

	List<String> getIds(double longitude, double latitude)
	{
		List<String> result = new ArrayList<>(containingIds.size());

		result.addAll(containingIds);
		if (!intersectingCountries.isEmpty())
		{
			Point point = new Point(Fixed1E7.doubleToFixed(longitude), Fixed1E7.doubleToFixed(latitude));
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

	void write(ObjectOutputStream out) throws IOException
	{
		out.writeInt(containingIds.size());
		for (String id : containingIds)
		{
			out.writeUTF(id);
		}
		out.writeInt(intersectingCountries.size());
		for (CountryAreas areas : intersectingCountries)
		{
			areas.write(out);
		}
	}

	static CountryBoundariesCell read(ObjectInputStream in) throws IOException
	{
		List<String> containingIds = Collections.emptyList();
		List<CountryAreas> intersectingCountries = Collections.emptyList();

		int containingIdsSize = in.readInt();
		if(containingIdsSize > 0)
		{
			containingIds = new ArrayList<>(containingIdsSize);
			for (int i = 0; i < containingIdsSize; i++)
			{
				containingIds.add(in.readUTF().intern());
			}
		}
		int intersectingPolygonsSize = in.readInt();
		if(intersectingPolygonsSize > 0)
		{
			intersectingCountries = new ArrayList<>(intersectingPolygonsSize);
			for (int i = 0; i < intersectingPolygonsSize; i++)
			{
				intersectingCountries.add(CountryAreas.read(in));
			}
		}
		return new CountryBoundariesCell(containingIds, intersectingCountries);
	}
}
