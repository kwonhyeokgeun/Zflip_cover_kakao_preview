package com.example.zflipcoverkakopreview.eventbus

import com.example.zflipcoverkakopreview.db.entity.Talk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object NotifyTalkEventBus {
    private val _notifyEvents = MutableSharedFlow<Talk>()
    val notifyEvents = _notifyEvents.asSharedFlow()

    suspend fun notifyTalkChanged(talk : Talk) {
        _notifyEvents.emit(talk)
    }
}