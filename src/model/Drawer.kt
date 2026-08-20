package model

import exception.AvailabilityRequirementException
import model.enum.IndianCurrency
import java.math.BigDecimal
import java.util.EnumMap

// The purpose of Drawer is to manage the cash inside a single vending machine.
class Drawer {

    private val denominations = EnumMap<IndianCurrency, Int>(IndianCurrency::class.java)

    init {
        IndianCurrency.entries.forEach { denominations[it] = 0 }
    }

    fun getCount(denomination: IndianCurrency): Int = denominations[denomination] ?: 0

    fun add(denomination: IndianCurrency, count: Int) {
        require(count > 0) { "Count must be greater than zero" }
        denominations[denomination] = getCount(denomination) + count
    }

    fun deduct(denomination: IndianCurrency, count: Int) {
        require(count > 0) { "Count must be greater than zero" }
        val current = getCount(denomination)
        if (count > current) throw AvailabilityRequirementException("Insufficient denomination to deduct")
        denominations[denomination] = current - count
    }

    fun getDenominations(): Map<IndianCurrency, Int> = denominations.toMap()

    fun totalCash(): BigDecimal =
        denominations.entries.fold(BigDecimal.ZERO) { acc, (denom, count) ->
            acc + BigDecimal.valueOf(denom.value.toLong()) * BigDecimal.valueOf(count.toLong())
        }

    override fun toString(): String =
        "Drawer\n------\n" +
                denominations.entries.joinToString("\n") { "  Rs.${it.key.value} x ${it.value}" } +
                "\nTotal : ₹${totalCash()}"
}