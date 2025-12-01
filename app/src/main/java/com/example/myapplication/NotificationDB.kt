package com.example.myapplication

import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

object NotificationDB {

    private const val DB_NAME = "notif.db"
    private const val DB_VERSION = 3
    private const val TABLE = "notifications"

    private fun db(context: Context): SQLiteDatabase {
        return DBHelper(context).writableDatabase
    }

    // NOTE: Removed PendingIntent from this method to fix the crash.
    fun save(context: Context, pkg: String, title: String?, text: String?, time: Long, image: ByteArray?) {
        val values = ContentValues().apply {
            put("pkg", pkg)
            put("title", title ?: "")
            put("text", text ?: "")
            put("time", time)
            put("image", image)
        }
        db(context).insert(TABLE, null, values)
    }

    fun getAll(context: Context): Cursor {
        return db(context).rawQuery(
            "SELECT * FROM $TABLE ORDER BY time DESC",
            null
        )
    }

    fun getByPackage(context: Context, pkg: String): Cursor {
        return db(context).rawQuery(
            "SELECT * FROM $TABLE WHERE pkg = ? ORDER BY time DESC",
            arrayOf(pkg)
        )
    }

    fun deleteAll(context: Context) {
        db(context).execSQL("DELETE FROM $TABLE")
    }

    private class DBHelper(context: Context) :
        SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS $TABLE (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    pkg TEXT,
                    title TEXT,
                    text TEXT,
                    time LONG,
                    pending_intent BLOB,
                    image BLOB
                )
                """.trimIndent()
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            if (oldVersion < 2) {
                db.execSQL("ALTER TABLE $TABLE ADD COLUMN pending_intent BLOB")
            }
            if (oldVersion < 3) {
                db.execSQL("ALTER TABLE $TABLE ADD COLUMN image BLOB")
            }
        }
    }
}
