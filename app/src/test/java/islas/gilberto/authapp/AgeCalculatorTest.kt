package islas.gilberto.authapp

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class AgeCalculatorTest {
    @Test
    fun leapBirthYearDoesNotDelayEighteenthBirthday() {
        assertEquals(18, AgeCalculator.calculate(date(2008, 3, 1), date(2026, 3, 1)))
    }

    @Test
    fun leapCurrentYearDoesNotAcceptMinorOneDayEarly() {
        assertEquals(17, AgeCalculator.calculate(date(2006, 3, 1), date(2024, 2, 29)))
    }

    @Test
    fun dayBeforeEighteenthBirthdayIsSeventeen() {
        assertEquals(17, AgeCalculator.calculate(date(2008, 10, 13), date(2026, 10, 12)))
    }

    @Test
    fun dayAfterEighteenthBirthdayIsEighteen() {
        assertEquals(18, AgeCalculator.calculate(date(2008, 10, 11), date(2026, 10, 12)))
    }

    @Test
    fun birthdayInPreviousMonthHasAlreadyPassed() {
        assertEquals(18, AgeCalculator.calculate(date(2008, 9, 30), date(2026, 10, 1)))
    }

    private fun date(year: Int, month: Int, day: Int) = Calendar.getInstance().apply {
        clear()
        set(year, month - 1, day)
    }
}
