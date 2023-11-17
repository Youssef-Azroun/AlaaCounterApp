package com.example.alaacounter


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AllWeeks : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WeekAdapter
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_weeks)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Retrieve weekly data from Firestore
        retrieveWeeklyDataFromFirestore()
    }

    private fun retrieveWeeklyDataFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val documentRef = firestore.collection("WeeklyData").document(user.uid)
            documentRef.get().addOnSuccessListener { documentSnapshot ->
                val weeklyDataList =
                    documentSnapshot.data?.get("weeklyDataList") as? List<Map<String, Any>> ?: emptyList()

                // Convert Firestore data to WeekData objects
                val convertedList = weeklyDataList.map {
                    WeekData(
                        weekNumber = (it["weekNumber"] as? Long)?.toInt() ?: 0,
                        greenButtonCount = (it["greenButtonCount"] as? Long)?.toInt() ?: 0,
                        resultText = it["resultText"] as? String ?: ""
                        // hej
                    )
                }

                // Initialize the adapter with the retrieved data
                adapter = WeekAdapter(convertedList)
                recyclerView.adapter = adapter
            }.addOnFailureListener { e ->
                // Handle error
            }
        }
    }

}


