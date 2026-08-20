package exception

import model.enum.ProductCategory
import java.math.BigDecimal
import java.time.LocalDate

abstract class VendingMachineException(message: String) : RuntimeException(message)

class AvailabilityRequirementException(message: String) : VendingMachineException(message)

class InsufficientPaymentException(total: BigDecimal, amountPaid: BigDecimal) : VendingMachineException(
    "Insufficient payment. Total: ₹$total, Paid: ₹$amountPaid. Collect refund from the inserting plate."
)

class InsufficientDenominationForChangeException(changeAmount: BigDecimal) : VendingMachineException(
    "Machine cannot make exact change of ₹$changeAmount."
)

class UnknownEntityException(
    entityDetail: String,
    entity: String = "Entity",
    suggestion: String = ""
) : VendingMachineException("$entity '$entityDetail' does not exist. $suggestion")

class MismatchingProductTypeAndVendingMachine(
    vendingMachineType: ProductCategory,
    productCategory: ProductCategory
) : VendingMachineException(
    "Cannot add $productCategory product to a $vendingMachineType vending machine."
)

class UnregisteredEntityException(
    item: String,
    itemId: String,
    container: String,
    containerId: String,
    suggestion: String = ""
) : VendingMachineException(
    "$item '$itemId' is not present in $container '$containerId'. $suggestion"
)

class EmptyMenuException(menu: String) : VendingMachineException(
    "No ${menu}s have been registered yet."
)

class IllegalNegativeValueException(valueName: String) : IllegalArgumentException(
    "$valueName cannot be negative."
)

class ExistsAlreadyException(message: String) : VendingMachineException(message)

class ExpiredProductException(
    batchId: String,
    manufacturingDate: LocalDate,
    expiryDate: LocalDate
) : VendingMachineException(
    "Batch '$batchId' manufactured on $manufacturingDate expired on $expiryDate — cannot be stocked."
)

class SlotVendingMachineMismatchException(slotId: String) : VendingMachineException(
    "Slot '$slotId' belongs to a different vending machine."
)

class CorruptedDataException(details: String) : VendingMachineException(
    "Vending machine data is corrupted. $details"
)