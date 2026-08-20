package repository

import model.VendingMachine

object VendingMachineRepository : BaseRepository<VendingMachine>() {
    override fun getId(entity: VendingMachine) = entity.vendingMachineId
}