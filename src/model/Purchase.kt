package model

import generator.IDGenerator
import java.math.BigDecimal
import java.time.LocalDateTime

// The purpose of Purchase is to be an immutable record of a completed transaction.
data class Purchase(
    private val itemsPurchased: Map<String, Int>,   // productId -> quantity
    val totalAmount: BigDecimal,
    val moneyPaidByCustomer: BigDecimal,
    val changeReturned: BigDecimal,
    val purchaseTime: LocalDateTime = LocalDateTime.now()
) {
    val purchaseId: String = IDGenerator.generatePurchaseId()

    init {
        require(itemsPurchased.isNotEmpty())        { "Purchase cannot have an empty cart" }
        require(totalAmount > BigDecimal.ZERO)      { "Total amount must be greater than zero" }
        require(moneyPaidByCustomer > BigDecimal.ZERO) { "Amount paid must be greater than zero" }
        require(changeReturned >= BigDecimal.ZERO)  { "Change cannot be negative" }
    }

    fun getItemsPurchased(): Map<String, Int> = itemsPurchased.toMap()

    override fun toString(): String =
        """
Purchase ID             : $purchaseId
Time                    : $purchaseTime
Items                   : $itemsPurchased
Total                   : ₹$totalAmount
Paid                    : ₹$moneyPaidByCustomer
Change Returned         : ₹$changeReturned
        """.trimIndent()
}