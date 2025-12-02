package com.pixlelabs.myapplication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow

class LatestViewModel : ViewModel() {

    fun getNotifications(context: Context, query: String, args: Array<String>?): Flow<PagingData<NotificationModel>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { NotificationDB.NotifPagingSource(context, query, args) }
        ).flow.cachedIn(viewModelScope)
    }

    fun getFavorites(context: Context): Flow<PagingData<NotificationModel>> {
        val query = "SELECT * FROM notifications WHERE is_favorite = 1 ORDER BY time DESC"
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { NotificationDB.NotifPagingSource(context, query, null) }
        ).flow.cachedIn(viewModelScope)
    }
}
