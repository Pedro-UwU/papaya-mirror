package ar.edu.itba.pf.types

import ar.edu.itba.pf.types.deserializers.OptionsDeserializer
import ar.edu.itba.pf.types.types.InfoLoggerOption
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize(using = OptionsDeserializer::class)
data class Options(
    val requestsPerEndpoint: Int,
    val numberOfWorkers: Int,
    val loggers: List<InfoLoggerOption>
)
