package islas.gilberto.authapp

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.inspector.WindowInspector
import android.widget.DatePicker
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class AuthFlowTest {
    @Test
    fun loginLaunchesAndRejectsInvalidEmail() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setText(R.id.etEmail, "correo-invalido")
                activity.setText(R.id.etPassword, "prueba123")
                activity.click(R.id.btnLogin)
                assertEquals("Correo electrónico inválido", activity.findViewById<EditText>(R.id.etEmail).error)
            }
        }
    }

    @Test
    fun registrationAndRecoveryReturnToLogin() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val registrationMonitor = instrumentation.addMonitor(RegisterActivity::class.java.name, null, false)
        val recoveryMonitor = instrumentation.addMonitor(ForgotPasswordActivity::class.java.name, null, false)
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.onActivity { it.click(R.id.tvRegister) }
                val registration = instrumentation.waitForMonitorWithTimeout(registrationMonitor, 15000)
                assertNotNull("El enlace debe abrir el registro", registration)
                instrumentation.runOnMainSync {
                    assertNotNull(registration.findViewById<View>(R.id.btnRegister))
                    registration.click(R.id.tvBackLogin)
                    assertTrue(registration.isFinishing)
                }
                scenario.onActivity { it.click(R.id.tvForgotPassword) }
                val recovery = instrumentation.waitForMonitorWithTimeout(recoveryMonitor, 15000)
                assertNotNull("El enlace debe abrir la recuperación", recovery)
                instrumentation.runOnMainSync {
                    recovery.click(R.id.btnCancel)
                    assertTrue(recovery.isFinishing)
                }
                scenario.onActivity { assertNotNull(it.findViewById<View>(R.id.btnLogin)) }
            }
        } finally {
            instrumentation.removeMonitor(registrationMonitor)
            instrumentation.removeMonitor(recoveryMonitor)
        }
    }

    @Test
    fun recoveryRejectsInvalidEmail() {
        ActivityScenario.launch(ForgotPasswordActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setText(R.id.etRecoveryEmail, "invalido")
                activity.click(R.id.btnSendRecovery)
                assertEquals("Correo electrónico inválido", activity.findViewById<EditText>(R.id.etRecoveryEmail).error)
                assertFalse(activity.isFinishing)
            }
        }
    }

    @Test
    fun recoveryAcceptsValidEmail() {
        ActivityScenario.launch(ForgotPasswordActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.setText(R.id.etRecoveryEmail, "prueba@example.com")
                activity.click(R.id.btnSendRecovery)
                assertTrue(activity.isFinishing)
            }
        }
    }

    @Test
    @SdkSuppress(minSdkVersion = 29)
    fun registrationRejectsMismatchedPasswords() {
        ActivityScenario.launch(RegisterActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                fillRegistration(activity, "diferente")
                selectBirthDate(activity, 20)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                assertTrue(activity.findViewById<EditText>(R.id.etBirthDate).text.isNotEmpty())
                activity.click(R.id.btnRegister)
                assertEquals("Las contraseñas no coinciden", activity.findViewById<EditText>(R.id.etConfirmPassword).error)
                assertFalse(activity.isFinishing)
            }
        }
    }

    @Test
    @SdkSuppress(minSdkVersion = 29)
    fun registrationRejectsMinor() {
        ActivityScenario.launch(RegisterActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                fillRegistration(activity)
                selectBirthDate(activity, 17)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                assertTrue(activity.findViewById<EditText>(R.id.etBirthDate).text.isNotEmpty())
                activity.click(R.id.btnRegister)
                assertFalse(activity.isFinishing)
            }
        }
    }

    @Test
    @SdkSuppress(minSdkVersion = 29)
    fun adultCanRegisterAfterActivityRecreation() {
        ActivityScenario.launch(RegisterActivity::class.java).use { scenario ->
            var selectedDate = ""
            scenario.onActivity { activity ->
                fillRegistration(activity)
                selectBirthDate(activity, 20)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                selectedDate = activity.findViewById<EditText>(R.id.etBirthDate).text.toString()
                assertTrue(selectedDate.isNotEmpty())
            }
            scenario.recreate()
            scenario.onActivity { activity ->
                assertEquals(selectedDate, activity.findViewById<EditText>(R.id.etBirthDate).text.toString())
                activity.click(R.id.btnRegister)
                assertTrue("El adulto debe poder registrarse tras recrear la pantalla", activity.isFinishing)
            }
        }
    }

    private fun fillRegistration(activity: Activity, confirmation: String = "prueba123") {
        activity.setText(R.id.etName, "Usuario de prueba")
        activity.setText(R.id.etRegisterEmail, "prueba@example.com")
        activity.setText(R.id.etRegisterPassword, "prueba123")
        activity.setText(R.id.etConfirmPassword, confirmation)
    }

    @RequiresApi(29)
    private fun selectBirthDate(activity: Activity, yearsAgo: Int) {
        val birthDate = Calendar.getInstance().apply { add(Calendar.YEAR, -yearsAgo) }
        activity.click(R.id.etBirthDate)
        val window = WindowInspector.getGlobalWindowViews().first { findDatePicker(it) != null }
        val picker = requireNotNull(findDatePicker(window))
        picker.updateDate(birthDate.get(Calendar.YEAR), birthDate.get(Calendar.MONTH), birthDate.get(Calendar.DAY_OF_MONTH))
        window.findViewById<View>(android.R.id.button1).performClick()
    }

    private fun findDatePicker(view: View): DatePicker? {
        if (view is DatePicker) return view
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                findDatePicker(view.getChildAt(index))?.let { return it }
            }
        }
        return null
    }

    private fun Activity.setText(id: Int, value: String) = findViewById<EditText>(id).setText(value)
    private fun Activity.click(id: Int) = findViewById<View>(id).performClick()
}
