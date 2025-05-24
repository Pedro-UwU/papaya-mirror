package ar.edu.itba.pf.types.types

import ar.edu.itba.pf.tools.infoLoggers.InfoLoggers
import ar.edu.itba.pf.types.deserializers.OptionsDeserializer
import ar.edu.itba.pf.types.deserializers.types.deserializers.InfoLoggerOptionDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.io.File

@JsonDeserialize(using = InfoLoggerOptionDeserializer::class)
data class InfoLoggerOption(
    val logger: InfoLoggers,
    val path: File?,
)
