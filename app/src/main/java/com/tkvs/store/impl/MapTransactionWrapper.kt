package com.tkvs.store.impl

import com.tkvs.store.StoreTransaction

class MapTransactionWrapper<K : Any, V : Any>(
    private val store: MutableMap<K, V>
) : StoreTransaction<K, V> {

    override fun set(key: K, value: V) {
        store[key] = value
    }

    override fun get(key: K): V? = store[key]
    override fun delete(key: K): V? = store.remove(key)
    override fun count(value: V): Int = store.count { it.value == value }

    override fun commit() = Unit
    override fun rollback() = Unit

    override fun beginNestedTransaction(): StoreTransaction<K, V> = TransactionImpl(this)
}