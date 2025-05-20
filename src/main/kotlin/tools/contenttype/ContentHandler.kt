package ar.edu.itba.pf.tools.contenttype

import com.fasterxml.jackson.databind.JsonNode

interface ContentHandler {
    fun encode(content: JsonNode): String
}