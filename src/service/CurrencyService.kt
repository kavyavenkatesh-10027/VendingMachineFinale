package service

import exception.IllegalNegativeValueException
import exception.InsufficientDenominationForChangeException
import model.Drawer
import model.enum.IndianCurrency
import java.math.BigDecimal
import java.util.EnumMap

object CurrencyService {

    fun acceptPayment(drawer: Drawer, inserted: Map<IndianCurrency, Int>): BigDecimal {
        var total = BigDecimal.ZERO
        for ((denom, count) in inserted) {
            drawer.add(denom, count)
            total += BigDecimal.valueOf(denom.value.toLong()) * BigDecimal.valueOf(count.toLong())
        }
        return total
    }

    // Why? Greedy algorithm i.e. largest denomination first
    fun makeChange(drawer: Drawer, changeAmount: BigDecimal): Map<IndianCurrency, Int> {
        if (changeAmount < BigDecimal.ZERO) throw IllegalNegativeValueException("Change amount")
        if (changeAmount.compareTo(BigDecimal.ZERO) == 0) return emptyMap()

        val change = EnumMap<IndianCurrency, Int>(IndianCurrency::class.java)
        var remaining = changeAmount
        val denoms = IndianCurrency.entries

        for (i in denoms.indices.reversed()) {
            if (remaining == BigDecimal.ZERO) break
            val denom = denoms[i]
            val denomValue = BigDecimal.valueOf(denom.value.toLong())
            val canUse = remaining.divideToIntegralValue(denomValue).toInt()
            val use = minOf(canUse, drawer.getCount(denom))
            if (use > 0) {
                change[denom] = use
                remaining -= denomValue * BigDecimal.valueOf(use.toLong())
            }
        }

        if (remaining != BigDecimal.ZERO) throw InsufficientDenominationForChangeException(changeAmount)

        for ((denom, count) in change) drawer.deduct(denom, count)
        return change
    }

    fun refund(drawer: Drawer, inserted: Map<IndianCurrency, Int>) {
        for ((denom, count) in inserted) drawer.deduct(denom, count)
    }

    fun addToDrawer(drawer: Drawer, denomination: IndianCurrency, count: Int) {
        require(count > 0) { "Count must be greater than zero" }
        drawer.add(denomination, count)
    }
}