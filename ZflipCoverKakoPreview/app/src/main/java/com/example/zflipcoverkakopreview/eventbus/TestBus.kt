package com.example.zflipcoverkakopreview.eventbus

import android.graphics.Bitmap
import android.graphics.drawable.Icon
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object TestBus {

    private val _notifyEvents = MutableSharedFlow<Icon>()
    val notifyEvents = _notifyEvents.asSharedFlow()

    suspend fun imageChange(bitmap : Icon) {
        _notifyEvents.emit(bitmap)

    }
}