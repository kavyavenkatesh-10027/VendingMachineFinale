package repository

import model.Admin

object AdminRepository : BaseRepository<Admin>() {
    override fun getId(entity: Admin) = entity.adminId
}