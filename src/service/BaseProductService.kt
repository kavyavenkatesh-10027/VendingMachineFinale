package service

import exception.UnknownEntityException
import model.Product
import repository.ElectronicsRepository
import repository.FoodRepository

abstract class BaseProductService<T : Product> {

    abstract fun getById(productId: String): T
    abstract fun getAllProducts(): Set<T>

    companion object {
        fun getProductById(productId: String): Product {
            if (FoodRepository.existsById(productId)){
                return FoodRepository.findById(productId)
            }else if (ElectronicsRepository.existsById(productId)){
                return ElectronicsRepository.findById(productId)
            }
            throw UnknownEntityException(productId)
        }

        fun productExistsById(productId: String): Boolean {
            return FoodRepository.existsById(productId) || ElectronicsRepository.existsById(productId)
        }
    }
}