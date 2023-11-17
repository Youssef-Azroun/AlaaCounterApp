package com.example.alaacounter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateAccount : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonCreateAccount: Button
    private val firestore = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        // Initialize views
        editTextName = findViewById(R.id.editTextName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount)

        // Set click listener on the button
        buttonCreateAccount.setOnClickListener {
            val name = editTextName.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            // Create user with email and password using Firebase Authentication
            // Create user with email and password using Firebase Authentication
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // User creation successful
                        val user = FirebaseAuth.getInstance().currentUser
                        // Store additional information (name) in Firestore using the user's uid
                        storeUserData(user?.uid, name, email)
                    } else {
                        // If user creation fails, handle the error
                        Toast.makeText(
                            this,
                            "Account creation failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

        }
    }

    private fun storeUserData(userId: String?, name: String, email: String) {
        // Add code to store user data in Firestore using the userId as the document ID
        // Use the 'userId' to identify the user in Firestore
        // This step depends on your Firestore database structure and implementation

        // Example:
        val documentRef = firestore.collection("Users").document(userId ?: "")
        val userData = mapOf(
            "name" to name,
            "email" to email
            // Add any other user-related data
        )

        documentRef.set(userData)
            .addOnSuccessListener {
                // Handle success
            }
            .addOnFailureListener { e ->
                // Handle error
            }
    }

}
