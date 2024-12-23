package com.daniminch.minibank.app.utils

import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

class KeyLockManager<K> {

  private val lockMap = ConcurrentHashMap<K, ReentrantLock>()
  private val logger = KotlinLogging.logger {}

  fun lock(key: K) {
    val lock = lockMap.computeIfAbsent(key) { ReentrantLock() }
    lock.lock()
  }

  fun unlock(key: K) {
    val lock = lockMap[key]
    // TODO singlethread bottleneck
    if (lock != null && lock.isHeldByCurrentThread) {
      lock.unlock()
      // Optionally clean up unused locks
      if (!lock.isLocked) {
        lockMap.remove(key, lock)
      }
    }
  }

  inline fun <T> locked(key: K, action: () -> T): T {
    lock(key)
    try {
      return action()
    } finally {
      unlock(key)
    }
  }

  fun hasLock(key: K): Boolean {
    val lock = lockMap[key]
    return lock != null && lock.isHeldByCurrentThread
  }
}