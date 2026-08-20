package service

import exception.CorruptedDataException
import exception.MismatchingProductTypeAndVendingMachine
import exception.UnknownEntityException
import exception.VendingMachineException
import generator.IDGenerator
import model.Product
import model.ProductionBatch
import model.Slot
import model.VendingMachine
import model.enum.Location
import model.enum.ProductCategory
import repository.ProductRepository
import repository.VendingMachineRepository
import java.time.LocalDate

object VendingMachineService {

    fun createVendingMachine(
        location: Location,
        establishedOn: LocalDate,
        firstSlotBatches: List<ProductionBatch>,
        category: ProductCategory
    ): VendingMachine {
        require(establishedOn <= LocalDate.now()) { "Established date must be on or before today" }
        validateBatches(category, firstSlotBatches)

        val firstSlot = Slot(IDGenerator.peekNextVendingMachineId(), firstSlotBatches.toMutableList())
        val vm = VendingMachine(location, establishedOn, category, firstSlot)
        VendingMachineRepository.add(vm)
        return vm
    }

    fun addSlotToVendingMachine(
        vendingMachineId: String,
        batches: List<ProductionBatch>
    ): Slot {
        val vm = getVendingMachineById(vendingMachineId)
        validateBatches(vm.productTypeInside, batches)
        val slot = Slot(vendingMachineId, batches.toMutableList())
        vm.addSlot(slot)
        return slot
    }

    fun getVendingMachineById(id: String): VendingMachine = VendingMachineRepository.findById(id)

    fun getAllVendingMachines(): Set<VendingMachine> = VendingMachineRepository.findAll()

    fun getSlotById(vendingMachineId: String, slotId: String): Slot =
        getVendingMachineById(vendingMachineId).getSlotById(slotId)
            ?: throw UnknownEntityException(slotId, "Slot")

    fun getAllSlotsInVendingMachine(vendingMachineId: String): Set<Slot> =
        getVendingMachineById(vendingMachineId).getAllSlots()

    // Why? Only shows products that have at least 1 non-expired unit available
    fun viewAvailableProducts(vendingMachineId: String): Set<Product> {
        val vm = getVendingMachineById(vendingMachineId)
        val productIds = vm.getAllSlots()
            .flatMap { it.getProductIds() }
            .toSet()

        return productIds
            .filter { productId -> getTotalSellableQuantity(vm, productId) > 0 }
            .map { productId ->
                try { ProductRepository.findById(productId) }
                catch (_: VendingMachineException) {
                    throw CorruptedDataException("Machine '$vendingMachineId' has unregistered product '$productId'")
                }
            }.toSet()
    }

    fun viewAvailableQuantityForAllProducts(vendingMachineId: String): Map<String, Int> {
        val vm = getVendingMachineById(vendingMachineId)
        val result = mutableMapOf<String, Int>()
        for (slot in vm.getAllSlots()) {
            for (productId in slot.getProductIds()) {
                val qty = slot.getSellableQuantity(productId)
                if (qty > 0) result[productId] = (result[productId] ?: 0) + qty
            }
        }
        return result
    }

    fun getAvailableQuantityForOneProduct(vendingMachineId: String, productId: String): Int {
        if (!ProductRepository.existsById(productId)) {
            throw UnknownEntityException(productId, "Product")
        }
        return getTotalSellableQuantity(getVendingMachineById(vendingMachineId), productId)
    }

    fun removeVendingMachine(vendingMachineId: String) {
        if (!VendingMachineRepository.existsById(vendingMachineId)) {
            throw UnknownEntityException(vendingMachineId, "Vending machine")
        }
        VendingMachineRepository.removeById(vendingMachineId)
    }

    // Why? Sums sellable quantity across all slots for one product
    fun getTotalSellableQuantity(vm: VendingMachine, productId: String): Int =
        vm.getAllSlots().sumOf { it.getSellableQuantity(productId) }

    // Why? Validates that every batch belongs to the right category and is not expired
    private fun validateBatches(category: ProductCategory, batches: List<ProductionBatch>) {
        require(batches.isNotEmpty()) { "A slot must have at least one batch." }
        for (batch in batches) {
            val product = ProductRepository.findById(batch.productId)
            if (product.productCategory != category) {
                throw MismatchingProductTypeAndVendingMachine(category, product.productCategory)
            }
            if (batch.isExpired()) {
                throw exception.ExpiredProductException(batch.batchId, batch.manufacturingDate, batch.expiryDate!!)
            }
        }
    }
}