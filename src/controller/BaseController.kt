package controller

import model.Product
import model.VendingMachine
import service.VendingMachineService

abstract class BaseController {

    fun viewVendingMachine(vendingMachineId: String): VendingMachine {
        require(vendingMachineId.isNotBlank()) { "Vending machine ID cannot be empty" }
        return VendingMachineService.getVendingMachineById(vendingMachineId)
    }

    fun viewAllVendingMachines(): Set<VendingMachine> = VendingMachineService.getAllVendingMachines()

    fun viewAvailableProducts(vendingMachineId: String): Set<Product> {
        require(vendingMachineId.isNotBlank()) { "Vending machine ID cannot be empty" }
        return VendingMachineService.viewAvailableProducts(vendingMachineId)
    }

    fun viewAvailableQuantityForAllProducts(vendingMachineId: String): Map<String, Int> {
        require(vendingMachineId.isNotBlank()) { "Vending machine ID cannot be empty" }
        return VendingMachineService.viewAvailableQuantityForAllProducts(vendingMachineId)
    }

    fun getAvailableQuantityForOneProduct(vendingMachineId: String, productId: String): Int {
        require(vendingMachineId.isNotBlank()) { "Vending machine ID cannot be empty" }
        require(productId.isNotBlank())        { "Product ID cannot be empty" }
        return VendingMachineService.getAvailableQuantityForOneProduct(vendingMachineId, productId)
    }
}