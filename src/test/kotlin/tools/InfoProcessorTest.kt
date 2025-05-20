package tools

import ar.edu.itba.pf.tools.InfoProcessor
import ar.edu.itba.pf.types.Context
import ar.edu.itba.pf.types.InfoLogger
import ar.edu.itba.pf.types.responses.ResponseSuccess
import ar.edu.itba.pf.types.infoBlocks.ExecutionData
import ar.edu.itba.pf.types.infoBlocks.InfoBlock
import ar.edu.itba.pf.types.infoBlocks.RequestData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertTrue

class InfoProcessorTest {
    @Test
    fun `Given single infoBlock Print it`() {
        val channel = Channel<InfoBlock>(capacity = 1)
        val mockLogger = mock<InfoLogger>()
        val processingRequests = AtomicInteger(1)
        val infoProcessor = InfoProcessor(
            channel = channel,
            processingRequests = processingRequests,
            infoLoggers = listOf(mockLogger)
        )

        runBlocking {
            val processorJob = launch {
                infoProcessor.start()
            }
            val testInfoBlock = InfoBlock(
                requestData = RequestData(emptyMap(), emptyMap(), null),
                executionData = ExecutionData(
                    Instant.now(),
                    Instant.now().plusSeconds(10),
                    ResponseSuccess.SUCCESS
                ),
                context = Context(
                    current = "test",
                    next = emptySet(),
                    content = mapOf("<_id_>" to "testID")
                )
            )
            channel.send(testInfoBlock)
            processingRequests.decrementAndGet()
            processorJob.join()

            assertTrue(!infoProcessor.isRunning())
            verify(mockLogger).processBlock(testInfoBlock)
        }
    }
}
