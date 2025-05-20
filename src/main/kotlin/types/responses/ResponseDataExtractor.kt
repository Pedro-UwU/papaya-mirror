package ar.edu.itba.pf.types.responses
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import java.util.AbstractMap.SimpleEntry

class ResponseDataExtractor(val response: Response) {
    suspend fun extractData(requestResponse: HttpResponse): Map<String, String> {
        return response.responseExtract.mapNotNull { extractionConfig ->
            try {
                extractData(requestResponse, extractionConfig.value)
            } catch (e: Exception) {
                // If extraction fails and there's a default value, use it
                extractionConfig.value.default?.let { defaultValue ->
                    SimpleEntry(extractionConfig.value.name, defaultValue)
                } ?: throw ExtractionException(String.format(
                    "Missing value of %s in response",
                    extractionConfig.value
                ))
            }
        }.associate { it.key to it.value }
    }

    private suspend fun extractData(
        requestResponse: HttpResponse,
        extraction: ResponseExtraction
    ): Map.Entry<String, String>? {
        val path = extraction.path

        return when {
            // Extract from response body
            path.startsWith("body.") -> {
                val bodyPath = path.substring(5)
                val responseText = requestResponse.bodyAsText()

                // Parse JSON response
                val jsonElement = try {
                    Json.parseToJsonElement(responseText)
                } catch (e: Exception) {
                    throw ExtractionException("Failed to parse response body as JSON: ${e.message}")
                }

                // Extract value using JSON path
                val value = extractFromJson(jsonElement, bodyPath)
                    ?: extraction.default
                    ?: throw ExtractionException("Path not found in response body: $bodyPath")

                SimpleEntry(extraction.name, value)
            }

            // Extract from response headers
            path.startsWith("header.") -> {
                val headerName = path.substring(7)
                    // Handle quoted header names (for headers with spaces)
                    .let {
                        if (it.startsWith("\"") && it.endsWith("\"")) {
                            it.substring(1, it.length - 1)
                        } else {
                            it
                        }
                    }

                val headerValue = requestResponse.headers[headerName]
                    ?: extraction.default
                    ?: throw ExtractionException("Header not found: $headerName")

                SimpleEntry(extraction.name, headerValue)
            }

            // Extract from response cookies
            path.startsWith("cookie.") -> {
                val cookieName = path.substring(7)
                    // Handle quoted cookie names
                    .let {
                        if (it.startsWith("\"") && it.endsWith("\"")) {
                            it.substring(1, it.length - 1)
                        } else {
                            it
                        }
                    }

                // Extract cookies from the response
                val cookies = requestResponse.headers.getAll(HttpHeaders.SetCookie)
                    ?.mapNotNull { parseSetCookieHeader(it) }
                    ?: emptyList()

                val cookieValue = cookies.find { it.first == cookieName }?.second
                    ?: extraction.default
                    ?: throw ExtractionException("Cookie not found: $cookieName")

                SimpleEntry(extraction.name, cookieValue)
            }

            else -> throw ExtractionException("Invalid extraction path format: $path")
        }
    }

    private fun extractFromJson(jsonElement: JsonElement, path: String): String? {
        val segments = splitJsonPath(path)
        var current: JsonElement = jsonElement

        for (segment in segments) {
            current = when {
                // Handle array access: users[0]
                segment.contains("[") && segment.endsWith("]") -> {
                    val property = segment.substringBefore("[")
                    val indexStr = segment.substring(
                        segment.indexOf("[") + 1,
                        segment.lastIndexOf("]")
                    )

                    val index = indexStr.toIntOrNull()
                        ?: throw ExtractionException("Invalid array index: $indexStr")

                    // Access property then array element
                    val array = when (current) {
                        is JsonObject -> current.jsonObject[property]
                        else -> throw ExtractionException("Cannot access property '$property' on non-object")
                    } ?: throw ExtractionException("Property not found: $property")

                    when (array) {
                        is JsonArray -> array.jsonArray.getOrNull(index)
                        else -> throw ExtractionException("Cannot access index on non-array: $property")
                    } ?: throw ExtractionException("Array index out of bounds: $index")
                }

                // Regular property access
                else -> {
                    val propertyName = if (segment.startsWith("\"") && segment.endsWith("\"")) {
                        // Handle quoted property names (for properties with spaces)
                        segment.substring(1, segment.length - 1)
                    } else {
                        segment
                    }

                    when (current) {
                        is JsonObject -> current.jsonObject[propertyName]
                        else -> throw ExtractionException("Cannot access property '$propertyName' on non-object")
                    } ?: throw ExtractionException("Property not found: $propertyName")
                }
            }
        }

        // Convert final JsonElement to string value
        return when (current) {
            is JsonPrimitive -> current.toString().trim('"')  // Remove quotes for string values
            else -> current.toString()
        }
    }

    private fun splitJsonPath(path: String): List<String> {
        val segments = mutableListOf<String>()
        var currentSegment = StringBuilder()
        var inQuotes = false

        for (char in path) {
            when {
                char == '"' -> {
                    inQuotes = !inQuotes
                    currentSegment.append(char)
                }
                char == '.' && !inQuotes -> {
                    if (currentSegment.isNotEmpty()) {
                        segments.add(currentSegment.toString())
                        currentSegment = StringBuilder()
                    }
                }
                else -> currentSegment.append(char)
            }
        }

        // Add the last segment
        if (currentSegment.isNotEmpty()) {
            segments.add(currentSegment.toString())
        }

        return segments
    }

    private fun parseSetCookieHeader(setCookieHeader: String): Pair<String, String>? {
        val cookieParts = setCookieHeader.split(";").firstOrNull()?.trim() ?: return null
        val nameValueParts = cookieParts.split("=", limit = 2)

        if (nameValueParts.size != 2) return null

        val name = nameValueParts[0].trim()
        val value = nameValueParts[1].trim()

        return name to value
    }

    class ExtractionException(message: String) : Exception(message)
}