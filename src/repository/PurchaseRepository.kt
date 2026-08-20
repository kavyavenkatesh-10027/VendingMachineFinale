package repository

import model.Purchase

object PurchaseRepository : BaseRepository<Purchase>() {
    override fun getId(entity: Purchase) = entity.purchaseId
}