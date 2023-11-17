package com.example.alaacounter

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainFunctions : AppCompatActivity() {
    private lateinit var textViewResult: TextView
    private var greenButtonCount = 0
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var sharedPreferences: SharedPreferences
    private val weeklyDataList = mutableListOf<WeekData>()
    private var weekNumber = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_functions)


        sharedPreferences = getSharedPreferences("selectedDays", Context.MODE_PRIVATE)


        val buttonMonday: Button = findViewById(R.id.buttonMonday)
        val buttonTuesday: Button = findViewById(R.id.buttonTuesday)
        val buttonWednesday: Button = findViewById(R.id.buttonWednesday)
        val buttonThursday: Button = findViewById(R.id.buttonThursday)
        val buttonFriday: Button = findViewById(R.id.buttonFriday)
        val buttonSaturday: Button = findViewById(R.id.buttonSaturday)
        val buttonSunday: Button = findViewById(R.id.buttonSunday)

        val buttons = listOf(buttonMonday, buttonTuesday, buttonWednesday, buttonThursday, buttonFriday, buttonSaturday, buttonSunday)
        textViewResult = findViewById(R.id.textViewResult)

        val historyButton: Button = findViewById(R.id.btnAllWeeks)
        historyButton.setOnClickListener {
            // Navigate to AllWeeks activity
            val intent = Intent(this, AllWeeks::class.java)
            startActivity(intent)
        }

        // Reset button setup
        val resetButton: Button = findViewById(R.id.Reset)
        resetButton.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            user?.let {
                // Show confirmation dialog before resetting
                val dialog = AlertDialog.Builder(this, R.style.RedPositiveButtonTheme)
                    .setTitle("Reset Confirmation")
                    .setMessage("Are you sure you want to reset the whole week?")
                    .setPositiveButton("Yes") { _, _ ->
                        // User clicked Yes, proceed with the reset
                        textViewResult.text = "0/7=0.0"

                        // Fetch current document state
                        val documentRef = firestore.collection("Days").document(user?.uid ?: "")
                        documentRef.get().addOnSuccessListener { documentSnapshot ->
                            val currentDays = documentSnapshot.data?.get("days") as? Map<String, Boolean> ?: emptyMap()

                            // Calculate the number of green buttons before the reset
                            greenButtonCount = currentDays.count { it.value }

                            // Update only the reset day to false
                            val updatedDays = currentDays.toMutableMap()
                            for (button in buttons) {
                                val dayOfWeek = getDayOfWeek(button.id)
                                updatedDays[dayOfWeek] = false
                            }

                            // Update Firestore with the reset day
                            updateFirestore(user.uid, updatedDays)

                            // Update UI for the reset day buttons
                            for (button in buttons) {
                                val dayOfWeek = getDayOfWeek(button.id)
                                button.tag = false
                                updateButtonColor(button)
                                button.isEnabled = true // Make all buttons clickable
                            }

                            // Update result text
                            //updateResultText()

                            // Save button states to SharedPreferences
                            with(sharedPreferences.edit()) {
                                for (button in buttons) {
                                    val dayOfWeek = getDayOfWeek(button.id)
                                    putBoolean(dayOfWeek, false)
                                }
                                apply()
                            }

                            // Add the weekly data to the list
                            weeklyDataList.add(
                                WeekData(
                                    weekNumber = ++weekNumber, // Increment the week number
                                    greenButtonCount = greenButtonCount,
                                    resultText = "0/7=0.0"
                                )
                            )

                            // Upload the updated list to Firestore
                            uploadWeeklyDataToFirestore()
                        }.addOnFailureListener { e ->
                            // Handle error
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        // User clicked No, do nothing
                    }
                    .show()

                // Set the red color for the "Yes" button
                dialog.getButton(DialogInterface.BUTTON_POSITIVE)?.setTextColor(getColor(R.color.red_color))
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(getColor(R.color.black_color))
            }
           // getResultText()
        }



        for (button in buttons) {
            val dayOfWeek = getDayOfWeek(button.id)

            // Fetch current document state when the activity starts
            val documentRef = firestore.collection("Days").document(auth.currentUser?.uid ?: "")
            documentRef.get().addOnSuccessListener { documentSnapshot ->
                val currentDays = documentSnapshot.data?.get("days") as? Map<String, Boolean> ?: emptyMap()

                // Check if the value of the day is true
                val isDaySelected = currentDays[dayOfWeek] == true

                // Update the corresponding day
                val updatedDays = currentDays.toMutableMap()

                // Update UI
                button.tag = isDaySelected
                updateButtonColor(button)
                greenButtonCount = updatedDays.count { it.value }
                updateResultText()

                // Save button state to SharedPreferences
                with(sharedPreferences.edit()) {
                    putBoolean(dayOfWeek, isDaySelected)
                    apply()
                }

                // Make the button unclickable if it's green, and clickable otherwise
                button.isEnabled = !isDaySelected

                // Set a click listener for the button
                button.setOnClickListener {
                    val user = auth.currentUser
                    user?.let {
                        // Fetch current document state
                        val documentRef = firestore.collection("Days").document(user.uid)
                        documentRef.get().addOnSuccessListener { documentSnapshot ->
                            val currentDays = documentSnapshot.data?.get("days") as? Map<String, Boolean> ?: emptyMap()

                            // Check if the value of the day is false
                            val isDaySelected = currentDays[dayOfWeek] == true

                            // Show confirmation dialog if the value is false
                            if (!isDaySelected) {
                                showConfirmationDialog(user.uid, currentDays, dayOfWeek, button)
                            }
                        }.addOnFailureListener { e ->
                            // Handle error
                        }
                    }
                }
            }.addOnFailureListener { e ->
                // Handle error
            }
        }

    }

    private fun showConfirmationDialog(userId: String, currentDays: Map<String, Boolean>, dayOfWeek: String, button: Button) {
        AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Are you sure you want to choose this day?")
            .setPositiveButton("Yes") { _, _ ->
                // User clicked Yes, update the value to true
                updateValueInDatabase(userId, currentDays, dayOfWeek, true, button)
            }
            .setNegativeButton("No") { _, _ ->
                // User clicked No, do nothing
            }
            .show()
    }

    private fun updateValueInDatabase(userId: String, currentDays: Map<String, Boolean>, dayOfWeek: String, newValue: Boolean, button: Button) {
        // Update the corresponding day
        val updatedDays = currentDays.toMutableMap()
        updatedDays[dayOfWeek] = newValue

        // Update Firestore with the entire map
        updateFirestore(userId, updatedDays)

        // Update UI
        button.tag = newValue
        updateButtonColor(button)
        greenButtonCount = updatedDays.count { it.value }
        updateResultText()

        // Save button state to SharedPreferences
        with(sharedPreferences.edit()) {
            putBoolean(dayOfWeek, newValue)
            apply()
        }

        // Make the button unclickable if it's green, and clickable otherwise
        button.isEnabled = !newValue
    }

    private fun updateFirestore(userId: String, days: Map<String, Boolean>) {
        val document = firestore.collection("Days").document(userId)
        val data = mapOf("days" to days)

        document.set(data)
            .addOnSuccessListener {
                // Handle success
            }
            .addOnFailureListener { e ->
                // Handle error
            }
    }

    private fun getDayOfWeek(buttonId: Int): String {
        return when (buttonId) {
            R.id.buttonMonday -> "Monday"
            R.id.buttonTuesday -> "Tuesday"
            R.id.buttonWednesday -> "Wednesday"
            R.id.buttonThursday -> "Thursday"
            R.id.buttonFriday -> "Friday"
            R.id.buttonSaturday -> "Saturday"
            R.id.buttonSunday -> "Sunday"
            else -> throw IllegalArgumentException("Invalid button ID")
        }
    }

    private fun updateResultText() {
        // Update the result text based on the green button count
        val resultText = getResultText()
        textViewResult.text = resultText
    }

    private fun getResultText(): String {
        return "$greenButtonCount/7=${greenButtonCount.toDouble() / 7.0}"
    }

    private fun updateButtonColor(button: Button) {
        val isSelected = button.tag as? Boolean ?: false
        val colorResId = if (isSelected) R.color.button_selected_color else R.color.button_default_color
        button.backgroundTintList = getColorStateList(colorResId)
    }

    // Add a function to upload the list to Firestore
    // Add a function to upload the list to Firestore
    private fun uploadWeeklyDataToFirestore() {
        val user = auth.currentUser
        user?.let {
            val documentRef = firestore.collection("WeeklyData").document(user.uid)

            // Fetch the current data from Firestore
            documentRef.get().addOnSuccessListener { documentSnapshot ->
                val currentWeeklyDataList = documentSnapshot.data?.get("weeklyDataList") as? List<Map<String, Any>> ?: emptyList()

                // Append the new data to the existing list
                val updatedWeeklyDataList = currentWeeklyDataList.toMutableList()
                updatedWeeklyDataList.add(
                    mapOf(
                        "weekNumber" to weekNumber,
                        "greenButtonCount" to greenButtonCount,
                        "resultText" to getResultText()
                    )
                )

                // Update Firestore with the updated list
                val data = mapOf("weeklyDataList" to updatedWeeklyDataList)
                documentRef.set(data)
                    .addOnSuccessListener {
                        // Handle success
                    }
                    .addOnFailureListener { e ->
                        // Handle error
                    }
            }.addOnFailureListener { e ->
                // Handle error
            }
        }
    }


}

