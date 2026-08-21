package model

import model.enum.FoodType
import model.enum.VegNonVeg
import java.math.BigDecimal
import java.time.LocalDate

// The purpose of Food is to represent a food product. Shelf life is stored here so the service layer can calculate expiry date for any production batch of this product.
class Food(
    productName: String,
    brand: String,
    description: String,
    price: BigDecimal,
    val vegOrNonVeg: VegNonVeg,
    private val ingredients: MutableList<String>,
    val shelfLifeMonths: Int,
    val foodType: FoodType,
    warning: String? = null
) : Product(
    productName = productName,
    brand = brand,
    description = description,
    price = price,
    productCategory = model.enum.ProductCategory.FOOD,
    warning = warning
) {
    init {
        require(ingredients.isNotEmpty())   { "Ingredients must be provided" }
        require(shelfLifeMonths > 0)        { "Shelf life must be greater than zero" }
    }

    // Why? Called by service layer when creating a CommonValuesBatch, so that expiry is calculated once at stocking time and stored on the batch itself.
    fun calculateExpiryDate(manufacturingDate: LocalDate): LocalDate =
        manufacturingDate.plusMonths(shelfLifeMonths.toLong())

    fun getIngredients(): List<String> = ingredients.toList()

    override fun toString(): String =
        super.toString() + "\n" +
                """
Food Type               : $foodType
Veg/Non-Veg             : $vegOrNonVeg
Ingredients             : ${ingredients.joinToString(", ")}
Shelf Life              : $shelfLifeMonths month(s) from manufacturing date
        """.trimIndent()
}