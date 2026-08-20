package service

import model.Electronics
import model.ProductFactory
import model.enum.ElectronicTypes
import model.enum.Location
import repository.ElectronicsRepository
import repository.ProductRepository
import java.math.BigDecimal

object ElectronicsService : BaseProductService<Electronics>() {

    fun registerElectronics(
        productName: String, brand: String, description: String,
        warning: String?, price: BigDecimal,
        warrantyMonths: Int, batteryPowered: Boolean, electronicsType: ElectronicTypes
    ): Electronics {
        val electronics = ProductFactory.createElectronics(
            productName, brand, description, warning, price,
            warrantyMonths, batteryPowered, electronicsType
        )
        ProductRepository.add(electronics)
        ElectronicsRepository.add(electronics)
        return electronics
    }

    override fun getById(productId: String): Electronics = ElectronicsRepository.findById(productId)
    override fun getAllProducts(): Set<Electronics> = ElectronicsRepository.findAll()
}