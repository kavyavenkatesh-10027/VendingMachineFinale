package controller

import model.Electronics
import model.Food
import model.Product
import model.ProductionBatch
import model.Purchase
import model.Slot
import model.VendingMachine
import model.enum.*
import service.*
import java.math.BigDecimal
import java.time.LocalDate

object AdminController : BaseController() {

    fun createVendingMachine(
        location: Location,
        establishedOn: LocalDate,
        firstSlotBatches: List<ProductionBatch>,
        category: ProductCategory
    ): VendingMachine {
        require(firstSlotBatches.isNotEmpty()) { "First slot must have at least one batch." }
        return VendingMachineService.createVendingMachine(location, establishedOn, firstSlotBatches, category)
    }

    fun addSlotToVendingMachine(vendingMachineId: String, batches: List<ProductionBatch>): Slot {
        require(vendingMachineId.isNotBlank()) { "Vending machine ID cannot be empty" }
        require(batches.isNotEmpty())          { "Slot must have at least one batch" }
        return VendingMachineService.addSlotToVendingMachine(vendingMachineId, batches)
    }

    fun addNewProductTypeToSlot(
        vmId: String, slotId: String, productId: String,
        manufacturingLocation: Location, manufacturingDate: LocalDate,
        quantity: Int, category: ProductCategory
    ) {
        require(vmId.isNotBlank())       { "Vending machine ID cannot be empty" }
        require(slotId.isNotBlank())     { "Slot ID cannot be empty" }
        require(productId.isNotBlank())  { "Product ID cannot be empty" }
        require(quantity > 0)            { "Quantity must be greater than zero" }
        SlotService.addNewProductTypeToSlot(vmId, slotId, productId, manufacturingLocation, manufacturingDate, quantity, category)
    }

    fun refillProductInSlot(
        vmId: String, slotId: String, productId: String,
        manufacturingLocation: Location, manufacturingDate: LocalDate, quantity: Int
    ) {
        require(vmId.isNotBlank())       { "Vending machine ID cannot be empty" }
        require(slotId.isNotBlank())     { "Slot ID cannot be empty" }
        require(productId.isNotBlank())  { "Product ID cannot be empty" }
        require(quantity > 0)            { "Quantity must be greater than zero" }
        SlotService.refillProductInSlot(vmId, slotId, productId, manufacturingLocation, manufacturingDate, quantity)
    }

    fun removeVendingMachine(vendingMachineId: String) {
        require(vendingMachineId.isNotBlank()) { "Vending machine ID cannot be empty" }
        VendingMachineService.removeVendingMachine(vendingMachineId)
    }

    fun getAllSlots(vendingMachineId: String): Set<Slot> =
        VendingMachineService.getAllSlotsInVendingMachine(vendingMachineId)

    fun getProductById(productId: String): Product {
        require(productId.isNotBlank()) { "Product ID cannot be empty" }
        return BaseProductService.getProductById(productId)
    }

    fun getProductCountForMachine(vendingMachineId: String): Map<String, Int> =
        viewAvailableQuantityForAllProducts(vendingMachineId)

    fun addCashToDrawer(vendingMachineId: String, denominations: Map<IndianCurrency, Int>) {
        require(vendingMachineId.isNotBlank())  { "Vending machine ID cannot be empty" }
        require(denominations.isNotEmpty())     { "Denomination map cannot be empty" }
        val vm = VendingMachineService.getVendingMachineById(vendingMachineId)
        for ((denom, count) in denominations) CurrencyService.addToDrawer(vm.drawer, denom, count)
    }

    fun getDenominationBreakdown(vendingMachineId: String): Map<IndianCurrency, Int> {
        require(vendingMachineId.isNotBlank()) { "Vending machine ID cannot be empty" }
        return VendingMachineService.getVendingMachineById(vendingMachineId).drawer.getDenominations()
    }

    fun getTotalCashInMachine(vendingMachineId: String): BigDecimal {
        require(vendingMachineId.isNotBlank()) { "Vending machine ID cannot be empty" }
        return VendingMachineService.getVendingMachineById(vendingMachineId).drawer.totalCash()
    }

    fun getAllPurchases(): Set<Purchase> = PurchaseService.getAllPurchases()

    fun registerFood(
        productName: String, brand: String, description: String, warning: String?,
        price: BigDecimal, vegOrNonVeg: VegNonVeg,
        ingredients: List<String>, shelfLifeMonths: Int, foodType: FoodType
    ): Food {
        require(productName.isNotBlank())       { "Food name cannot be empty" }
        require(brand.isNotBlank())             { "Brand cannot be empty" }
        require(description.isNotBlank())       { "Description cannot be empty" }
        require(price > BigDecimal.ZERO)        { "Price cannot be zero or negative" }
        require(ingredients.isNotEmpty())       { "At least one ingredient must be provided" }
        return FoodService.registerFood(productName, brand, description, warning, price,
             vegOrNonVeg, ingredients, shelfLifeMonths, foodType)
    }

    fun registerElectronics(
        productName: String, brand: String, description: String, warning: String?,
        price: BigDecimal, warrantyMonths: Int,
        batteryPowered: Boolean, electronicsType: ElectronicTypes
    ): Electronics {
        require(productName.isNotBlank())   { "Electronics name cannot be empty" }
        require(brand.isNotBlank())         { "Brand cannot be empty" }
        require(description.isNotBlank())   { "Description cannot be empty" }
        require(price > BigDecimal.ZERO)    { "Price cannot be zero or negative" }
        require(warrantyMonths >= 0)        { "Warranty months cannot be negative" }
        return ElectronicsService.registerElectronics(productName, brand, description, warning,
            price, warrantyMonths, batteryPowered, electronicsType)
    }

    fun getCategoryByVendingMachineId(vendingMachineId: String): ProductCategory =
        VendingMachineService.getVendingMachineById(vendingMachineId).productTypeInside

    fun getSlotById(vendingMachineId: String, slotId: String): Slot =
        VendingMachineService.getSlotById(vendingMachineId, slotId)

    fun getProductsInSlot(vendingMachineId: String, slotId: String): List<Product> =
        getSlotById(vendingMachineId, slotId)
            .getProductIds()
            .map { BaseProductService.getProductById(it) }

    fun getAllProductsOfCategory(category: ProductCategory): Set<Product> = when (category) {
        ProductCategory.FOOD       -> FoodService.getAllProducts()
        ProductCategory.ELECTRONIC -> ElectronicsService.getAllProducts()
    }
}
