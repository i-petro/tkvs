package com.tkvs.store.impl

import com.tkvs.store.Store
import com.tkvs.store.StoreTransaction
import com.tkvs.store.runInTransaction
import java.util.concurrent.ConcurrentHashMap

class TransactionalStoreImpl<K : Any, V : Any> : Store<K, V> {
    private val store = ConcurrentHashMap<K, V>()
    private val lastTransaction = MapTransactionWrapper(store)

    override fun begin(): StoreTransaction<K, V> = TransactionImpl(lastTransaction)

    override fun set(key: K, value: V) = runInTransaction { set(key, value) }
    override fun get(key: K): V? = runInTransaction { get(key) }
    override fun delete(key: K) = runInTransaction { delete(key) }
    override fun count(value: V): Int = runInTransaction { count(value) }
}