package controller

import model.Purchase
import model.enum.IndianCurrency
import service.PurchaseService
import service.VendingMachineService
import java.math.BigDecimal

object ConsumerController : BaseController() {

    fun buyProducts(
        vendingMachineId: String,
        cart: Map<String, Int>,
        inserted: Map<IndianCurrency, Int>
    ): Purchase {
        require(vendingMachineId.isNotBlank()) { "Vending machine ID cannot be empty" }
        require(cart.isNotEmpty())             { "Cart is empty" }
        require(inserted.isNotEmpty())         { "No money inserted" }
        val vm = VendingMachineService.getVendingMachineById(vendingMachineId)
        return PurchaseService.processPurchase(vm, cart, inserted)
    }

    fun getCartTotal(cart: Map<String, Int>): BigDecimal {
        require(cart.isNotEmpty()) { "Cart is empty" }
        return PurchaseService.getCartTotal(cart)
    }

    fun getAvailableStock(vendingMachineId: String, productId: String): Int =
        getAvailableQuantityForOneProduct(vendingMachineId, productId)
}