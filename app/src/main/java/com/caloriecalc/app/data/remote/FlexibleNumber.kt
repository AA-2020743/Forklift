package com.caloriecalc.app.data.remote

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/**
 * Reads a number that Open Food Facts might send as a JSON number *or* a JSON string.
 *
 * OFF's data is crowd-contributed and only loosely typed: the same field arrives as `30`,
 * `"30"`, `"30 g"`, `"1,5"` (comma decimal separator) or `""` depending on who entered it and
 * when. kotlinx.serialization is strict by default, so a single string where a Double was
 * declared throws and takes the *entire* response down with it — which is why one badly-typed
 * product could make a whole search look like it returned nothing.
 *
 * Anything unparseable becomes null (unknown) rather than an exception.
 */
object FlexibleDoubleSerializer : KSerializer<Double?> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleDouble", PrimitiveKind.STRING).nullable

    override fun deserialize(decoder: Decoder): Double? {
        val jsonDecoder = decoder as? JsonDecoder ?: return null
        val element = jsonDecoder.decodeJsonElement()
        if (element is JsonNull) return null
        val primitive = element as? JsonPrimitive ?: return null
        val raw = primitive.content.trim()
        if (raw.isEmpty()) return null
        // Strip a trailing unit ("30 g", "250ml") and normalise a comma decimal separator.
        val numeric = raw.replace(',', '.').takeWhile { it.isDigit() || it == '.' || it == '-' }
        return numeric.toDoubleOrNull()
    }

    override fun serialize(encoder: Encoder, value: Double?) {
        if (value == null) encoder.encodeNull() else encoder.encodeDouble(value)
    }
}
