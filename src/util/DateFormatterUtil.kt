package util

import java.time.format.DateTimeFormatter
import java.util.Locale

class DateFormatterUtil {
    companion object {
        val purchaseDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss 'hrs'", Locale.ENGLISH)
    }
}