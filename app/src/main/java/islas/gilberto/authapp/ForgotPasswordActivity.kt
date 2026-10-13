package islas.gilberto.authapp

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val email = findViewById<EditText>(R.id.etRecoveryEmail)

        val btnSend = findViewById<Button>(R.id.btnSendRecovery)
        val btnCancel = findViewById<Button>(R.id.btnCancel)

        // Simular el envío del correo
        btnSend.setOnClickListener {

            val correo = email.text.toString().trim()

            if (correo.isEmpty()) {

                email.error = "Ingresa tu correo electrónico"
                email.requestFocus()

            } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {

                email.error = "Correo electrónico inválido"
                email.requestFocus()

            } else {

                Toast.makeText(
                    this,
                    "Se envió un enlace de recuperación a $correo",
                    Toast.LENGTH_LONG
                ).show()

                finish()
            }
        }

        // Cancelar y regresar al login
        btnCancel.setOnClickListener {
            finish()
        }
    }
}
