package model

import controller.AdminController
import exception.AvailabilityRequirementException
import exception.UnregisteredEntityException
import generator.IDGenerator

class Slot(
    val vendingMachineId: String,
    private val batches: MutableList<CommonValuesBatch> = mutableListOf()
) {
    val slotId: String = IDGenerator.generateSlotId()

    init {
        require(vendingMachineId.isNotBlank()) { "Vending machine ID cannot be blank" }
    }

    fun getBatches(): List<CommonValuesBatch> = batches.toList()

    fun getProductIds(): Set<String> = batches.map { it.productId }.toSet()

    fun addNewProductTypeToSlot(batch: CommonValuesBatch) {
        require(!batch.isExpired()) {
            "Cannot stock an already-expired batch (${batch.batchId})"
        }
        if (batches.any { it.productId == batch.productId }) {
            throw IllegalArgumentException(
                "Product ${batch.productId} already exists in slot $slotId."
            )
        }
        batches.add(batch)
    }

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
    }

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
        val totalQty = batches.sumOf { it.quantity }
        val width = 82
        val innerWidth = width - 4

        val content = if (batches.isEmpty()) {
            "  [ EMPTY SLOT ] No batches currently stocked.".padEnd(width - 1)
        } else {
            batches.groupBy { it.productId }.entries.joinToString("\n".padEnd(width - 1) + "\n") { (pid, pBatches) ->
                val productName = try {
                    AdminController.getProductById(pid).productName
                } catch (_: Exception) {
                    "Unknown Product"
                }
                val totalProductQty = pBatches.sumOf { it.quantity }

                val headerText = "$productName [$pid] (Total: $totalProductQty units)"
                val headerLine = headerText.padEnd(innerWidth)

                val batchLines = pBatches.sortedBy { it.manufacturingDate }.joinToString("\n") { b ->
                    val exp = b.expiryDate?.toString() ?: "N/A"
                    val status = if (b.isExpired()) "EXPIRED" else "OK"
                    val rowText = "   ├─ Batch: %-12s │ MFD: %-10s │ Exp: %-14s │ Qty: %-3d [%s]".format(
                        b.batchId, b.manufacturingDate, exp, b.quantity, status
                    )
                    rowText.padEnd(innerWidth)
                }
                "$headerLine\n$batchLines"
            }
        }

        val slotLine = "  Slot ID            : $slotId".padEnd(width - 1)
        val vmLine   = "  Vending Machine ID : $vendingMachineId".padEnd(width - 1)
        val qtyLine  = "  Total Units        : $totalQty".padEnd(width - 1)

        return """

  SLOT DETAILS${"".padEnd(innerWidth - 12)} 

$slotLine
$vmLine
$qtyLine

  BATCH INVENTORY${"".padEnd(innerWidth - 15)} 

$content

    """.trimIndent()
    }
}