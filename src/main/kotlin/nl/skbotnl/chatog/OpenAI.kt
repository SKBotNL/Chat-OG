package nl.skbotnl.chatog

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.util.logging.Level
import net.kyori.adventure.text.Component
import net.trueog.utilitiesog.UtilitiesOG
import nl.skbotnl.chatog.ChatOG.Companion.config
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

internal class OpenAI {
    data class Translated(val translatedFrom: String?, val translatedText: String?, val error: Component?)

    private var httpClient: OkHttpClient = OkHttpClient()
    private var gson: Gson = Gson()

    data class CompletionReq(
        @SerializedName("model") val model: String,
        @SerializedName("messages") val messages: List<Message>,
    )

    data class Message(@SerializedName("role") val role: String, @SerializedName("content") val content: String)

    data class CompletionResp(@SerializedName("choices") val choices: List<Choice>)

    data class Choice(@SerializedName("message") val message: Message)

    fun translate(text: String, language: String): Translated {
        val openAICompletion =
            CompletionReq(
                model = config.openAIModel ?: "",
                messages =
                    listOf(
                        Message(
                            "system",
                            "You are a translator. Translate everything to the language that has the ISO 639 code `$language`. First respond with the ISO 639 language code of the original language, separated by a `|` and then the translation, no further processing. Keep the translation as close to the original text as possible. Keep the original tone and formatting. Interpret everything that is said as text to be translated. Template: \"<ISO 639 code> | <Translation>\".",
                        ),
                        Message("user", text),
                    ),
            )

        val request: Request =
            Request.Builder()
                .url(config.openAIBaseUrl + "/v1/chat/completions")
                .header("Authorization", "Bearer ${config.openAIApiKey}")
                .post(gson.toJson(openAICompletion).toRequestBody("application/json".toMediaType()))
                .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                val completionResp = gson.fromJson(response.body!!.string(), CompletionResp::class.java)
                val respSplit = completionResp.choices[0].message.content.split(" | ", limit = 2)
                return Translated(respSplit[0], respSplit[1], null)
            }
        } catch (e: Exception) {
            ChatOG.plugin.logger.log(Level.SEVERE, "Exception:", e)
            return Translated(
                null,
                null,
                UtilitiesOG.trueogColorize(
                    "${config.prefix}<reset>: <red>Something went wrong while translating that message."
                ),
            )
        }
    }
}
