package ar.edu.itba.pf.tools.infoLoggers

import ar.edu.itba.pf.types.InfoLogger
import ar.edu.itba.pf.types.infoBlocks.InfoBlock

class MinimalLogger : InfoLogger {
    override fun processBlock(infoBlock: InfoBlock) {
        println("Request sent to ${infoBlock.context?.current} - status: ${infoBlock.executionData.success}")
    }

}