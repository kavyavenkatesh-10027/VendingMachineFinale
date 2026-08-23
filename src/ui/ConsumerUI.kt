package ui

import controller.AdminController
import controller.ConsumerController
import exception.VendingMachineException
import model.Electronics
import model.Food
import model.Purchase
import model.enum.IndianCurrency
import model.enum.ProductCategory
import java.math.BigDecimal
import java.util.EnumMap

class ConsumerUI : Interactable {

    fun show() {
        var running = true
        while (running) {
            println("\n=====================================")
            println("  CUSTOMER MENU")
            println("=====================================")
            println("  1. Buy products")
            println("  0. Exit")
            println("=====================================")
            try {
                when (prompt("Choice: ")) {
                    "1"  -> buyProducts()
                    "0"  -> running = false
                    else -> println("Invalid choice.")
                }
            } catch (e: VendingMachineException) {
                println("\n  [Error] ${e.message}")
            } catch (e: IllegalArgumentException) {
                println("\n  [Input Error] ${e.message}")
            }
        }
    }

    private fun showAllMachines() {
        val machines = ConsumerController.viewAllVendingMachines()
        println("\n===== Vending Machines =====")
        if (machines.isEmpty()) { println("  No machines available."); return }
        machines.sortedBy { it.vendingMachineId }.forEach {
            println("  ${it.vendingMachineId} | ${it.vendingMachineLocation} | ${it.productTypeInside}")
        }
    }

    private fun showAvailableProducts(vmId: String) {
        val products = ConsumerController.viewAvailableProducts(vmId)
            .sortedBy { it.productId.substringAfterLast("-").toIntOrNull() ?: 0 }
        val category = AdminController.getCategoryByVendingMachineId(vmId)

        println("\n===== Available $category Products =====")
        if (products.isEmpty()) {
            println("  No products in stock.")
            return
        }

        when (category) {
            ProductCategory.FOOD -> {
                // Layout: ID(10) | Name(24) | Price(8) | Type(14) | Diet(8) | Warning(30) | Stock(6)
                println("  %-10s %-24s %8s  %-14s %-8s %-30s %6s"
                    .format("Product ID", "Name", "Price", "Type", "Diet", "Warning", "Stock"))
                println("  " + "-".repeat(108))

                for (p in products) {
                    val qty = ConsumerController.getAvailableStock(vmId, p.productId)
                    val f = p as Food
                    val priceStr = "Rs.${p.price}"

                    // Truncate warning to 28 chars so it never breaks layout
                    val rawWarning = p.warning ?: "-"
                    val warningStr = if (rawWarning.length > 28) rawWarning.take(25) + "..." else rawWarning

                    println("  %-10s %-24s %8s  %-14s %-8s %-30s %6d"
                        .format(p.productId, p.productName, priceStr, f.foodType, f.vegOrNonVeg, warningStr, qty))
                }
            }
            ProductCategory.ELECTRONIC -> {
                // Layout: ID(10) | Name(24) | Price(8) | Type(18) | Warranty(10) | Warning(20) | Stock(6)
                println("  %-10s %-24s %8s  %-18s %-10s %-20s %6s"
                    .format("Product ID", "Name", "Price", "Type", "Warranty", "Warning", "Stock"))
                println("  " + "-".repeat(102))

                for (p in products) {
                    val qty = ConsumerController.getAvailableStock(vmId, p.productId)
                    val e = p as Electronics
                    val priceStr = "Rs.${p.price}"
                    val warrantyStr = "${e.warrantyMonths}m"

                    val rawWarning = p.warning ?: "-"
                    val warningStr = if (rawWarning.length > 18) rawWarning.take(15) + "..." else rawWarning

                    println("  %-10s %-24s %8s  %-18s %-10s %-20s %6d"
                        .format(p.productId, p.productName, priceStr, e.electronicsType, warrantyStr, warningStr, qty))
                }
            }
        }
    }

    private fun buyProducts() {
        showAllMachines()
        val vmId = prompt("Vending machine ID: ").uppercase()
        showAvailableProducts(vmId)

        val cart = buildCart(vmId)
        if (cart.isEmpty()) { println("Nothing in cart. Returning."); return }

        val total = ConsumerController.getCartTotal(cart)
        println("\n  Cart total: Rs.$total")

        val payment = collectPayment(total)
        if (payment.isEmpty()) { println("Purchase cancelled."); return }

        val purchase = ConsumerController.buyProducts(vmId, cart, payment)
        printReceipt(purchase)
    }

    private fun buildCart(vmId: String): Map<String, Int> {
        val cart = mutableMapOf<String, Int>()
        println("\nAdd items (blank Product ID to finish):")
        while (true) {
            val productId = prompt("  Product ID: ").uppercase()
            if (productId.isBlank()) break
            val available = try {
                ConsumerController.getAvailableStock(vmId, productId)
            } catch (e: VendingMachineException) { println("  [!] ${e.message}"); continue }
            if (available == 0) { println("  Out of stock."); continue }
            val qty = readInt("  Quantity (available: $available)")
            if (qty > available) { println("  Only $available available."); continue }
            cart[productId] = (cart[productId] ?: 0) + qty
            println("  Added $qty × $productId")
        }
        return cart
    }

    private fun collectPayment(totalRequired: BigDecimal): Map<IndianCurrency, Int> {
        val payment = EnumMap<IndianCurrency, Int>(IndianCurrency::class.java)
        var paid = BigDecimal.ZERO
        println("\nAccepted: ${IndianCurrency.entries.joinToString(", ") { "Rs.${it.value}" }}")
        println("Type amount or DONE to cancel.\n")

        while (paid < totalRequired) {
            println("  Paid: Rs.$paid  |  Still needed: Rs.${totalRequired - paid}")
            val input = prompt("  Insert: ")

            if (input == "DONE") { println("Cancelled."); return EnumMap(IndianCurrency::class.java) }

            val coin = input.toIntOrNull()?.let { amt -> IndianCurrency.entries.find { it.value == amt } }
            if (coin == null) { println("  Invalid denomination."); continue }

            payment[coin] = (payment[coin] ?: 0) + 1
            paid += BigDecimal.valueOf(coin.value.toLong())
            println("  Accepted Rs.${coin.value}  |  Total: Rs.$paid")
        }
        return payment
    }

    private fun printReceipt(purchase: Purchase) {
        println("\n=====================================")
        println("              RECEIPT")
        println("=====================================")
        println("  ID     : ${purchase.purchaseId}")
        println("  Time   : ${purchase.purchaseTime}")
        println("  Items  : ${purchase.getItemsPurchased()}")
        println("  Total  : Rs.${purchase.totalAmount}")
        println("  Paid   : Rs.${purchase.moneyPaidByCustomer}")
        println("  Change : Rs.${purchase.changeReturned}")
        println("=====================================")
        if (purchase.changeReturned > BigDecimal.ZERO)
            println("  Please collect your change: Rs.${purchase.changeReturned}")
        println("  Thank you!")
        println("=====================================\n")
    }
}