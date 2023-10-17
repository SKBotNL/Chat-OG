package nl.skbotnl.chatog

import com.google.gson.Gson
import net.kyori.adventure.text.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object BingTranslator {
    data class Translated(val translatedText: String?, val translatedFrom: String?, val error: Component?)

    data class DetectedLanguage(val language: String, val score: Double)
    data class Translation(val text: String, val to: String)
    data class TranslationResponse(val detectedLanguage: DetectedLanguage, val translations: List<Translation>)

    data class Error(val error: ErrorDetails)
    data class ErrorDetails(val code: Int, val message: String)

    private const val endpoint = "https://api.cognitive.microsofttranslator.com"
    var apiKey = Config.getApiKey()
    var subscriptionRegion = Config.getSubRegion()

    fun translate(text: String, language: String): Translated {
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${endpoint}/translate?api-version=3.0&to=${language}"))
            .headers(
                "Content-Type",
                "application/json",
                "Ocp-Apim-Subscription-Key",
                apiKey,
                "Ocp-Apim-Subscription-Region",
                subscriptionRegion
            )
            .POST(HttpRequest.BodyPublishers.ofString("""[{"Text":"${text.replace("\"", """\"""")}"}]"""))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val body = response.body()

        val gson = Gson()

        if (response.statusCode() == 401) {
            val errorResponse = gson.fromJson(body, Error::class.java)
            return Translated(
                null,
                null,
                ChatOG.mm.deserialize("<red>Something went wrong. <white>Error Code: ${errorResponse.error.code}, Error Message: ${errorResponse.error.message}")
            )
        }

        val translationResponse = gson.fromJson(body, Array<TranslationResponse>::class.java)

        return Translated(
            translationResponse[0].translations[0].text,
            translationResponse[0].detectedLanguage.language,
            null
        )
    }
}