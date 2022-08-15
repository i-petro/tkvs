package com.tkvs.store

interface Store<K, V> : StoreOperation<K, V> {
    fun begin(): StoreTransaction<K, V>
}

fun <K, V, T> Store<K, V>.runInTransaction(body: StoreTransaction<K, V>.() -> T): T =
    begin().use(body)

fun <K, V, T> StoreTransaction<K, V>.runInTransaction(body: StoreTransaction<K, V>.() -> T): T =
    beginNestedTransaction().use(body)