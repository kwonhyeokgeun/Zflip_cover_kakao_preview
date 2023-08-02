package com.kun.zflipcoverkakopreview.eventbus

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object NotifyRoomEventBus {
    private val _notifyEvents = MutableSharedFlow<Unit>() // private mutable shared flow
    val notifyEvents = _notifyEvents.asSharedFlow()

    suspend fun notifyRoomChanged() {
        _notifyEvents.emit(Unit) //전송할 데이터 없어서 Unit
    }
}