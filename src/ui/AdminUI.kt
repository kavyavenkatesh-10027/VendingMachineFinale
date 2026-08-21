package ui

import controller.AdminController
import exception.EmptyMenuException
import exception.VendingMachineException
import model.Product
import model.Food
import model.ProductionBatch
import model.enum.*
import java.util.EnumMap

class AdminUI : Interactable {

    fun show() {
        var running = true
        while (running) {
            println("\n========== ADMIN MENU ==========")
            println("1. Create vending machine")
            println("2. View vending machine")
            println("3. Remove vending machine")
            println("4. Register product")
            println("5. View all products")
            println("6. Add product to slot")
            println("7. View cash drawer")
            println("8. Add cash to drawer")
            println("9. View purchase history")
            println("0. Exit")
            println("=================================")
            try {
                when (prompt("Choice: ")) {
                    "1" -> createVendingMachine()
                    "2" -> viewVendingMachine()
                    "3" -> removeVendingMachine()
                    "4" -> registerProductStandalone()
                    "5" -> viewAllProducts()
                    "6" -> addProductToSlot()
                    "7" -> viewCashDrawer()
                    "8" -> addCashToDrawer()
                    "9" -> viewPurchaseHistory()
                    "0" -> running = false
                    else -> println("Invalid choice.")
                }
            } catch (e: VendingMachineException) {
                println("[Error] ${e.message}")
            } catch (e: IllegalArgumentException) {
                println("[Input Error] ${e.message}")
            }
        }
    }



    private fun registerProductStandalone() {
        println("\n--- Register Product ---")
        val category = readEnum(ProductCategory::class.java, "Product category")
        val product = when (category) {
            ProductCategory.FOOD       -> registerFood()
            ProductCategory.ELECTRONIC -> registerElectronics()
        }
        println("\nProduct registered successfully!")
        println(product)
    }



    private fun createVendingMachine() {
        println("\n--- Create Vending Machine ---")
        val category      = readEnum(ProductCategory::class.java, "Product category")
        val location      = readEnum(Location::class.java, "Location")
        val establishedOn = readDate("Established on (yyyy-MM-dd): ")

        val hasProducts = displayProductMenu(category)
        if (!hasProducts) {
            println("No ${category.name.lowercase()} products registered.")
            val register = prompt("Register one now? (y/n): ")
            if (!register.equals("y", ignoreCase = true)) {
                println("Cancelled. Register a product first.")
                return
            }
            registerProduct(category)
            displayProductMenu(category)  // ab dikhao registered product
        }

        val batches = readBatchList(category, "first slot")
        val vm = AdminController.createVendingMachine(location, establishedOn, batches, category)
        println("\nVending machine created successfully!")
        println("  Batches added: ${batches.size}")
        addCashToDrawer(vm.vendingMachineId)
        println("\n$vm")
    }



    private fun viewVendingMachine() {
        println("\n--- View Vending Machine ---")
        println("  1. All   2. One")
        when (prompt("Choose: ")) {
            "1" -> AdminController.viewAllVendingMachines()
                .sortedBy { it.vendingMachineId }
                .forEach { println("\n$it\n" + "-".repeat(40)) }
            "2" -> {
                displayVendingMachineMenu()
                val vmId = prompt("Vending machine ID: ")
                val vm = AdminController.viewVendingMachine(vmId)
                println("\n$vm")
                AdminController.getAllSlots(vmId)
                    .sortedBy { it.slotId }
                    .forEach { println("\n$it") }
            }
            else -> println("Invalid choice.")
        }
    }



    private fun removeVendingMachine() {
        println("\n--- Remove Vending Machine ---")
        displayVendingMachineMenu()
        val vmId = prompt("Vending machine ID to remove: ")
        AdminController.removeVendingMachine(vmId)
        println("Removed $vmId.")
    }



    private fun viewAllProducts() {
        val category = readEnum(ProductCategory::class.java, "Product category")
        val products = AdminController.getAllProductsOfCategory(category)
        if (products.isEmpty()) { println("No $category items registered."); return }
        println("\n===== All $category Items =====")
        products.sortedBy { it.productId }.forEach { println("$it\n" + "-".repeat(40)) }
    }



    private fun addProductToSlot() {
        println("\n--- Add Product to Slot ---")
        displayVendingMachineMenu()
        val vmId     = prompt("Vending machine ID: ")
        val category = AdminController.getCategoryByVendingMachineId(vmId)

        println("\n  1. Existing slot   2. New slot")
        when (prompt("Choose: ")) {
            "2"  -> createNewSlot(vmId, category)
            else -> {
                val slotId = pickExistingSlot(vmId, category) ?: return
                addBatchToExistingSlot(vmId, slotId, category)
            }
        }
    }

