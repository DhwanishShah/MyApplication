package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

object NotificationDB {

    private const val DB_NAME = "notif.db"
    private const val DB_VERSION = 4
    private const val TABLE = "notifications"

    private fun db(context: Context): SQLiteDatabase {
        return DBHelper(context).writableDatabase
    }

    fun save(
        context: Context, pkg: String, title: String?, text: String?, time: Long,
        image: ByteArray?, appName: String?, appIcon: ByteArray?
    ) {
        val values = ContentValues().apply {
            put("pkg", pkg)
            put("title", title ?: "")
            put("text", text ?: "")
            put("time", time)
            put("image", image)
            put("app_name", appName)
            put("app_icon", appIcon)
        }
        db(context).insert(TABLE, null, values)
    }

    fun getAll(context: Context): Cursor {
        return db(context).rawQuery(
            "SELECT * FROM $TABLE ORDER BY time DESC",
            null
        )
    }

    fun getSince(context: Context, time: Long): Cursor {
        return db(context).rawQuery(
            "SELECT * FROM $TABLE WHERE time >= ? ORDER BY time DESC",
            arrayOf(time.toString())
        )
    }

    fun getBetween(context: Context, startTime: Long, endTime: Long): Cursor {
        return db(context).rawQuery(
            "SELECT * FROM $TABLE WHERE time BETWEEN ? AND ? ORDER BY time DESC",
            arrayOf(startTime.toString(), endTime.toString())
        )
    }

    fun getDistinctApps(context: Context): Cursor {
        return db(context).rawQuery(
            "SELECT pkg, app_name, app_icon, MAX(time) AS latest_time FROM $TABLE WHERE app_name IS NOT NULL GROUP BY pkg ORDER BY latest_time DESC",
            null
        )
    }

    fun getByPackage(context: Context, pkg: String): Cursor {
        return db(context).rawQuery(
            "SELECT * FROM $TABLE WHERE pkg = ? ORDER BY time DESC",
            arrayOf(pkg)
        )
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
                    image BLOB,
                    app_name TEXT,
                    app_icon BLOB
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
            if (oldVersion < 4) {
                db.execSQL("ALTER TABLE $TABLE ADD COLUMN app_name TEXT")
                db.execSQL("ALTER TABLE $TABLE ADD COLUMN app_icon BLOB")
            }
        }
    }
}
