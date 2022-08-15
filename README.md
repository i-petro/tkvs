# Transactional Key Value Store

## Task:
https://www.notion.so/Transactional-Key-Value-Store-d72f26aa31e34eef9aa7442507215ce7

## How to start the app:
Just run `com.tkvs.Main.main()` and follow the menu in your console. You can start it even in your Android Studio. No additional hassle is required.  

## Tests:
You can find unit tests in the tests folder

## Code sample
```kotlin
        val store = TransactionalStoreImpl<String, String>()
        
        store.set("foo", "bar")
        assert(store.get("foo") == "bar")
        
        assert(store.count("bar") == 1)
        
        store.delete("foo")
        assert(store.get("foo") == null)
        
        store.runInTransaction { // commits automatically
            set("foo1", "bar")
            set("foo2", "bar")
            
            // nested transaction
            runInTransaction { // commits automatically
                set("foo3", "bar")
                set("foo4", "bar")
            }
            
            val transaction = beginNestedTransaction()
            set("foo4", "won't be saved")
            transaction.rollback()
            // or
            transaction.commit()
        }
```
