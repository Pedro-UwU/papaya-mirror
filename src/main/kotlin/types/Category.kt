package ar.edu.itba.pf.types

enum class Category {
    TEXT, // Lorem ipsum
    USERNAME,
    NAME,
    SURNAME,
    PASSWORD,
    EMAIL,
    CURRENCY,
    INTEGER,
    FLOAT,
    TIMESTAMP,
    FILE,
    PICTURE,  // Base64 - TODO: Generalize
    AVATAR;  // Base64 - TODO: Generalize

    companion object {
        private val stringToEnum = values().associateBy { it.name.lowercase() }

        fun fromString(symbol: String): Category? {
            return stringToEnum[symbol.lowercase()]
        }
    }
}