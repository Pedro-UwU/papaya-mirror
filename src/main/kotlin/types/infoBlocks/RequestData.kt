package ar.edu.itba.pf.types.infoBlocks

import com.fasterxml.jackson.databind.JsonNode

data class RequestData(
    val headers: Map<String, String>,
    val queryParams: Map<String, String>,
    val body: JsonNode? = null,
)
