package de.westnordost.countryboundaries

import kotlinx.io.IOException
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.io.InputStream
import java.io.OutputStream
import kotlin.jvm.Throws

// Java API with inputstream and outputstream

/** Create a new CountryBoundaries by deserializing from the given input stream */
@Throws(IOException::class)
public fun deserializeFrom(inputStream: InputStream): CountryBoundaries =
    CountryBoundaries.deserializeFrom(inputStream.asSource().buffered())


/** Serialize this CountryBoundaries to the given output stream */
@Throws(IOException::class)
internal fun serializeTo(countryBoundaries: CountryBoundaries, outputStream: OutputStream) {
    countryBoundaries.serializeTo(outputStream.asSink().buffered())
}
