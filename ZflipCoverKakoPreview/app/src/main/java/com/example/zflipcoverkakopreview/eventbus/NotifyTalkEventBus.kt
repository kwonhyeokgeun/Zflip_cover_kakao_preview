package com.example.zflipcoverkakopreview.eventbus

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object NotifyTalkEventBus {
    private val _notifyEvents = MutableSharedFlow<TalkInfo>() // private mutable shared flow
    val notifyEvents = _notifyEvents.asSharedFlow()

    suspend fun notifyTalkChanged(talkInfo : TalkInfo) {
        _notifyEvents.emit(talkInfo) //전송할 데이터 없어서 Unit
    }
}