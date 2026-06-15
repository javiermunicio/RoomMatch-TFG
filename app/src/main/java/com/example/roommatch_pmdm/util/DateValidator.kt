package com.example.roommatch_pmdm.utils

object DateValidator {

    private val DATE_REGEX = Regex("""^\d{2}/\d{2}/\d{4}$""")

    /**
     * Valida que el String tenga formato DD/MM/YYYY y que la fecha sea real.
     * Devuelve null si es válida, o un mensaje de error si no lo es.
     */
    fun validate(date: String): String? {
        if (!DATE_REGEX.matches(date)) {
            return "Formato de fecha incorrecto (DD/MM/AAAA)"
        }

        val parts = date.split("/")
        val day   = parts[0].toIntOrNull() ?: return "Día inválido"
        val month = parts[1].toIntOrNull() ?: return "Mes inválido"
        val year  = parts[2].toIntOrNull() ?: return "Año inválido"

        if (month < 1 || month > 12) {
            return "El mes debe estar entre 01 y 12"
        }

        if (year < 2000 || year > 2100) {
            return "El año debe estar entre 2000 y 2100"
        }

        val maxDay = daysInMonth(month, year)
        if (day < 1 || day > maxDay) {
            return "El mes $month/$year solo tiene $maxDay días"
        }

        return null
    }

    private fun daysInMonth(month: Int, year: Int): Int = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11            -> 30
        2                      -> if (isLeapYear(year)) 29 else 28
        else                   -> 0
    }

    private fun isLeapYear(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}