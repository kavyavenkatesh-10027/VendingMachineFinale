package service

import model.Electronics
import model.enum.ElectronicTypes
import repository.ElectronicsRepository
import repository.ProductRepository
import java.math.BigDecimal

object ElectronicsService : BaseProductService<Electronics>() {

    fun registerElectronics(
        productName: String, brand: String, description: String,
        warning: String?, price: BigDecimal,
        warrantyMonths: Int, batteryPowered: Boolean, electronicsType: ElectronicTypes
    ): Electronics {
        val electronics = Electronics(
                productName = productName,
                brand = brand,
                description = description,
                price = price,
                warrantyMonths = warrantyMonths,
                batteryPowered = batteryPowered,
                electronicsType = electronicsType,
                warning = warning
            )
        ProductRepository.add(electronics)
        ElectronicsRepository.add(electronics)
        return electronics
    }

    override fun getById(productId: String): Electronics = ElectronicsRepository.findById(productId)
    override fun getAllProducts(): Set<Electronics> = ElectronicsRepository.findAll()
}