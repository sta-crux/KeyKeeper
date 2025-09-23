# KeyKeeper Bot

KeyKeeper Bot is a Telegram bot designed to securely manage and back up credentials. It allows users to store, retrieve, and back up their credentials, ensuring a chat based password management experience.

## Features

- **Secure Credential Backup**: Encrypts and stores credentials in a backup file.
- **Password-Protected Imports**: Users can import encrypted backup files by providing the correct password.
- **Session-Based Local Backup**: Option to enable/disable local storage of backups within the bot.
- **Bot Interaction**: Users interact via text commands and buttons.

## How Secure Is It?
- **Encryption**: Backup files are encrypted zip with AES 256 with a unique 16 alphanumeric key randomly generated for each backup request.
- **No Permanent Storage**: The encrypted backup (containing the credentials) is not stored permanently unless the local backup option is enabled.
- **User-Controlled Security**: The encryption key is provided separately, ensuring only the user has access to their backup.
- **Session-Based Access**: Operations require an active session to prevent unauthorized access.

The bot serves your password in a telegram chat, this means that it is as secure as Telegram, do not trust it if you don't trust Telegram. Unfortunately Telegram does not allow E2E encryption (secret chats) with bots, this means that 
- your messages, hence passwords are encrypted by Telegram servers
- Telegram potentially has a way to access your messages' content

Use this bot at your own risk and keeping in mind what is written above.

## Architecture

The Bot main class is the implementation of "KeyKeeper".
The bot is always in a state called "life stage"

**Bot Life Stages**: The bot current state is called Life Stage, any life stage is an extension of `AbstractBotLifeStage`
   - `Binding a user id`: This bot life stage is designed to interact with a single user, which must be bound at the first start
   - `Serving credentials`: The main life stage, the bot expects any URL as message and will return the related credentials (if present)
   - `Add new credentials`: In this life stage the bot register new credentials
   - `BackUpLifeStage`: Manages backup and restore functionalities
   - `Restoring session life stage`:  the bot works by default in a stateless way, in case of reboots you will have to provide the backup file with the related password. If the stateful mode is enabled, in case of reboots the bot tries to import the locally stored backup, you will have to send it the right password (shared with you previously by the bot itself)

if more actions are required, this more likely means defining a new life stage and a way to start it. 
To start a life stage you can call the method advanceBotLifeStage passing the next desired life stage; when to call this method? when the need arise, for instance receiving a message that should change life stage

The logic used during lifestages to interact with the model is exposed via services, the main ones are

**Services**
   - `BackUpService`: Handles encryption, decryption, and file validation.
   - `CredentialsService`: Manages credential storage and retrieval.
   - `SessionService`: Tracks session status and backup preferences.

### Workflow

1. User interacts with the bot via commands or buttons.
2. Depending on the request:
   - Backup is generated and sent to the user.
   - If local backup is enabled, a copy is stored.
   - User can send an encrypted backup file and enter a password to restore credentials.
3. The bot validates and processes the input accordingly.

## Setup & Usage

### Prerequisites
- docker and docker compose installed
- Telegram session (anywhere, pc, smartphone)

### Installation

- clone this repo
- cd into the root (KeyKeeper)
- build and run the container: docker compose up -d --build
  - to run it again later on, remove the build option: docker compose up -d

### First usage
the first time you start the bot you will need to perform 2 actions
- provide a telegram bot token (generate your own bot using the bot father in telegram)
  - to do so, the container exposes a POST service
    - curl -X POST http://localhost:9130/botToken -d "token=YOUR_BOT_TOKEN"
  - once the token is saved, the bot enters a binding stage, it expects to be linked to a telegram account (yours)
    - get the binding key (it is just a string of text)
      - curl -X GET http://localhost:9130/bindingKey?token=YOUR_BOT_TOKEN
    - from your telegram account, look for the bot (you know the name, you created it with the bot father)
      - send the binding key to the bot via chat, this will bind it to you
  - once the binding is done, it won't be necessary to do it again (unless the bot is reinstalled somewhere else)

### Backing up
All actions are done via Telegram, back-ups included. When selecting the back-up option, the bot will share two messages
- an encrypted file containing the credentials
- a password to unencrypt (the bot will not memorize it)

Keep these safe somewhere (for example, forward to yourself), whenever the bot is reinitialized, you can send it back the encrypted file and the password to get back all the credentials (this means that you can install it on a server that dies and reinstall it elsewhere without losing the credentials, provided that you performed regular backups)

## License
This project is licensed under the MIT License. See `LICENSE` for details.

## Contributing
Pull requests are welcome! For major changes, please open an issue first to discuss what you’d like to change.

