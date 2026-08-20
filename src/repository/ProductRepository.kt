package repository

import model.Product

object ProductRepository : BaseRepository<Product>() {
    override fun getId(entity: Product) = entity.productId

    fun getCategoryOf(productId: String) = findById(productId).productCategory
}