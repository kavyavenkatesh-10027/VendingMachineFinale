package model

import exception.AvailabilityRequirementException
import exception.UnregisteredEntityException
import generator.IDGenerator

// The purpose of Slot is to represent one physical rack inside a vending machine.
class Slot(
    val vendingMachineId: String,
    private val batches: MutableList<CommonValuesBatch> = mutableListOf()
) {
    val slotId: String = IDGenerator.generateSlotId()

    init {
        require(vendingMachineId.isNotBlank()) { "Vending machine ID cannot be blank" }
    }

    // Why? Read-only defensive copy so callers cannot mutate internal state
    fun getBatches(): List<CommonValuesBatch> = batches.toList()

    // Why? Returns the set of unique productIds currently in this slot
    fun getProductIds(): Set<String> = batches.map { it.productId }.toSet()

    // Why? Adds the very first batch of a product type that has never been in this slot before
    fun addNewProductTypeToSlot(batch: CommonValuesBatch) {
        require(!batch.isExpired()) {
            "Cannot stock an already-expired batch (${batch.batchId})"
        }
        if (batches.any { it.productId == batch.productId }) {
            throw IllegalArgumentException(
                "Product ${batch.productId} already exists in slot $slotId. Use refillSlot() instead."
            )
        }
        batches.add(batch)
    }

    // Why? Adds a new production batch to a product that already exists in this slot (refill)
    fun refillSlot(batch: CommonValuesBatch) {
        require(!batch.isExpired()) {
            "Cannot stock an already-expired batch (${batch.batchId})"
        }
        if (batches.none { it.productId == batch.productId }) {
            throw UnregisteredEntityException(
                "Product", batch.productId, "Slot", slotId,
                "Use addNewProductTypeToSlot() instead."
            )
        }
        batches.add(batch)
    }

    // Why? FIFO drain — always sell from the oldest manufacturing date first
    fun sellFromSlot(productId: String, quantity: Int) {
        require(quantity > 0) { "Quantity must be greater than zero" }

        val sellable = batches
            .filter { it.productId == productId && !it.isExpired() }
            .sortedBy { it.manufacturingDate }

        if (sellable.isEmpty()) {
            throw UnregisteredEntityException("Product", productId, "Slot", slotId)
        }

        val totalAvailable = sellable.sumOf { it.quantity }
        if (quantity > totalAvailable) {
            throw AvailabilityRequirementException(
                "Cannot sell $quantity of $productId — only $totalAvailable non-expired unit(s) in slot $slotId"
            )
        }

        var remaining = quantity
        for (batch in sellable) {
            if (remaining == 0) break
            val take = minOf(batch.quantity, remaining)
            batch.quantity -= take
            remaining -= take
        }
        batches.removeIf { it.quantity == 0 }
    }

    // Why? Total sellable (non-expired) units of a product in this slot
    fun getSellableQuantity(productId: String): Int =
        batches
            .filter { it.productId == productId && !it.isExpired() }
            .sumOf { it.quantity }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Slot) return false
        return slotId == other.slotId
    }

    override fun hashCode(): Int = slotId.hashCode()

    override fun toString(): String {
        val batchLines = batches
            .groupBy { it.productId }
            .entries
            .joinToString("\n    ") { (pid, pBatches) ->
                "$pid ->\n        " + pBatches
                    .sortedBy { it.manufacturingDate }
                    .joinToString("\n        ") { it.toString() }
            }
        return """
Slot ID                : $slotId
Vending Machine ID     : $vendingMachineId
Batches:
    $batchLines
        """.trimIndent()
    }
}