package tools

import ar.edu.itba.pf.tools.InfoProcessor
import ar.edu.itba.pf.types.Context
import ar.edu.itba.pf.types.InfoLogger
import ar.edu.itba.pf.types.responses.ResponseSuccess
import ar.edu.itba.pf.types.infoBlocks.ExecutionData
import ar.edu.itba.pf.types.infoBlocks.InfoBlock
import ar.edu.itba.pf.types.infoBlocks.RequestData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertTrue

class InfoProcessorTest {
    @Test
    fun `Given single infoBlock Print it`() {
        val channel = Channel<InfoBlock>(capacity = 1)
        val mockLogger = mock<InfoLogger>()
        val processingInfo = AtomicBoolean(false)
        val infoProcessor = InfoProcessor(
            channel = channel,
            infoLoggers = listOf(mockLogger),
            processingInfo
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
                    content = mapOf("<_id_>" to "testID"),
                    globalParameters = emptyMap()
                ),
                executionId = "test"
            )
            channel.send(testInfoBlock)
            yield()
            while (processingInfo.get()) {
                yield()
            }
            infoProcessor.stop()
            processorJob.join()

            assertTrue(!infoProcessor.isRunning())
            verify(mockLogger).processBlock(testInfoBlock)
        }
    }
}
