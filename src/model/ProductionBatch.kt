package model

import generator.IDGenerator
import model.enum.Location
import java.time.LocalDate

// The purpose of ProductionBatch is to represent one real-world production run of a product made in the same factory, same manufacturing date, fixed quantity. This is what physically arrives at a slot. Expiry is stored directly after calculating (null in cases where it does not apply).
data class ProductionBatch(
    val productId: String,
    val manufacturingLocation: Location,
    val manufacturingDate: LocalDate,
    val expiryDate: LocalDate?,
    var quantity: Int
) {
    // Format: place-year-month-date-batch-numerical
    val batchId: String =
        "${manufacturingLocation}-${manufacturingDate}-${IDGenerator.generateBatchId()}"

    init {
        require(productId.isNotBlank())                          { "Product ID cannot be blank" }
        require(!manufacturingDate.isAfter(LocalDate.now()))     { "Manufacturing date cannot be in the future" }
        require(quantity > 0)                                    { "Quantity must be greater than zero" }
        expiryDate?.let {
            require(it.isAfter(manufacturingDate))               { "Expiry date must be after manufacturing date" }
        }
    }

    // Why? Convenience check used by service layer
    fun isExpired(asOf: LocalDate = LocalDate.now()): Boolean =
        expiryDate != null && expiryDate.isBefore(asOf)

    override fun toString(): String =
        "BatchID: $batchId | Product: $productId | " +
                "MFD: $manufacturingDate | Expiry: ${expiryDate ?: "N/A"} | " +
                "Origin: $manufacturingLocation | Qty: $quantity"
}