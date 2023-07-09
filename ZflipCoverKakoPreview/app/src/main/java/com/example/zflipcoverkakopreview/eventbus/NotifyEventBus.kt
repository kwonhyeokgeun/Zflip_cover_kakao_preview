package com.example.zflipcoverkakopreview.eventbus

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object NotifyEventBus {
    private val _notifyEvents = MutableSharedFlow<Unit>() // private mutable shared flow
    val notifyEvents = _notifyEvents.asSharedFlow()

    suspend fun notifyTalkChanged() {
        _notifyEvents.emit(Unit) //전송할 데이터 없어서 Unit
    }
}