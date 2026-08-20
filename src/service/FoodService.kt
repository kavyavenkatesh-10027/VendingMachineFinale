package service

import model.Food
import model.ProductFactory
import model.enum.FoodType
import model.enum.VegNonVeg
import repository.FoodRepository
import repository.ProductRepository
import java.math.BigDecimal

object FoodService : BaseProductService<Food>() {

    fun registerFood(
        productName: String, brand: String, description: String,
        warning: String?, price: BigDecimal,
        vegOrNonVeg: VegNonVeg, ingredients: List<String>,
        shelfLifeMonths: Int, foodType: FoodType
    ): Food {
        val food = ProductFactory.createFood(
            productName, brand, description, warning, price,
            vegOrNonVeg, ingredients, shelfLifeMonths, foodType
        )
        ProductRepository.add(food)
        FoodRepository.add(food)
        return food
    }

    override fun getById(productId: String): Food = FoodRepository.findById(productId)
    override fun getAllProducts(): Set<Food> = FoodRepository.findAll()
}