package model

import model.enum.Gender
import java.time.LocalDate

// The purpose of User is to provide a template of mandatory data for all user types, and it does
// this by abstract class.
abstract class User(
    val name: String,
    val dob: LocalDate,
    val gender: Gender
) {
    init {
        require(name.isNotBlank())                  { "Name cannot be empty" }
        require(!dob.isAfter(LocalDate.now()))       { "Date of Birth must be on or before today" }
    }

    override fun toString(): String =
        "Name          : $name\n" +
                "Date of Birth : $dob\n" +
                "Gender        : $gender"
}