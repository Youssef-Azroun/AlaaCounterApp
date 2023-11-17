package com.example.alaacounter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonCreateAccount: Button = findViewById(R.id.buttonCreateAccount)
        val buttonLogin: Button = findViewById(R.id.buttonLogin)
        val editTextEmail: EditText = findViewById(R.id.editTextEmail)
        val editTextPassword: EditText = findViewById(R.id.editTextPassword)

        buttonCreateAccount.setOnClickListener {
            // Handle create account logic here
            val intent = Intent(this, CreateAccount::class.java)
            startActivity(intent)
        }

        buttonLogin.setOnClickListener {
            // Handle login logic here
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginWithEmailAndPassword(email, password) { loginResult ->
                    if (loginResult) {
                        // Login successful, navigate to MainFunctions activity
                        val intent = Intent(this, MainFunctions::class.java)
                        startActivity(intent)
                    } else {
                        // Handle login failure, show a toast or appropriate message
                        Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // Show a toast if email or password is empty
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginWithEmailAndPassword(email: String, password: String, callback: (Boolean) -> Unit) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login successful
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        // Optionally, you can check additional conditions here
                        // such as verifying the email or any custom user attributes
                        callback.invoke(true)
                    } else {
                        callback.invoke(false)
                    }
                } else {
                    // If login fails, handle the error
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()

                    callback.invoke(false)
                }
            }
    }
}
