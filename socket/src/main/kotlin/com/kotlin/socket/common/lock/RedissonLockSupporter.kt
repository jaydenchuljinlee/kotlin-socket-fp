package com.kotlin.socket.common.lock

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RedissonLockSupporter(
    private val redissonClient: RedissonClient
): LockSupporter {
    override fun <T> withLock(lockKey: String, action: () -> T): T {
        val lock = redissonClient.getLock(lockKey)
        try {
            val acquired = lock.tryLock(3, 10, TimeUnit.SECONDS)
            if (!acquired) throw IllegalStateException("Lock timeout for key: $lockKey")
            return action()
        } finally {
            if (lock.isHeldByCurrentThread) lock.unlock()
        }
    }

    override suspend fun <T> withLockSuspend(lockKey: String, action: suspend () -> T): T {
        return withContext(Dispatchers.IO) {
            val lock = redissonClient.getLock(lockKey)
            try {
                val acquired = lock.tryLock(3, 10, TimeUnit.SECONDS)
                if (!acquired) throw IllegalStateException("Lock timeout for key: $lockKey")
                action()
            } finally {
                if (lock.isHeldByCurrentThread) lock.unlock()
            }
        }
    }

}