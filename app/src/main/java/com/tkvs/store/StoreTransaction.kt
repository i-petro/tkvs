package com.tkvs.store

import java.io.Closeable

interface StoreTransaction<K, V> : StoreOperation<K, V>, Closeable {
    fun commit()
    fun rollback()
    fun beginNestedTransaction(): StoreTransaction<K, V>

    override fun close() = commit()
}