package service

import exception.ExistsAlreadyException
import exception.MismatchingProductTypeAndVendingMachine
import exception.UnregisteredEntityException
import model.Electronics
import model.Food
import model.ProductionBatch
import model.enum.Location
import model.enum.ProductCategory
import repository.ProductRepository
import java.time.LocalDate

object SlotService {

    // Why? Adds a brand new product type (first ever batch) into a slot
    fun addNewProductTypeToSlot(
        vendingMachineId: String,
        slotId: String,
        productId: String,
        manufacturingLocation: Location,
        manufacturingDate: LocalDate,
        quantity: Int,
        category: ProductCategory
    ) {
        val slot = VendingMachineService.getSlotById(vendingMachineId, slotId)
        val product = ProductRepository.findById(productId)

        if (product.productCategory != category) {
            throw MismatchingProductTypeAndVendingMachine(category, product.productCategory)
        }
        if (slot.getBatches().any { it.productId == productId }) {
            throw ExistsAlreadyException(
                "Product '$productId' already exists in slot '$slotId'. Use refillSlot instead."
            )
        }

        val batch = buildBatch(product, productId, manufacturingLocation, manufacturingDate, quantity)
        slot.addNewProductTypeToSlot(batch)
    }

    // Why? Adds a new production batch to a product already in the slot (restocking)
    fun refillProductInSlot(
        vendingMachineId: String,
        slotId: String,
        productId: String,
        manufacturingLocation: Location,
        manufacturingDate: LocalDate,
        quantity: Int
    ) {
        val slot = VendingMachineService.getSlotById(vendingMachineId, slotId)

        if (slot.getBatches().none { it.productId == productId }) {
            throw UnregisteredEntityException(
                "Product", productId, "Slot", slotId, "Use addNewProductTypeToSlot instead."
            )
        }

        val product = ProductRepository.findById(productId)
        val batch = buildBatch(product, productId, manufacturingLocation, manufacturingDate, quantity)
        slot.refillSlot(batch)
    }

    // Why? Centralised batch construction — Food gets calculated expiry, Electronics gets null
    private fun buildBatch(
        product: model.Product,
        productId: String,
        manufacturingLocation: Location,
        manufacturingDate: LocalDate,
        quantity: Int
    ): ProductionBatch {
        val expiryDate = when (product) {
            is Food        -> product.calculateExpiryDate(manufacturingDate)
            is Electronics -> null
        }
        return ProductionBatch(
            productId = productId,
            manufacturingLocation = manufacturingLocation,
            manufacturingDate = manufacturingDate,
            expiryDate = expiryDate,
            quantity = quantity
        )
    }
}