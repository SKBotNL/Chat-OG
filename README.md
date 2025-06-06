# Chat-OG
Chat plugin for [TrueOG](https://github.com/true-og/true-og)

## Features
- Adds prefix, union and suffix to chat messages.
- Formats chat messages with MiniMessage if the player sending them has the `chat-og.color` permission.
- Translates chat messages using any OpenAI(-compatible) API.
- Discord bridge with custom and animated emoji (Minecraft to Discord only) support.
- Staff and premium chat.

## Building
```./gradlew build```

## Emoji Converter Credits
- https://github.com/mathiasbynens/emoji-test-regex-pattern
- https://github.com/amio/emoji.json/blob/HEAD/scripts/gen.js (this project is using a modified version, it's in the repository's root folder also called `gen.js`)
