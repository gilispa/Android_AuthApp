package islas.gilberto.authapp

import java.util.Calendar

internal object AgeCalculator {
    fun calculate(birthDate: Calendar, today: Calendar): Int {
        var age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
        val month = today.get(Calendar.MONTH)
        val birthMonth = birthDate.get(Calendar.MONTH)
        if (month < birthMonth ||
            (month == birthMonth && today.get(Calendar.DAY_OF_MONTH) < birthDate.get(Calendar.DAY_OF_MONTH))
        ) {
            age--
        }
        return age
    }
}
