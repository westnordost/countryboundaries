package de.westnordost.countryboundaries;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class QueryResult
{
	private final Collection<String> containingIds;
	private final Collection<String> intersectingIds;

	public QueryResult(Collection<String> containingIds,
					   Collection<String> intersectingIds)
	{
		this.containingIds = containingIds == null ? Collections.emptyList() : containingIds;
		this.intersectingIds = intersectingIds == null ? Collections.emptyList() : intersectingIds;
	}

	public QueryResult(String[] containingIds, String[] intersectingIds)
	{
		this.containingIds = containingIds == null ? Collections.emptyList() : Arrays.asList(containingIds);
		this.intersectingIds = intersectingIds == null ? Collections.emptyList() : Arrays.asList(intersectingIds);
	}

	public Collection<String> getContainingIds()
	{
		return Collections.unmodifiableCollection(containingIds);
	}

	public Collection<String> getIntersectingIds()
	{
		return Collections.unmodifiableCollection(intersectingIds);
	}

	/** Two QueryResult objects are equal when they contain the same elements, regardless of order */
	@Override public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		QueryResult that = (QueryResult) o;

		return
				containingIds.containsAll(that.containingIds) &&
				that.containingIds.containsAll(containingIds) &&
				intersectingIds.containsAll(that.intersectingIds) &&
				that.intersectingIds.containsAll(intersectingIds);
	}

	@Override public int hashCode()
	{
		return 31 * containingIds.hashCode() + intersectingIds.hashCode();
	}

	@Override public String toString()
	{

		return Arrays.toString(containingIds.toArray()) + " " + Arrays.toString(intersectingIds.toArray());
	}
}
