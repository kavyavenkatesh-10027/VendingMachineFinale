package repository

import exception.ExistsAlreadyException
import exception.UnknownEntityException

// The purpose of BaseRepository is to perform common CRUD data handling using generics.
abstract class BaseRepository<T : Any> {

    protected val store = mutableMapOf<String, T>()

    protected abstract fun getId(entity: T): String

    open fun add(entity: T) {
        val id = getId(entity)
        if (existsById(id)) throw ExistsAlreadyException("Entity '$id' already exists.")
        store[id] = entity
    }

    fun findById(id: String): T =
        store[id] ?: throw UnknownEntityException(id)

    fun findAll(): Set<T> = store.values.toSet()

    open fun removeById(id: String) {
        if (!existsById(id)) throw UnknownEntityException(id)
        store.remove(id)
    }

    fun existsById(id: String): Boolean = store.containsKey(id)
}