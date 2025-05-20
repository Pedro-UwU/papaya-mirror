package ar.edu.itba.pf.types

enum class HTTPMethod {
    GET, POST, PUT, DELETE, HEAD, PATCH;

    companion object {
        private val stringToEnum = values().associateBy { it.name.lowercase() }

        fun fromString(symbol: String): HTTPMethod? {
            return stringToEnum[symbol.lowercase()]
        }
    }
}