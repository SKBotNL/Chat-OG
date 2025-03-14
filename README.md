# Chat-OG
Chat plugin for [TrueOG](https://github.com/true-og/true-og)
> [!WARNING]  
> libffi, openssl, lzma, and zlib are needed for the argostranslate Python package to function

## Features
- Adds prefix, union and suffix to chat messages
- Formats chat messages with MiniMessage if the player sending them has the `chat-og.color` permission
- Translates chat messages using [argos-translate](https://github.com/argosopentech/argos-translate)
- Discord bridge with custom and animated emoji support.
- Staff chat and Premium chat.

## Building
```./gradlew build```

## Emoji Converter Credits
- https://github.com/mathiasbynens/emoji-test-regex-pattern
- https://github.com/amio/emoji.json/blob/HEAD/scripts/gen.js (this project is using a modified version, it's in the repository's root folder also called `gen.js`)
