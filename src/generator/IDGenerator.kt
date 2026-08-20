package generator

object IDGenerator {

    private var nextVendingMachineId = 1L
    private var nextSlotId = 1L
    private var nextAdminId = 1L
    private var nextProductId = 1L
    private var nextPurchaseId = 1L
    private var nextBatchId = 1L

    fun peekNextVendingMachineId(): String = "vendingMachine-$nextVendingMachineId"

    fun generateVendingMachineId(): String = "vendingMachine-${nextVendingMachineId++}"

    fun generateSlotId(): String = "slot-${nextSlotId++}"

    fun generateAdminId(): String = "admin-${nextAdminId++}"

    fun generateProductId(): String = "product-${nextProductId++}"

    fun generatePurchaseId(): String = "purchase-${nextPurchaseId++}"

    fun generateBatchId(): String = "batch-${nextBatchId++}"
}