    private fun pickExistingSlot(vmId: String, category: ProductCategory): String? {
        val slots = AdminController.getAllSlots(vmId).filter { it.vendingMachineId == vmId }
        if (slots.isEmpty()) {
            println("No existing slots — creating a new one.")
            createNewSlot(vmId, category)
            return null
        }
        println("\nSlots on $vmId:")
        slots.sortedBy { it.slotId }.forEach { println("  ${it.slotId}") }
        val slotId = prompt("Slot ID: ")
        if (slotId.isBlank()) {
            println("Cancelled.")
            return null
        }
        return slotId
    }

    private fun createNewSlot(vmId: String, category: ProductCategory): String {
        val hasProducts = displayProductMenu(category)
        if (!hasProducts) {
            println("No ${category.name.lowercase()} products registered.")
            val register = prompt("Register one now? (y/n): ")
            if (!register.equals("y", ignoreCase = true)) {
                println("Cancelled.")
                return ""
            }
            registerProduct(category)
            displayProductMenu(category)
        }
        val batches = readBatchList(category, "new slot")
        val slot = AdminController.addSlotToVendingMachine(vmId, batches)
        println("\nSlot added: ${slot.slotId}")
        println(slot)
        return slot.slotId
    }

    private fun addBatchToExistingSlot(vmId: String, slotId: String, category: ProductCategory) {
        var productId: String? = null
        var done = false
        while (!done) {
            println("\n  1. New product (not registered yet)")
            println("  2. Existing product")
            println("  3. View current stock at this machine")
            when (prompt("Choose: ")) {
                "1" -> { productId = registerProduct(category); done = true }
                "2" -> { productId = pickExistingProduct(category); done = true }
                "3" -> viewProductCount(vmId)
                else -> println("Invalid.")
            }
        }

        val id = productId ?: return
        val location = readEnum(Location::class.java, "Manufacturing location of this batch")
        val mfgDate  = readDate("Manufacturing date (yyyy-MM-dd): ")
        val qty      = readInt("Quantity")

        val alreadyInSlot = AdminController.getProductsInSlot(vmId, slotId).any { it.productId == id }
        if (alreadyInSlot) {
            AdminController.refillProductInSlot(vmId, slotId, id, location, mfgDate, qty)
            println("Slot refilled.")
        } else {
            AdminController.addNewProductTypeToSlot(vmId, slotId, id, location, mfgDate, qty, category)
            println("Product added to slot.")
        }
    }

    private fun registerProduct(category: ProductCategory): String =
        when (category) {
            ProductCategory.FOOD       -> registerFood().productId
            ProductCategory.ELECTRONIC -> registerElectronics().productId
        }

    private fun registerFood(): Product {
        println("\n--- Register Food ---")
        val name        = prompt("Product name: ")
        val brand       = prompt("Brand: ")
        val description = prompt("Description: ")
        var warning     = prompt("Warning (Enter to skip): ")
        if (warning.isBlank()) warning = "- nil -"
        val price       = readBigDecimal("Price: ")
        val shelfLife   = readInt("Shelf life (months)")
        val vegNonVeg   = readEnum(VegNonVeg::class.java, "Veg/Non-Veg")
        val ingredients = prompt("Ingredients (comma-separated): ").split(",").map { it.trim() }
        val foodType    = readEnum(FoodType::class.java, "Food type")
        val food = AdminController.registerFood(name, brand, description, warning, price,
            vegNonVeg, ingredients, shelfLife, foodType)
        println("\nFood registered: ${food.productId}")
        return food
    }

    private fun registerElectronics(): Product {
        println("\n--- Register Electronics ---")
        val name          = prompt("Product name: ")
        val brand         = prompt("Brand: ")
        val description   = prompt("Description: ")
        var warning       = prompt("Warning (Enter to skip): ")
        if (warning.isBlank()) warning = "- nil -"
        val price         = readBigDecimal("Price: ")
        val warranty      = readInt("Warranty (months)")
        val battery       = prompt("Battery powered? (y/n): ").equals("y", ignoreCase = true)
        val electronicsType = readEnum(ElectronicTypes::class.java, "Electronics type")
        val e = AdminController.registerElectronics(name, brand, description, warning, price,
            warranty, battery, electronicsType)
        println("\nElectronics registered: ${e.productId}")
        return e
    }

    private fun pickExistingProduct(category: ProductCategory): String {
        displayProductMenu(category)
        return prompt("Product ID: ")
    }

    private fun viewProductCount(vmId: String) {
        val stockMap = AdminController.getProductCountForMachine(vmId)
        if (stockMap.isEmpty()) { println("No products stocked."); return }
        println("\n  %-14s %-24s %8s  %6s".format("Product ID", "Name", "Price", "Stock"))
        println("  " + "-".repeat(58))
        for ((productId, qty) in stockMap) {
            val p = AdminController.getProductById(productId)
            println("  %-14s %-24s Rs.%-5s  %6d".format(p.productId, p.productName, p.price, qty))
        }
        println("  Total units: ${stockMap.values.sum()}")
    }



