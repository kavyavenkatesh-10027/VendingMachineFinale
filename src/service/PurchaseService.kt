package service

import exception.AvailabilityRequirementException
import exception.InsufficientPaymentException
import exception.VendingMachineException
import model.Purchase
import model.VendingMachine
import model.enum.IndianCurrency
import repository.ProductRepository
import repository.PurchaseRepository
import java.math.BigDecimal

object PurchaseService {

    fun processPurchase(
        vm: VendingMachine,
        cart: Map<String, Int>,
        inserted: Map<IndianCurrency, Int>
    ): Purchase {
        // Validate stock
        for ((productId, qty) in cart) {
            require(productId.isNotBlank())  { "Product ID cannot be blank" }
            require(qty > 0)                 { "Quantity must be greater than zero" }
            val product = ProductRepository.findById(productId)
            val stock = VendingMachineService.getTotalSellableQuantity(vm, productId)
            if (stock < qty) {
                throw AvailabilityRequirementException(
                    "Insufficient stock for '${product.productName}'. Available: $stock"
                )
            }
        }

        val total = getCartTotal(cart)
        val amountPaid = CurrencyService.acceptPayment(vm.drawer, inserted)

        if (amountPaid < total) {
            CurrencyService.refund(vm.drawer, inserted)
            throw InsufficientPaymentException(total, amountPaid)
        }

        val change = amountPaid - total
        try {
            CurrencyService.makeChange(vm.drawer, change)
        } catch (e: VendingMachineException) {
            CurrencyService.refund(vm.drawer, inserted)
            throw e
        }

        // Deduct stock — based on oldest manufacturing date
        for ((productId, qty) in cart) {
            var remaining = qty
            for (slot in vm.getAllSlots()) {
                if (remaining <= 0) break
                val inSlot = slot.getSellableQuantity(productId)
                if (inSlot > 0) {
                    val take = minOf(inSlot, remaining)
                    slot.sellFromSlot(productId, take)
                    remaining -= take
                }
            }
        }

        val purchase = Purchase(cart, total, amountPaid, change)
        PurchaseRepository.add(purchase)
        return purchase
    }

    fun getCartTotal(cart: Map<String, Int>): BigDecimal =
        cart.entries.fold(BigDecimal.ZERO) { acc, (productId, qty) ->
            acc + ProductRepository.findById(productId).price * BigDecimal.valueOf(qty.toLong())
        }

    fun getAllPurchases(): Set<Purchase> = PurchaseRepository.findAll()
}