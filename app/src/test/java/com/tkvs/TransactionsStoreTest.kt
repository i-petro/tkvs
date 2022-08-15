package com.tkvs

import com.tkvs.store.Store
import com.tkvs.store.impl.TransactionalStoreImpl
import com.tkvs.store.runInTransaction
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionsStoreTest {
    private val testable: Store<String, String> = TransactionalStoreImpl()

    @Test
    fun `should keep changes inside of transaction until committed`() {
        // arrange
        testable.set("foo", "bar")
        testable.set("bim", "bom")

        // act
        val transaction = testable.begin()
        transaction.set("foo", "new bar")
        transaction.delete("bim")

        // assert
        assertEquals("bar", testable.get("foo"))
        assertEquals("bom", testable.get("bim"))
        assertEquals("new bar", transaction.get("foo"))
        assertEquals(null, transaction.get("bim"))

        // act
        transaction.commit()

        // assert
        assertEquals("new bar", testable.get("foo"))
        assertEquals(null, testable.get("bim"))
    }

    @Test
    fun `should rollback transaction`() {
        // arrange
        testable.set("foo", "bar")
        val transaction = testable.begin()
        transaction.set("foo", "new bar")

        // act
        transaction.rollback()

        // assert
        assertEquals("bar", testable.get("foo"))
    }

    @Test
    fun `should support nested transaction`() {
        testable.runInTransaction {
            // inside of transaction1

            // arrange
            set("foo", "bar")

            // act
            val transaction2 = beginNestedTransaction()
            transaction2.set("foo", "new bar")

            // assert
            assertEquals("new bar", transaction2.get("foo"))
            assertEquals("bar", get("foo"))
            assertEquals(null, testable.get("foo"))

            // act
            transaction2.commit()

            // assert data moved from transaction2 to transaction1
            assertEquals("new bar", get("foo"))
            assertEquals(null, testable.get("foo"))
        }

        assertEquals("new bar", testable.get("foo"))
    }

    @Test
    fun `should return value from parent transaction`() {
        // arrange
        testable.set("foo", "bar")

        val transaction1 = testable.begin()

        // act
        var value = transaction1.get("foo")

        // assert
        assertEquals("bar", value)

        // arrange
        transaction1.set("foo", "new bar")
        val transaction2 = transaction1.beginNestedTransaction()

        // act
        value = transaction2.get("foo")

        // assert
        assertEquals("new bar", value)

        // arrange
        transaction2.delete("foo")

        // act
        value = transaction2.get("foo")

        // assert
        assertEquals(null, value)
    }

    @Test
    fun `should return null if value is deleted in the new transaction`() {
        // arrange
        testable.set("foo", "bar")

        // act
        testable.runInTransaction {
            delete("foo")

            // assert
            assertEquals(null, get("foo"))
            assertEquals(0, count("bar"))
            assertEquals("bar", testable.get("foo"))
            assertEquals(1, testable.count("bar"))
        }

        // assert
        assertEquals(null, testable.get("foo"))
        assertEquals(0, testable.count("bar"))
    }

    @Test
    fun `should count values count for nested transactions`() {
        // arrange
        testable.runInTransaction {
            assertEquals(0, count("bar"))

            // act
            set("foo1", "bar")
            set("foo2", "bar")

            // assert
            assertEquals(2, count("bar"))

            runInTransaction {
                // act
                set("foo3", "bar")

                // assert
                assertEquals(3, count("bar"))

                // act
                delete("foo2")

                // assert
                assertEquals(2, count("bar"))

                // act
                delete("foo3")

                // assert
                assertEquals(1, count("bar"))

                // act
                delete("foo1")

                // assert
                assertEquals(0, count("bar"))
            }
        }
    }

    // TODO
    @Test
    fun `should not count value if new transaction overrides it`() {
        testable.runInTransaction {
            set("foo1", "bar")
            set("foo2", "bar")

            assertEquals(2, count("bar"))

            runInTransaction {
                set("foo2", "not-bar")

                assertEquals(1, count("bar"))
            }
        }
    }
}