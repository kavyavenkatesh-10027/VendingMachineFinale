package service

import model.Product
import repository.ProductRepository

abstract class BaseProductService<T : Product> {

    abstract fun getById(productId: String): T
    abstract fun getAllProducts(): Set<T>

    companion object {
        fun getProductById(productId: String): Product = ProductRepository.findById(productId)
    }
}