package de.westnordost.countryboundaries;

public class OsmXmlException extends RuntimeException
{
	public OsmXmlException(Throwable cause) { super(cause); }
	public OsmXmlException(String message) { super(message); }
	public OsmXmlException(String message, Throwable cause) { super(message, cause); }
}
