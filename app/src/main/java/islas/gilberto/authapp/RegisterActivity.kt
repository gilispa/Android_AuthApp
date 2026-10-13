package islas.gilberto.authapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class RegisterActivity : AppCompatActivity() {

    private var birthDate: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        if (savedInstanceState?.containsKey(BIRTH_DATE_KEY) == true) {
            birthDate = Calendar.getInstance().apply {
                timeInMillis = savedInstanceState.getLong(BIRTH_DATE_KEY)
            }
        }

        val name = findViewById<EditText>(R.id.etName)
        val email = findViewById<EditText>(R.id.etRegisterEmail)
        val password = findViewById<EditText>(R.id.etRegisterPassword)
        val confirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val date = findViewById<EditText>(R.id.etBirthDate)

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBack = findViewById<TextView>(R.id.tvBackLogin)

        // Abrir calendario para elegir la fecha
        date.setOnClickListener {

            val today = Calendar.getInstance()
            val selectedDate = birthDate ?: today

            val year = selectedDate.get(Calendar.YEAR)
            val month = selectedDate.get(Calendar.MONTH)
            val day = selectedDate.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->

                    birthDate = Calendar.getInstance().apply {
                        set(
                            selectedYear,
                            selectedMonth,
                            selectedDay,
                            0,
                            0,
                            0
                        )
                        set(Calendar.MILLISECOND, 0)
                    }

                    date.setText(
                        "%02d/%02d/%04d".format(
                            selectedDay,
                            selectedMonth + 1,
                            selectedYear
                        )
                    )
                },
                year,
                month,
                day
            )

            // No permitir fechas futuras
            datePicker.datePicker.maxDate = today.timeInMillis

            datePicker.show()
        }

        // Validar formulario al registrarse
        btnRegister.setOnClickListener {

            val nombre = name.text.toString().trim()
            val correo = email.text.toString().trim()
            val contrasena = password.text.toString()
            val confirmar = confirmPassword.text.toString()
            val fecha = date.text.toString()

            if (nombre.isEmpty() ||
                correo.isEmpty() ||
                contrasena.isEmpty() ||
                confirmar.isEmpty() ||
                fecha.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Todos los campos son obligatorios",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // Validar formato del correo
            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {

                email.error = "Correo electrónico inválido"
                email.requestFocus()

                return@setOnClickListener
            }

            // Validar que las contraseñas coincidan
            if (contrasena != confirmar) {

                confirmPassword.error = "Las contraseñas no coinciden"

                Toast.makeText(
                    this,
                    "Las contraseñas deben ser iguales",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // Calcular edad usando fecha de nacimiento
            val nacimiento = birthDate ?: run {
                Toast.makeText(
                    this,
                    "Selecciona tu fecha de nacimiento",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val hoy = Calendar.getInstance()

            val edad = AgeCalculator.calculate(nacimiento, hoy)

            // Verificar mayoría de edad
            if (edad < 18) {

                Toast.makeText(
                    this,
                    "Debes tener al menos 18 años",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            // Todas las validaciones pasaron
            Toast.makeText(
                this,
                "Registro exitoso",
                Toast.LENGTH_LONG
            ).show()

            finish()
        }

        // Regresar al login
        btnBack.setOnClickListener {
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        birthDate?.let { outState.putLong(BIRTH_DATE_KEY, it.timeInMillis) }
        super.onSaveInstanceState(outState)
    }

    private companion object {
        const val BIRTH_DATE_KEY = "birth_date"
    }
}
