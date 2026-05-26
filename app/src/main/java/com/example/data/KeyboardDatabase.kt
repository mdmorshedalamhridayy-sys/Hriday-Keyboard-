package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [CustomEmoji::class, CustomShortcut::class, KeyboardConfig::class], version = 1, exportSchema = false)
abstract class KeyboardDatabase : RoomDatabase() {
    abstract fun keyboardDao(): KeyboardDao

    companion object {
        @Volatile
        private var INSTANCE: KeyboardDatabase? = null

        fun getDatabase(context: Context): KeyboardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KeyboardDatabase::class.java,
                    "hriday_keyboard_db"
                ).addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default values in a background coroutine
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getDatabase(context).keyboardDao()
                            
                            // 1. Seed cool custom text style emojis
                            val defaultEmojis = listOf(
                                "(*^▽^*)", 
                                "(｡♥‿♥｡)", 
                                "( ͡° ͜ʖ ͡°)", 
                                "¯\\_(ツ)_/¯", 
                                "✨❤️🔥", 
                                "🚀💖😎",
                                "🎮👾💻", 
                                "☕🍰🌻",
                                "(╯°□°）╯︵ ┻━┻",
                                "＼(≧▽≦)／"
                            )
                            defaultEmojis.forEach {
                                dao.insertEmoji(CustomEmoji(emoji = it))
                            }

                            // 2. Seed helpful template shortcuts
                            val defaultShortcuts = listOf(
                                CustomShortcut(keyword = "btw", phrase = "by the way"),
                                CustomShortcut(keyword = "hriday", phrase = "Hriday Keyboard is awesome! ❤️"),
                                CustomShortcut(keyword = "hru", phrase = "how are you?"),
                                CustomShortcut(keyword = "ty", phrase = "Thank you so much!"),
                                CustomShortcut(keyword = "gm", phrase = "Good morning! ☀️"),
                                CustomShortcut(keyword = "gn", phrase = "Good night! 🌙"),
                                CustomShortcut(keyword = "omw", phrase = "On my way! 🏃")
                            )
                            defaultShortcuts.forEach {
                                dao.insertShortcut(it)
                            }

                            // 3. Seed default config
                            dao.saveConfig(KeyboardConfig())
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
