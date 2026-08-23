package generator

object IDGenerator {

    private var nextVendingMachineId = 1L
    private var nextSlotId = 1L
    private var nextAdminId = 1L
    private var nextProductId = 1L
    private var nextPurchaseId = 1L
    private var nextBatchId = 1L

    fun peekNextVendingMachineId(): String = "VDM-$nextVendingMachineId"

    fun generateVendingMachineId(): String = "VDM-${nextVendingMachineId++}"

    fun generateSlotId(): String = "SLT-${nextSlotId++}"

    fun generateAdminId(): String = "ADM-${nextAdminId++}"

    fun generateProductId(): String = "PDT-${nextProductId++}"

    fun generatePurchaseId(): String = "PCH-${nextPurchaseId++}"

    fun generateBatchId(): String = "BTC-${nextBatchId++}"
}