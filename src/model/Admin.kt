package model

import generator.IDGenerator
import model.enum.Gender
import java.time.LocalDate

// The purpose of Admin is to represent an admin user, and it does this by extending User.
class Admin(
    name: String,
    dob: LocalDate,
    gender: Gender
) : User(name, dob, gender) {

    val adminId: String = IDGenerator.generateAdminId()

    override fun toString(): String = super.toString() + "\nAdmin ID      : $adminId"
}