    private fun viewCashDrawer() {
        displayVendingMachineMenu()
        val vmId = prompt("Vending machine ID: ")
        println("\n===== Cash Drawer — $vmId =====")
        AdminController.getDenominationBreakdown(vmId)
            .forEach { (denom, count) -> println("  Rs.%-4d  x  %d".format(denom.value, count)) }
        println("  Total: ₹${AdminController.getTotalCashInMachine(vmId)}")
    }



    private fun addCashToDrawer(vmId: String = "") {
        println("\n--- Add Cash to Drawer ---")
        val vendingMachineId = vmId.ifBlank {
            displayVendingMachineMenu()
            prompt("Vending machine ID: ")
        }

        val denominations = EnumMap<IndianCurrency, Int>(IndianCurrency::class.java)
        println("Enter count for each denomination (Enter to skip):")
        for (denom in IndianCurrency.entries) {
            val input = prompt("  Rs.${denom.value}: ")
            if (input.isBlank()) continue
            try {
                val count = input.toInt()
                if (count > 0) denominations[denom] = count
            } catch (_: NumberFormatException) { println("  Invalid, skipping.") }
        }
        if (denominations.isEmpty()) { println("Nothing added."); return }

        AdminController.addCashToDrawer(vendingMachineId, denominations)
        println("\nCash added. Current drawer:")
        AdminController.getDenominationBreakdown(vendingMachineId)
            .forEach { (denom, count) -> println("  Rs.%-4d  x  %d".format(denom.value, count)) }
        println("  Total: ₹${AdminController.getTotalCashInMachine(vendingMachineId)}")
    }



    private fun viewPurchaseHistory() {
        val purchases = AdminController.getAllPurchases()
        if (purchases.isEmpty()) { println("No purchases yet."); return }
        println("\n===== Purchase History =====")
        for (p in purchases) {
            println("  ID     : ${p.purchaseId}")
            println("  Time   : ${p.purchaseTime}")
            println("  Items  : ${p.getItemsPurchased()}")
            println("  Total  : ₹${p.totalAmount}")
            println("  Paid   : ₹${p.moneyPaidByCustomer}")
            println("  Change : ₹${p.changeReturned}")
            println("  " + "-".repeat(40))
        }
    }

    // Display helpers

    private fun displayVendingMachineMenu() {
        val all = AdminController.viewAllVendingMachines()
        if (all.isEmpty()) throw EmptyMenuException("vending machine")
        println("\n@Vending Machines:")
        all.sortedBy { it.vendingMachineId }.forEach {
            println("  ${it.vendingMachineId} | ${it.vendingMachineLocation} | ${it.productTypeInside}")
        }
        println()
    }

    private fun displayProductMenu(category: ProductCategory): Boolean {
        val products = AdminController.getAllProductsOfCategory(category)
            .sortedBy { it.productId.substringAfterLast("-").toIntOrNull() ?: 0 }
        if (products.isEmpty()) return false
        println("\n@$category Products:")
        products.sortedBy { it.productId }.forEach {
            println("  ${it.productId} | ${it.productName} | ${it.brand} | ₹${it.price}")
        }
        println()
        return true
    }



    // Why? Reads one or more ProductionBatch entries for a slot from the admin.
    // Each batch = one productId + one manufacturing location + one date + quantity.
    private fun readBatchList(category: ProductCategory, context: String): List<ProductionBatch> {
        val batches = mutableListOf<ProductionBatch>()
        println("Enter batches for $context (blank Product ID to stop):")
        while (true) {
            val input = prompt("  Product ID (or NEW to register): ")
            val productId = when {
                input.equals("NEW", ignoreCase = true) -> {
                    try { registerProduct(category) }
                    catch (e: VendingMachineException) { println("  [!] ${e.message}"); continue }
                    catch (e: IllegalArgumentException) { println("  [!] ${e.message}"); continue }
                }
                input.isBlank() -> {
                    if (batches.isEmpty()) { println("  At least one batch required."); continue }
                    break
                }
                else -> input
            }

            val location = readEnum(Location::class.java, "  Manufacturing location")
            val mfgDate  = readDate("  Manufacturing date (yyyy-MM-dd): ")
            val qty      = readInt("  Quantity")

            val product = try {
                AdminController.getProductById(productId)
            } catch (e: VendingMachineException) {
                println("  [!] ${e.message}"); continue
            }

            val expiryDate = if (product is Food) product.calculateExpiryDate(mfgDate) else null

            batches.add(
                ProductionBatch(
                    productId = productId,
                    manufacturingLocation = location,
                    manufacturingDate = mfgDate,
                    expiryDate = expiryDate,
                    quantity = qty
                )
            )
        }
        return batches
    }
}