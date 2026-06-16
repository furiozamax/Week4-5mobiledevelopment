package com.example.data

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseSyncManager {
    private var initialized = false

    fun getFirebaseProjectId(): String {
        val pId = BuildConfig.FIREBASE_PROJECT_ID
        return if (pId.isEmpty() || pId == "MY_FIREBASE_PROJECT_ID") "abc-church-59d33" else pId
    }

    fun isConfigured(): Boolean {
        val apiKey = BuildConfig.FIREBASE_API_KEY
        val projectId = getFirebaseProjectId()
        val appId = BuildConfig.FIREBASE_APP_ID

        return apiKey.isNotEmpty() && apiKey != "MY_FIREBASE_API_KEY" &&
                projectId.isNotEmpty() && projectId != "MY_FIREBASE_PROJECT_ID" &&
                appId.isNotEmpty() && appId != "MY_FIREBASE_APP_ID"
    }

    fun isInitialized(): Boolean = initialized

    fun initialize(context: Context): Boolean {
        if (initialized) return true
        if (!isConfigured()) {
            Log.w("FirebaseSyncManager", "Firebase credentials are not configured inside environment secrets yet.")
            return false
        }

        try {
            // If already initialized by system, mark as such
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                initialized = true
                return true
            }

            val options = FirebaseOptions.Builder()
                .setApiKey(BuildConfig.FIREBASE_API_KEY)
                .setProjectId(getFirebaseProjectId())
                .setApplicationId(BuildConfig.FIREBASE_APP_ID)
                .build()

            FirebaseApp.initializeApp(context, options)
            initialized = true
            Log.d("FirebaseSyncManager", "Successfully initialized Firebase App programmatically.")
            return true
        } catch (e: Exception) {
            Log.e("FirebaseSyncManager", "Failed to initialize Firebase programmatically", e)
            initialized = false
            return false
        }
    }

    fun getFirestore(): FirebaseFirestore? {
        return if (initialized) {
            try {
                FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                Log.e("FirebaseSyncManager", "Failed to retrieve Firestore instance", e)
                null
            }
        } else {
            null
        }
    }

    fun backupToFirestore(
        households: List<Household>,
        contributions: List<Contribution>,
        ceremonies: List<Ceremony>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = getFirestore() ?: run {
            onFailure(Exception("Firebase is not initialized or configured."))
            return
        }

        val batch = db.batch()

        // 1. Queue households
        households.forEach { hh ->
            val docRef = db.collection("households").document(hh.id.toString())
            val data = hashMapOf(
                "id" to hh.id,
                "familyName" to hh.familyName,
                "headName" to hh.headName,
                "phone" to hh.phone,
                "email" to hh.email,
                "envelopeNumber" to hh.envelopeNumber,
                "memberNames" to hh.memberNames,
                "pledgeAmount" to hh.pledgeAmount,
                "pledgeRemaining" to hh.pledgeRemaining,
                "weeklyEnvelopesSent" to hh.weeklyEnvelopesSent,
                "capitalEnvelopesSent" to hh.capitalEnvelopesSent,
                "dateRegistered" to hh.dateRegistered
            )
            batch.set(docRef, data)
        }

        // 2. Queue contributions
        contributions.forEach { c ->
            val docRef = db.collection("contributions").document(c.id.toString())
            val data = hashMapOf(
                "id" to c.id,
                "householdId" to c.householdId,
                "envelopeNumber" to c.envelopeNumber,
                "amount" to c.amount,
                "type" to c.type,
                "paymentMethod" to c.paymentMethod,
                "date" to c.date,
                "notes" to (c.notes ?: "")
            )
            batch.set(docRef, data)
        }

        // 3. Queue ceremonies
        ceremonies.forEach { cr ->
            val docRef = db.collection("ceremonies").document(cr.id.toString())
            val data = hashMapOf(
                "id" to cr.id,
                "type" to cr.type,
                "date" to cr.date,
                "primaryPerson" to cr.primaryPerson,
                "additionalPerson" to (cr.additionalPerson ?: ""),
                "sponsorGodmother" to (cr.sponsorGodmother ?: ""),
                "sponsorGodfather" to (cr.sponsorGodfather ?: ""),
                "officiant" to cr.officiant,
                "infantOrAdult" to (cr.infantOrAdult ?: ""),
                "classesCompleted" to cr.classesCompleted,
                "weeksCompleted" to cr.weeksCompleted,
                "notes" to (cr.notes ?: "")
            )
            batch.set(docRef, data)
        }

        // Execute batch write
        batch.commit()
            .addOnSuccessListener {
                Log.d("FirebaseSyncManager", "Successfully pushed standard database items to Remote Firestore!")
                onSuccess()
            }
            .addOnFailureListener { error ->
                Log.e("FirebaseSyncManager", "Firestore batch upload failure", error)
                onFailure(error)
            }
    }

    fun restoreFromFirestore(
        onSuccess: (List<Household>, List<Contribution>, List<Ceremony>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = getFirestore() ?: run {
            onFailure(Exception("Firebase is not initialized or configured."))
            return
        }

        // Retrieve step-by-step
        db.collection("households").get()
            .addOnSuccessListener { hhSnap ->
                val householdsList = hhSnap.map { doc ->
                    Household(
                        id = (doc.getLong("id") ?: 0).toInt(),
                        familyName = doc.getString("familyName") ?: "",
                        headName = doc.getString("headName") ?: "",
                        phone = doc.getString("phone") ?: "",
                        email = doc.getString("email") ?: "",
                        envelopeNumber = (doc.getLong("envelopeNumber") ?: 0).toInt(),
                        memberNames = doc.getString("memberNames") ?: "",
                        pledgeAmount = doc.getDouble("pledgeAmount") ?: 0.0,
                        pledgeRemaining = doc.getDouble("pledgeRemaining") ?: 0.0,
                        weeklyEnvelopesSent = doc.getBoolean("weeklyEnvelopesSent") ?: true,
                        capitalEnvelopesSent = doc.getBoolean("capitalEnvelopesSent") ?: true,
                        dateRegistered = doc.getLong("dateRegistered") ?: System.currentTimeMillis()
                    )
                }

                db.collection("contributions").get()
                    .addOnSuccessListener { cSnap ->
                        val contributionsList = cSnap.map { doc ->
                            Contribution(
                                id = (doc.getLong("id") ?: 0).toInt(),
                                householdId = doc.getLong("householdId")?.toInt(),
                                envelopeNumber = doc.getLong("envelopeNumber")?.toInt(),
                                amount = doc.getDouble("amount") ?: 0.0,
                                type = doc.getString("type") ?: "Regular Sunday Offering",
                                paymentMethod = doc.getString("paymentMethod") ?: "Check",
                                date = doc.getLong("date") ?: System.currentTimeMillis(),
                                notes = doc.getString("notes")
                            )
                        }

                        db.collection("ceremonies").get()
                            .addOnSuccessListener { crSnap ->
                                val ceremoniesList = crSnap.map { doc ->
                                    Ceremony(
                                        id = (doc.getLong("id") ?: 0).toInt(),
                                        type = doc.getString("type") ?: "Baptism",
                                        date = doc.getLong("date") ?: System.currentTimeMillis(),
                                        primaryPerson = doc.getString("primaryPerson") ?: "",
                                        additionalPerson = doc.getString("additionalPerson"),
                                        sponsorGodmother = doc.getString("sponsorGodmother"),
                                        sponsorGodfather = doc.getString("sponsorGodfather"),
                                        officiant = doc.getString("officiant") ?: "",
                                        infantOrAdult = doc.getString("infantOrAdult"),
                                        classesCompleted = doc.getBoolean("classesCompleted") ?: false,
                                        weeksCompleted = (doc.getLong("weeksCompleted") ?: 0).toInt(),
                                        notes = doc.getString("notes")
                                    )
                                }

                                onSuccess(householdsList, contributionsList, ceremoniesList)
                            }
                            .addOnFailureListener(onFailure)
                    }
                    .addOnFailureListener(onFailure)
            }
            .addOnFailureListener(onFailure)
    }
}
