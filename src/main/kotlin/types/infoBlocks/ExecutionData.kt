package ar.edu.itba.pf.types.infoBlocks

import ar.edu.itba.pf.types.responses.ResponseSuccess
import java.time.Instant

data class ExecutionData(
    val start: Instant,
    val end: Instant,
    val success: ResponseSuccess,
    val errorMessage: String? = null,
)
