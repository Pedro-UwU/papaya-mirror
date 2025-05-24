package ar.edu.itba.pf.tools.infoLoggers

enum class InfoLoggers {
    STDOUT,
    MINIMAL,
    TSV,
    JSON,
    NONE;

    companion object {
        private val stringToEnum = InfoLoggers.values().associateBy { it.name.lowercase() }

        fun fromString(symbol: String): InfoLoggers {
            return stringToEnum[symbol.lowercase()] ?: NONE
        }
    }
}