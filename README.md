# Chat-OG
Chat plugin for [TrueOG](https://github.com/true-og/true-og)\
**Libffi is needed for the argostranslate Python package. Otherwise, the package will not install!**
## Features
- Adds prefix, union and suffix to chat messages
- Formats chat messages with MiniMessage if the player sending them has the `chat-og.color` permission
- Translates chat messages using [argos-translator](https://github.com/argosopentech/argos-translate)
- Discord bridge
- Staff and donor chat
## Building
If you want the translator ([argos-translate](https://github.com/argosopentech/argos-translate)) run `./gradlew buildPython` before building (make sure you have the development packages of zlib, openssl, lzma and libffi)\
Building: `./gradlew build`

## Emoji Converter Credits
- https://github.com/mathiasbynens/emoji-test-regex-pattern
- https://github.com/amio/emoji.json/blob/HEAD/scripts/gen.js (this project is using a modified version, it's in the repository's root folder also called `gen.js`)
