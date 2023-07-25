package com.example.zflipcoverkakopreview.eventbus

import android.graphics.drawable.Icon
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object EventBusI {
    private val _notifyEvents = MutableSharedFlow<Icon>()
    val notifyEvents = _notifyEvents.asSharedFlow()

    suspend fun notifyTalkChanged(talk : Icon) {
        _notifyEvents.emit(talk)
    }
}