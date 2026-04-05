# hmyChat Android

Native Android client for hmyChat - an end-to-end encrypted messenger with WhatsApp x Discord features.

## Features

- Full chat functionality (send, edit, delete, reply, forward messages)
- 1:1 and group chats with unread badges
- Long-press context menu on messages
- OIDC/PKCE authentication via hmySSO
- Profile pictures with initials fallback
- Typing indicators and read receipts
- Smart polling for real-time updates
- Material3 dark/light theme

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material3
- **Architecture**: MVVM + Repository Pattern
- **DI**: Hilt
- **Networking**: Retrofit + OkHttp
- **Local DB**: Room
- **Auth**: AppAuth (OIDC/PKCE)
- **Image Loading**: Coil
- **Token Storage**: EncryptedSharedPreferences

## Setup

1. Open in Android Studio
2. Sync Gradle
3. Configure SSO credentials in `data/local/TokenStore.kt` if needed
4. Build and run on device/emulator (min SDK 26)

## Authentication Flow

1. User taps "Mit hmySSO anmelden"
2. AppAuth opens browser for SSO login (PKCE flow)
3. After login, redirect back to app (`de.haaremy.hmychat://auth/callback`)
4. App exchanges code for bearer token via `POST /api/auth/token`
5. Token stored in EncryptedSharedPreferences
6. All API requests include `Authorization: Bearer <token>` header

## Project Structure

```
app/src/main/java/de/haaremy/hmychat/
  di/                  # Hilt modules (Network, Database)
  data/
    api/               # Retrofit interfaces (ChatApi, KeyApi)
    api/models/        # API DTOs
    db/                # Room database, DAOs, entities
    repository/        # ChatRepository, AuthRepository
    local/             # TokenStore (EncryptedSharedPreferences)
  ui/
    auth/              # Login screen + ViewModel
    chatlist/          # Chat list screen + ViewModel
    chat/              # Chat screen + ViewModel
    chat/components/   # MessageBubble, UserAvatar, ContextMenu
    settings/          # Settings screen
    theme/             # Material3 theme, colors
    navigation/        # NavGraph
```

## Related Projects

- [hmyChat](https://github.com/Haaremy/hmyChat) - Web app (Next.js)
- [hmySSO](https://github.com/Haaremy/hmySSO) - OIDC identity provider
