package repository

import model.Electronics

object ElectronicsRepository : BaseRepository<Electronics>() {
    override fun getId(entity: Electronics) = entity.productId
}