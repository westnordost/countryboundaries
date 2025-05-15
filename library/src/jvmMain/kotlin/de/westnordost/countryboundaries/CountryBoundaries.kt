package de.westnordost.countryboundaries

import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import java.io.InputStream
import java.io.OutputStream

public fun deserializeFrom(inputStream: InputStream): CountryBoundaries =
    CountryBoundaries.deserializeFrom(inputStream.asSource().buffered())

internal fun serializeTo(countryBoundaries: CountryBoundaries, outputStream: OutputStream) {
    countryBoundaries.serializeTo(outputStream.asSink().buffered())
}
