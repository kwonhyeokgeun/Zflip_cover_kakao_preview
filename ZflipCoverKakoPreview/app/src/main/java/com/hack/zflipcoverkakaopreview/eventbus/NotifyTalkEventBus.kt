package com.hack.zflipcoverkakopreview.eventbus

import com.hack.zflipcoverkakopreview.db.entity.TalkItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object NotifyTalkEventBus {
    private val _notifyEvents = MutableSharedFlow<TalkItem>()
    val notifyEvents = _notifyEvents.asSharedFlow()

    suspend fun notifyTalkChanged(talk : TalkItem) {
        _notifyEvents.emit(talk)
    }
}