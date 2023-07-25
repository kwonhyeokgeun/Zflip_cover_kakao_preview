package com.example.zflipcoverkakopreview.eventbus

import android.graphics.Bitmap
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object EventBusB {
    private val _notifyEvents = MutableSharedFlow<Bitmap>()
    val notifyEvents = _notifyEvents.asSharedFlow()

    suspend fun notifyTalkChanged(talk : Bitmap) {
        _notifyEvents.emit(talk)
    }
}