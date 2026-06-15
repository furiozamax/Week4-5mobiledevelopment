package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "households")
data class Household(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val familyName: String,
    val headName: String,
    val phone: String,
    val email: String,
    val envelopeNumber: Int, // pre-numbered collections envelopes
    val memberNames: String, // comma-separated names of family members
    val pledgeAmount: Double, // capital campaign 3-year pledge commitment
    val pledgeRemaining: Double, // remaining pledge balance
    val weeklyEnvelopesSent: Boolean = true,
    val capitalEnvelopesSent: Boolean = true,
    val dateRegistered: Long = System.currentTimeMillis()
)

@Entity(tableName = "contributions")
data class Contribution(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val householdId: Int?, // null for loose offerings
    val envelopeNumber: Int?, // null for loose offerings
    val amount: Double,
    val type: String, // "Regular Sunday Offering", "Capital Improvement"
    val paymentMethod: String, // "Cash", "Check"
    val date: Long = System.currentTimeMillis(),
    val notes: String? = null
)

@Entity(tableName = "ceremonies")
data class Ceremony(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "Baptism", "Wedding", "Funeral"
    val date: Long,
    val primaryPerson: String, // name of baptized person, deceased, or husband/wife
    val additionalPerson: String? = null, // e.g. spouse for wedding, parents for baptism
    val sponsorGodmother: String? = null, // for baptisms
    val sponsorGodfather: String? = null, // for baptisms
    val officiant: String, // "Reverend Timothy Beck" or "Reverend Jason Howard"
    val infantOrAdult: String? = null, // "Infant" or "Adult"
    val classesCompleted: Boolean = false, // preparation classes
    val weeksCompleted: Int = 0, // weekly classes progress (baptism: 3 months, wedding: 2 months)
    val notes: String? = null
)
