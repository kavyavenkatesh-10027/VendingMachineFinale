package ui

import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeParseException

interface Interactable {

    fun prompt(label: String): String {
        print(label)
        return readln().trim()
    }

    fun readInt(label: String): Int {
        while (true) {
            print("$label : ")
            try {
                val value = readln().trim().toInt()
                if (value > 0) return value
                println("Please enter a number greater than zero.")
            } catch (_: NumberFormatException) {
                println("Invalid input. Please enter a whole number greater than zero.")
            }
        }
    }

    fun readDate(label: String): LocalDate {
        while (true) {
            print(label)
            try { return LocalDate.parse(readln().trim()) }
            catch (_: DateTimeParseException) { println("Invalid date. Use yyyy-MM-dd.") }
        }
    }

    fun readBigDecimal(label: String): BigDecimal {
        while (true) {
            print(label)
            try {
                val value = BigDecimal(readln().trim())
                if (value > BigDecimal.ZERO) return value
                println("Please enter a number greater than zero.")
            } catch (_: NumberFormatException) {
                println("Invalid input.")
            }
        }
    }

    fun <T : Enum<T>> readEnum(clazz: Class<T>, label: String): T {
        val constants = clazz.enumConstants
        println("$label options:")
        constants.forEachIndexed { i, value -> println("  ${i + 1}. $value") }
        while (true) {
            print("Choose (1-${constants.size}): ")
            try {
                val choice = readln().trim().toInt()
                if (choice in 1..constants.size) return constants[choice - 1]
            } catch (_: NumberFormatException) {}
            println("Invalid choice.")
        }
    }
}