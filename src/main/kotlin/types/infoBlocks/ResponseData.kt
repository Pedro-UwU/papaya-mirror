package ar.edu.itba.pf.types.infoBlocks

data class ResponseData(
    val code: Int,
    val headers: Map<String, List<String>>,
    val body: String,
    val bodySize: Int
)
