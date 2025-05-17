package com.kotlin.socket.common.lock

interface LockSupporter {
    fun <T> withLock(lockKey: String, action: () -> T): T
    suspend fun <T> withLockSuspend(lockKey: String, action: suspend () -> T): T
}