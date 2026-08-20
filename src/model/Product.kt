package model

import generator.IDGenerator
import model.enum.ProductCategory
import java.math.BigDecimal

// The purpose of Product is to represent any product sold in a vending machine.
// Sealed to make use when function which is offered by enum too, but so it can have its own entity and varying properties.
sealed class Product(
    val productName: String,
    val brand: String,
    val description: String,
    val price: BigDecimal,
    val productCategory: ProductCategory,
    val warning: String? = null
) {
    val productId: String = IDGenerator.generateProductId()

    init {
        require(productName.isNotBlank())       { "Product must have a name" }
        require(brand.isNotBlank())             { "Product must have a brand" }
        require(description.isNotBlank())       { "Product must have a description" }
        require(price > BigDecimal.ZERO)        { "Price must be positive" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Product) return false
        return productId == other.productId
    }

    override fun hashCode(): Int = productId.hashCode()

    override fun toString(): String =
        """
Product ID              : $productId
Name                    : $productName
Category                : $productCategory
Brand                   : $brand
Description             : $description
Price                   : ₹$price
Warning                 : ${warning ?: "None"}
        """.trimIndent()
}