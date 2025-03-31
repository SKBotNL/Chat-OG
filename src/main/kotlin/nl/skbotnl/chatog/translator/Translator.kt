package nl.skbotnl.chatog.translator

import net.kyori.adventure.text.Component

interface Translator {
    data class Translated(val translatedFrom: String?, val translatedText: String?, val error: Component?)

    fun init()

    fun translate(text: String, language: String): Translated
}