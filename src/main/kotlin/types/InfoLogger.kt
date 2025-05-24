package ar.edu.itba.pf.types

import ar.edu.itba.pf.types.infoBlocks.InfoBlock

interface InfoLogger {
    fun processBlock(infoBlock: InfoBlock)
    fun stop() {/* Do nothing by default */
    }
}