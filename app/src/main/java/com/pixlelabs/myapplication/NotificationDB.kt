package com.pixlelabs.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.paging.PagingSource
import androidx.paging.PagingState

object NotificationDB {

    private const val DB_NAME = "notif.db"
    private const val DB_VERSION = 5
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
            put("is_favorite", 0)
        }
        db(context).insert(TABLE, null, values)
    }

    // The PagingSource for notifications
    class NotifPagingSource(private val context: Context, private val sqlQuery: String, private val selectionArgs: Array<String>?) : PagingSource<Int, NotificationModel>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NotificationModel> {
            val pageNumber = params.key ?: 0
            val pageSize = params.loadSize
            val offset = pageNumber * pageSize

            return try {
                val cursor = db(context).rawQuery("$sqlQuery LIMIT $pageSize OFFSET $offset", selectionArgs)
                val notifications = mutableListOf<NotificationModel>()
                cursor.use {
                    while (it.moveToNext()) {
                        val id = it.getInt(it.getColumnIndexOrThrow("id"))
                        val pkg = it.getString(it.getColumnIndexOrThrow("pkg"))
                        val title = it.getString(it.getColumnIndexOrThrow("title"))
                        val text = it.getString(it.getColumnIndexOrThrow("text"))
                        val time = it.getLong(it.getColumnIndexOrThrow("time"))
                        val image = it.getBlob(it.getColumnIndexOrThrow("image"))
                        val appIcon = it.getBlob(it.getColumnIndexOrThrow("app_icon"))
                        val isFavorite = it.getInt(it.getColumnIndexOrThrow("is_favorite")) == 1
                        notifications.add(NotificationModel(id, pkg, title, text, time, image, null, appIcon, isFavorite))
                    }
                }
                LoadResult.Page(
                    data = notifications,
                    prevKey = if (pageNumber == 0) null else pageNumber - 1,
                    nextKey = if (notifications.isEmpty()) null else pageNumber + 1
                )
            } catch (e: Exception) {
                LoadResult.Error(e)
            }
        }

        override fun getRefreshKey(state: PagingState<Int, NotificationModel>): Int? {
            return state.anchorPosition?.let {
                state.closestPageToPosition(it)?.prevKey?.plus(1) ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
            }
        }
    }

    fun getFavorites(context: Context): Cursor {
        return db(context).rawQuery(
            "SELECT * FROM $TABLE WHERE is_favorite = 1 ORDER BY time DESC",
            null
        )
    }

    fun setFavorite(context: Context, id: Int, isFavorite: Boolean) {
        val values = ContentValues().apply {
            put("is_favorite", if (isFavorite) 1 else 0)
        }
        db(context).update(TABLE, values, "id = ?", arrayOf(id.toString()))
    }

    fun delete(context: Context, ids: List<Int>) {
        if (ids.isEmpty()) return
        val args = ids.map { it.toString() }.toTypedArray()
        val placeholders = ids.map { "?" }.joinToString()
        db(context).delete(TABLE, "id IN ($placeholders)", args)
    }

    fun deleteByPackage(context: Context, packageNames: List<String>) {
        if (packageNames.isEmpty()) return
        val args = packageNames.toTypedArray()
        val placeholders = packageNames.map { "?" }.joinToString()
        db(context).delete(TABLE, "pkg IN ($placeholders)", args)
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
                    app_icon BLOB,
                    is_favorite INTEGER DEFAULT 0
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
            if (oldVersion < 5) {
                db.execSQL("ALTER TABLE $TABLE ADD COLUMN is_favorite INTEGER DEFAULT 0")
            }
        }
    }
}
