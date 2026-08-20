package model

import exception.SlotVendingMachineMismatchException
import generator.IDGenerator
import model.enum.Location
import model.enum.ProductCategory
import java.time.LocalDate

// The purpose of VendingMachine is to represent a physical vending machine and own its slots.
class VendingMachine(
    val vendingMachineLocation: Location,
    val establishedOn: LocalDate,
    val productTypeInside: ProductCategory,
    firstSlot: Slot
) {
    val vendingMachineId: String = IDGenerator.generateVendingMachineId()
    val drawer: Drawer = Drawer()

    private val slots: MutableSet<Slot> = mutableSetOf()

    init {
        require(establishedOn <= LocalDate.now()) { "Established date must be on or before today" }
        addSlot(firstSlot)
    }

    fun addSlot(slot: Slot) {
        if (slot.vendingMachineId != vendingMachineId) {
            throw SlotVendingMachineMismatchException(slot.slotId)
        }
        slots.add(slot)
    }

    fun getAllSlots(): Set<Slot> = slots.toSet()

    fun getSlotById(slotId: String): Slot? = slots.find { it.slotId == slotId }

    override fun toString(): String =
        """
Vending Machine ID      : $vendingMachineId
Type                    : $productTypeInside
Location                : $vendingMachineLocation
Established On          : $establishedOn
Number of Slots         : ${slots.size}
Cash Available          : ₹${drawer.totalCash()}
        """.trimIndent()
}