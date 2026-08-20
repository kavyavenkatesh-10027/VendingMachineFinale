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
        if (products.isEmpty()) { println("  No products in stock."); return }

        when (category) {
            ProductCategory.FOOD -> {
                println("  %-12s %-22s %8s  %-10s  %-10s  %-14s  %5s"
                    .format("Product ID", "Name", "Price", "Type", "Veg/NonVeg", "Warning", "Stock"))
                println("  " + "-".repeat(90))
                for (p in products) {
                    val qty = ConsumerController.getAvailableStock(vmId, p.productId)
                    val f = p as Food
                    println("  %-12s %-22s Rs.%-5s  %-10s  %-10s  %-14s  %5d"
                        .format(p.productId, p.productName, p.price, f.foodType, f.vegOrNonVeg, p.warning ?: "-", qty))
                }
            }
            ProductCategory.ELECTRONIC -> {
                println("  %-12s %-22s %8s  %-18s  %-10s  %-14s  %5s"
                    .format("Product ID", "Name", "Price", "Type", "Warranty", "Warning", "Stock"))
                println("  " + "-".repeat(95))
                for (p in products) {
                    val qty = ConsumerController.getAvailableStock(vmId, p.productId)
                    val e = p as Electronics
                    println("  %-12s %-22s Rs.%-5s  %-18s  %-10s  %-14s  %5d"
                        .format(p.productId, p.productName, p.price, e.electronicsType, "${e.warrantyMonths}m", p.warning ?: "-", qty))
                }
            }
        }
    }

    private fun buyProducts() {
        showAllMachines()
        val vmId = prompt("Vending machine ID: ")
        showAvailableProducts(vmId)

        val cart = buildCart(vmId)
        if (cart.isEmpty()) { println("Nothing in cart. Returning."); return }

        val total = ConsumerController.getCartTotal(cart)
        println("\n  Cart total: ₹$total")

        val payment = collectPayment(total)
        if (payment.isEmpty()) { println("Purchase cancelled."); return }

        val purchase = ConsumerController.buyProducts(vmId, cart, payment)
        printReceipt(purchase)
    }

    private fun buildCart(vmId: String): Map<String, Int> {
        val cart = mutableMapOf<String, Int>()
        println("\nAdd items (blank Product ID to finish):")
        while (true) {
            val productId = prompt("  Product ID: ")
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
            println("  Paid: ₹$paid  |  Still needed: ₹${totalRequired - paid}")
            val input = prompt("  Insert: ").uppercase()
            if (input == "DONE") { println("Cancelled."); return EnumMap(IndianCurrency::class.java) }
            val coin = input.toIntOrNull()?.let { amt -> IndianCurrency.entries.find { it.value == amt } }
            if (coin == null) { println("  Invalid denomination."); continue }
            payment[coin] = (payment[coin] ?: 0) + 1
            paid += BigDecimal.valueOf(coin.value.toLong())
            println("  Accepted ₹${coin.value}  |  Total: ₹$paid")
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
        println("  Total  : ₹${purchase.totalAmount}")
        println("  Paid   : ₹${purchase.moneyPaidByCustomer}")
        println("  Change : ₹${purchase.changeReturned}")
        println("=====================================")
        if (purchase.changeReturned > BigDecimal.ZERO)
            println("  Please collect your change: ₹${purchase.changeReturned}")
        println("  Thank you!")
        println("=====================================\n")
    }
}