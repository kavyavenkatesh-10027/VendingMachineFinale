package repository

import model.Food

object FoodRepository : BaseRepository<Food>() {
    override fun getId(entity: Food) = entity.productId
}