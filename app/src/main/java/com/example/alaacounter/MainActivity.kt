package com.example.alaacounter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

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

            // Implement your login logic, for example, using Firebase Authentication
            // If login is successful, navigate to MainFunctions activity
            if (loginSuccessful(email, password)) {
                val intent = Intent(this, MainFunctions::class.java)
                startActivity(intent)
            } else {
                // Handle login failure, show a toast or appropriate message
                Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginSuccessful(email: String, password: String): Boolean {
        // Implement your login logic here, for example, using Firebase Authentication
        // Return true if login is successful, otherwise return false
        // Note: Replace this with your actual login implementation
        return true
    }
}
