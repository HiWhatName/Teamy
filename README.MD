# Teamy - A silly bot made for a minecraft competition
![main workflow](https://github.com/HiWhatName/Teamy/actions/workflows/gradle.yml/badge.svg) Teamy allows servers to manage teams of users in an easy way, whilst providing a lot of features. This was kinda intended to be a way for me(HiWhatName) to learn the JDA-5 framework.

**Important notice:** *As of the 8th jan 2023 I(HiWhatName) won't be maintaining this project anymore. If you still choose to use it, be warned as I won't provide any support.*

## "Features":
- Reaction based team system with slash commands. -> Each guild(server) can have their own preferences.
- Mute annoying screamers.
- Config file
- In-depth logging

## Setup tutorial
1] Copy '.env.example' to a file called '.env'
2] Configure the bot to your likings.
3] Enable the intent listed under the 'Required intents' section in the discord developer settings.
4] Change guild(server) specific settings in the guilds.json file.
5] Done! Ready to use the bot

## Required intents:
- Message content intent
- Server member intent
- (optional) Presence intent
