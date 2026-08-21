package model

import model.enum.ElectronicTypes
import java.math.BigDecimal

// The purpose of Electronics is to represent an electronics product. Electronics never expire so CommonValuesBatch.expiryDate will always be null for these.
class Electronics(
    productName: String,
    brand: String,
    description: String,
    price: BigDecimal,
    val warrantyMonths: Int,
    val batteryPowered: Boolean,
    val electronicsType: ElectronicTypes,
    warning: String? = null
) : Product(
    productName = productName,
    brand = brand,
    description = description,
    price = price,
    productCategory = model.enum.ProductCategory.ELECTRONIC,
    warning = warning
) {
    init {
        require(warrantyMonths >= 0) { "Warranty months cannot be negative" }
    }

    override fun toString(): String =
        super.toString() + "\n" +
                """
Electronics Type        : $electronicsType
Warranty                : $warrantyMonths month(s)
Battery Powered         : $batteryPowered
        """.trimIndent()
}