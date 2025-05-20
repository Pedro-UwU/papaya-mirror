package tools.utils

import java.io.File

class PythonServerUtil {
    private val pythonServerPath = "src/test/resources/simple_python_server"
    private var serverProcess: Process? = null

    fun startServer() {
        val processBuilder = ProcessBuilder("python3", "main.py")
            .directory(File(pythonServerPath))
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
        serverProcess = processBuilder.start()
        // Wait a bit to ensure the server is up
        Thread.sleep(3000)
    }

    fun stopServer() {
        serverProcess?.destroy()
        serverProcess?.waitFor()
    }
}