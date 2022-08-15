package com.tkvs

import com.tkvs.store.Store
import com.tkvs.store.impl.TransactionalStoreImpl
import org.junit.Assert.assertEquals
import org.junit.Test

class SimpleOpStoreTest {
    private val testable: Store<String, String> = TransactionalStoreImpl()

    @Test
    fun `should set and return value`() {
        // arrange

        // act
        testable.set("foo", "bar")

        // assert
        assertEquals("bar", testable.get("foo"))
    }

    @Test
    fun `should delete value`() {
        // arrange
        testable.set("foo", "bar")

        // act
        testable.delete("foo")

        // assert
        assertEquals(null, testable.get("foo"))
    }

    @Test
    fun `should return count of values`() {
        // arrange
        testable.set("foo1", "bar")
        testable.set("foo2", "bar")
        testable.set("foo3", "bar")

        // act
        val count = testable.count("bar")

        // assert
        assertEquals(3, count)

        // act
        testable.delete("foo2")

        // assert
        assertEquals(2, testable.count("bar"))

        // act
        testable.delete("foo1")
        testable.delete("foo3")

        // assert
        assertEquals(0, testable.count("bar"))
    }
}