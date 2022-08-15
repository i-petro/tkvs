package com.tkvs.store.impl

import com.tkvs.store.StoreTransaction
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class TransactionImpl<K : Any, V : Any>(
    private val parent: StoreTransaction<K, V>
) : StoreTransaction<K, V> {
    private var isProcessed = false
    private val transactionMemory = ConcurrentHashMap<K, Optional<V>>()
    private val deletedValues = LinkedList<V>()

    override fun set(key: K, value: V) {
        assert(!isProcessed) { "unable to set value using committed or cancelled transaction" }
        parent.get(key)
            ?.takeIf { it != value }
            ?.let(deletedValues::add)
        transactionMemory[key] = Optional.ofNullable(value)
    }

    override fun get(key: K): V? {
        assert(!isProcessed) { "unable to get value using committed or cancelled transaction" }

        return when {
            transactionMemory.containsKey(key) -> transactionMemory[key]?.orElse(null)
            else -> parent.get(key)
        }
    }

    override fun delete(key: K): V? {
        assert(!isProcessed) { "unable to delete value using committed or cancelled transaction" }
        val valueOptional = transactionMemory[key] ?: Optional.empty()
        transactionMemory[key] = Optional.empty()
        if (!valueOptional.isPresent) {
            parent.get(key)?.let(deletedValues::add)
        }
        return valueOptional.orElse(null)
    }

    override fun count(value: V): Int {
        assert(!isProcessed) { "unable to count values using committed or cancelled transaction" }
        return transactionMemory.count { it.value.orElse(null) == value } + parent.count(value) - deletedValues.count { it == value }
    }

    override fun commit() {
        assert(!isProcessed) { "unable to commit already committed or cancelled transaction" }
        isProcessed = true

        transactionMemory.forEach { (key, optionalValue) ->
            if (optionalValue.isPresent) {
                parent.set(key, optionalValue.get())
            } else parent.delete(key)
        }

        releaseResources()
    }

    override fun rollback() {
        assert(!isProcessed) { "unable to rollback already committed or cancelled transaction" }
        isProcessed = true
        releaseResources()
    }

    private fun releaseResources() {
        transactionMemory.clear()
        deletedValues.clear()
    }

    override fun beginNestedTransaction(): StoreTransaction<K, V> {
        assert(!isProcessed) { "unable to begin nested transaction inside of the already committed transaction" }
        return TransactionImpl(this)
    }
}