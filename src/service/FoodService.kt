package service

import model.Food
import model.enum.FoodType
import model.enum.VegNonVeg
import repository.FoodRepository
import java.math.BigDecimal

object FoodService : BaseProductService<Food>() {

    fun registerFood(
        productName: String, brand: String, description: String,
        warning: String?, price: BigDecimal,
        vegOrNonVeg: VegNonVeg, ingredients: List<String>,
        shelfLifeMonths: Int, foodType: FoodType
    ): Food {
        val food = Food(
            productName = productName,
            brand = brand,
            description = description,
            price = price,
            vegOrNonVeg = vegOrNonVeg,
            ingredients = ingredients.toMutableList(),
            shelfLifeMonths = shelfLifeMonths,
            foodType = foodType,
            warning = warning
        )
        FoodRepository.add(food)
        return food
    }

    override fun getById(productId: String): Food = FoodRepository.findById(productId)
    override fun getAllProducts(): Set<Food> = FoodRepository.findAll()
}