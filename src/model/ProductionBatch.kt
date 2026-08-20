package model

import generator.IDGenerator
import model.enum.Location
import java.time.LocalDate

// The purpose of ProductionBatch is to represent one real-world production run of a product —
// same factory, same manufacturing date, fixed quantity. This is what physically arrives at
// a slot. Expiry is stored directly (null for electronics which never expire).
data class ProductionBatch(
    val productId: String,
    val manufacturingLocation: Location,
    val manufacturingDate: LocalDate,
    val expiryDate: LocalDate?,          // null for Electronics, calculated from shelfLifeMonths for Food
    var quantity: Int
) {
    // Format: COIMBATORE-2025-07-15-batch-1
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

    // Why? Convenience check used by service layer — batch is expired if expiryDate is before today
    fun isExpired(asOf: LocalDate = LocalDate.now()): Boolean =
        expiryDate != null && expiryDate.isBefore(asOf)

    override fun toString(): String =
        "BatchID: $batchId | Product: $productId | " +
                "MFD: $manufacturingDate | Expiry: ${expiryDate ?: "N/A"} | " +
                "Origin: $manufacturingLocation | Qty: $quantity"
}