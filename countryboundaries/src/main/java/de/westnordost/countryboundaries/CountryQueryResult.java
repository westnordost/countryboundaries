package de.westnordost.countryboundaries;

import java.util.Collection;
import java.util.Collections;

public class CountryQueryResult
{
	private final Collection<String> containingCountryCodes;
	private final Collection<String> intersectingCountryCodes;

	public CountryQueryResult(Collection<String> containingCountryCodes,
							  Collection<String> intersectingCountryCodes)
	{
		this.containingCountryCodes = containingCountryCodes;
		this.intersectingCountryCodes = intersectingCountryCodes;
	}

	public Collection<String> getContainingCountryCodes()
	{
		return containingCountryCodes == null ?
				Collections.emptyList() : Collections.unmodifiableCollection(containingCountryCodes);
	}

	public Collection<String> getIntersectingCountryCodes()
	{
		return intersectingCountryCodes == null ?
				Collections.emptyList() : Collections.unmodifiableCollection(intersectingCountryCodes);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CountryQueryResult that = (CountryQueryResult) o;

		return
				containingCountryCodes != null ?
						containingCountryCodes.equals(that.containingCountryCodes)
						: that.containingCountryCodes == null
						&&
						intersectingCountryCodes != null ?
						intersectingCountryCodes.equals(that.intersectingCountryCodes)
						: that.intersectingCountryCodes == null;
	}

	@Override
	public int hashCode()
	{
		int result = containingCountryCodes != null ? containingCountryCodes.hashCode() : 0;
		result = 31 * result + (intersectingCountryCodes != null ? intersectingCountryCodes.hashCode() : 0);
		return result;
	}
}
