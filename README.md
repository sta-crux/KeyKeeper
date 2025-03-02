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

### Components

1. **Bot Life Stages**: The bot current state is called Life Stage, any life stage is an extension of `AbstractBotLifeStage`
   - `Binding a user id`: This bot is designed to interact with a single user, which must be bound at the first start
   - `Serving credentials`: The main life stage, the bot expects any URL as message and will return the related credentials (if present)
   - `Add new credentials`: In this life stage the bot register new credentials 
   - `BackUpLifeStage`: Manages backup and restore functionalities
   - `Restoring session life stage`:  the bot works by default in a stateless way, in case of reboots you will have to provide the backup file with the related password. If the stateful mode is enabled, in case of reboots the bot tries to import the locally stored backup, you will have to send it the right password (shared with you previously by the bot itself)

3. **Services**
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
- Java 17+
- Telegram Bot Token

### Installation

1. Build the fat JAR with dependencies:
   ```sh
   ./gradlew shadowJar
   ```
2. Run the bot:
   ```sh
   java -jar build/libs/keykeeper-bot-all.jar
   ```
3. Deploy the bot on a server

### First usage
The first time it is started, the bot asks for a token; you can provide it via standard input. Alternatively, you can create a folder "keyKeeper" in your home folder, here create a file "botToken", no extension, containing just the token (this file is created automatically by the bot in case of token provided via standard input).
If the token is accepted, the bot will print a binding key in the console, send it via telegram to the bot to create a binding, from now on the bot will reply only to you.

## License
This project is licensed under the MIT License. See `LICENSE` for details.

## Contributing
Pull requests are welcome! For major changes, please open an issue first to discuss what you’d like to change.

