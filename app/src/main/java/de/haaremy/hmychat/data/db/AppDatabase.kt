package de.haaremy.hmychat.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import de.haaremy.hmychat.data.db.dao.ChatDao
import de.haaremy.hmychat.data.db.dao.MessageDao
import de.haaremy.hmychat.data.db.entity.ChatEntity
import de.haaremy.hmychat.data.db.entity.MessageEntity
import de.haaremy.hmychat.data.db.entity.UserEntity

@Database(
    entities = [ChatEntity::class, MessageEntity::class, UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
}
