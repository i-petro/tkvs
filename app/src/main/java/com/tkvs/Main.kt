package com.tkvs

import com.tkvs.store.StoreTransaction
import com.tkvs.store.impl.TransactionalStoreImpl
import java.util.*

/**
 * @see Transactional Key Value Store
 * https://www.notion.so/Transactional-Key-Value-Store-d72f26aa31e34eef9aa7442507215ce7
 */
object Main {
    private const val COMMAND_SET = "SET"
    private const val COMMAND_GET = "GET"
    private const val COMMAND_DELETE = "DELETE"
    private const val COMMAND_COUNT = "COUNT"
    private const val COMMAND_BEGIN = "BEGIN"
    private const val COMMAND_COMMIT = "COMMIT"
    private const val COMMAND_ROLLBACK = "ROLLBACK"
    private const val COMMAND_END = "END"

    @JvmStatic
    fun main(vararg args: String) {
        val store = TransactionalStoreImpl<String, String>()
        val transactions = LinkedList<StoreTransaction<String, String>>()
        transactions.push(store.begin())

        val scanner = Scanner(System.`in`)

        do {
            println("""
            Type:
            SET <key> <value> to store the value for key
            GET <key> to return the current value for key
            DELETE <key> to remove the entry for key
            COUNT <value> to return the number of keys that have the given value
            BEGIN to start a new transaction
            COMMIT to complete the current transaction
            ROLLBACK to revert to state prior to BEGIN call
            END to finish the program
            >>> 
        """.trimIndent())
            val input = scanner.nextLine()
            val params = input.split(' ')
            val command = params.getOrNull(0)?.uppercase() ?: ""
            val param1 = params.getOrNull(1)
            val param2 = params.getOrNull(2)

            when (command){
                COMMAND_SET -> {
                    requireNotNull(param1)
                    requireNotNull(param2)
                    transactions.peek()!!.set(param1, param2)
                    println("Set \"$param1\" = \"$param2\"")
                }
                COMMAND_GET -> {
                    requireNotNull(param1)
                    val result = transactions.peek()!!.get(param1)
                    println("Value for key \"$param1\" = \"$result\"")
                }
                COMMAND_DELETE -> {
                    requireNotNull(param1)
                    val result = transactions.peek()!!.delete(param1)
                    println("Deleted the key \"$param1\" with value \"$result\"")
                }
                COMMAND_COUNT -> {
                    requireNotNull(param1)
                    val result = transactions.peek()!!.count(param1)
                    println("Count of keys with value \"$param1\" = $result")
                }
                COMMAND_BEGIN -> {
                    println("Starting transaction #${transactions.size}...")
                    transactions.push(transactions.peek()!!.beginNestedTransaction())
                }
                COMMAND_COMMIT -> {
                    println("Committing transaction #${transactions.size - 1}...")
                    transactions.pop().commit()
                }
                COMMAND_ROLLBACK -> {
                    println("Cancelling transaction #${transactions.size - 1}")
                    transactions.pop().rollback()
                }
                COMMAND_END -> {
                    println("Bye!")
                }
                else -> println("Invalid command")
            }

            println("==================")
        } while (command != COMMAND_END)
    }
}