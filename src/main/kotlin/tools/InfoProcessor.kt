package ar.edu.itba.pf.tools

import ar.edu.itba.pf.types.InfoLogger
import ar.edu.itba.pf.types.infoBlocks.InfoBlock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.yield
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class InfoProcessor(
    private val channel: Channel<InfoBlock>,
    private val infoLoggers: List<InfoLogger>,
    private val processingInfoBlock: AtomicBoolean

) {
    private var running = false

    suspend fun start() {
        running = true
        while (running) {
//            println("Processing Info: ${processingInfoBlock.get()}")
            val infoBlock = channel.tryReceive().getOrNull()
            if (infoBlock != null) {
                processingInfoBlock.getAndSet(true)
                processBlock(infoBlock)
                processingInfoBlock.getAndSet(false)
            }
            yield()
        }
    }

    fun stop() {
        running = false
    }

    private fun processBlock(infoBlock: InfoBlock) =
        infoLoggers.forEach { logger -> logger.processBlock(infoBlock) }

    fun isRunning(): Boolean = running
}
