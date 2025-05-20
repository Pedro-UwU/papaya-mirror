package ar.edu.itba.pf.types.infoBlocks

import ar.edu.itba.pf.types.Context

data class InfoBlock(
    val context: Context? = null,
    val requestData: RequestData,
    val responseData: ResponseData? = null,
    val executionData: ExecutionData,
    val executionId: String,
)


