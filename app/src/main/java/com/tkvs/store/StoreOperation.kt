package com.tkvs.store

interface StoreOperation<K, V> {
    fun set(key: K, value: V)
    fun get(key: K): V?
    fun delete(key: K): V?
    fun count(value: V): Int
}