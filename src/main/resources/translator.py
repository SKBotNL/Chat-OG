from lingua import Language, LanguageDetectorBuilder
import argostranslate.translate
import sys

text = sys.argv[1]
to_code = sys.argv[2]

lingua_to_argos = {
    Language.ARABIC: "ar",
    Language.AZERBAIJANI: "az",
    Language.CATALAN: "ca",
    Language.CHINESE: "zh",
    Language.CZECH: "cs",
    Language.DANISH: "da",
    Language.DUTCH: "nl",
    Language.ENGLISH: "en",
    Language.ESPERANTO: "eo",
    Language.FINNISH: "fi",
    Language.FRENCH: "fr",
    Language.GERMAN: "de",
    Language.GREEK: "el",
    Language.HEBREW: "he",
    Language.HINDI: "hi",
    Language.HUNGARIAN: "hu",
    Language.INDONESIAN: "id",
    Language.IRISH: "ga",
    Language.ITALIAN: "it",
    Language.JAPANESE: "ja",
    Language.KOREAN: "ko",
    Language.PERSIAN: "fa",
    Language.POLISH: "pl",
    Language.PORTUGUESE: "pt",
    Language.RUSSIAN: "ru",
    Language.SLOVAK: "sk",
    Language.SPANISH: "es",
    Language.SWEDISH: "sv",
    Language.TURKISH: "tr",
    Language.UKRAINIAN: "uk"
}

languages = [Language.ARABIC, Language.AZERBAIJANI, Language.CATALAN, Language.CHINESE, Language.CZECH, Language.DANISH, Language.DUTCH, Language.ENGLISH, Language.ESPERANTO, Language.FINNISH, Language.FRENCH, Language.GERMAN, Language.GREEK, Language.HEBREW, Language.HINDI, Language.HUNGARIAN, Language.INDONESIAN, Language.IRISH, Language.ITALIAN, Language.JAPANESE, Language.KOREAN, Language.PERSIAN, Language.POLISH, Language.PORTUGUESE, Language.RUSSIAN, Language.SLOVAK, Language.SPANISH, Language.SWEDISH, Language.TURKISH, Language.UKRAINIAN]
detector = LanguageDetectorBuilder.from_languages(*languages).build()
language = detector.detect_language_of(text)

print(lingua_to_argos[language] + " " + argostranslate.translate.translate(text, lingua_to_argos[language], to_code))