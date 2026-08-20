package model

import model.enum.ElectronicTypes
import model.enum.FoodType
import model.enum.VegNonVeg
import java.math.BigDecimal

// The purpose of ProductFactory is to centralize product construction.
object ProductFactory {

    fun createFood(
        productName: String,
        brand: String,
        description: String,
        warning: String?,
        price: BigDecimal,
        vegOrNonVeg: VegNonVeg,
        ingredients: List<String>,
        shelfLifeMonths: Int,
        foodType: FoodType
    ): Food = Food(
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

    fun createElectronics(
        productName: String,
        brand: String,
        description: String,
        warning: String?,
        price: BigDecimal,
        warrantyMonths: Int,
        batteryPowered: Boolean,
        electronicsType: ElectronicTypes
    ): Electronics = Electronics(
        productName = productName,
        brand = brand,
        description = description,
        price = price,
        warrantyMonths = warrantyMonths,
        batteryPowered = batteryPowered,
        electronicsType = electronicsType,
        warning = warning
    )
}