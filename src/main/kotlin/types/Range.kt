package ar.edu.itba.pf.types

import ar.edu.itba.pf.types.deserializers.RangeDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize(using = RangeDeserializer::class)
data class Range(
    val from: Any,
    val to: Any,
)
