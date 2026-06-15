package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class ChurchViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = ChurchRepository(db)

    // Exposed Flows from database
    val households: StateFlow<List<Household>> = repository.households
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contributions: StateFlow<List<Contribution>> = repository.contributions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ceremonies: StateFlow<List<Ceremony>> = repository.ceremonies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Interactive States
    private val _selectedHouseholdId = MutableStateFlow<Int?>(null)
    val selectedHouseholdId: StateFlow<Int?> = _selectedHouseholdId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDetroitMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    val selectedDetroitMonth: StateFlow<Int> = _selectedDetroitMonth.asStateFlow()

    private val _selectedDetroitYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedDetroitYear: StateFlow<Int> = _selectedDetroitYear.asStateFlow()

    // Map of Month-Year string -> submission date long
    private val _submittedReports = MutableStateFlow<Map<String, Long>>(emptyMap())
    val submittedReports: StateFlow<Map<String, Long>> = _submittedReports.asStateFlow()

    init {
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
    }

    // Search and Filters
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectHousehold(id: Int?) {
        _selectedHouseholdId.value = id
    }

    fun selectDetroitDate(month: Int, year: Int) {
        _selectedDetroitMonth.value = month
        _selectedDetroitYear.value = year
    }

    // Household management
    fun addHousehold(
        familyName: String,
        headName: String,
        phone: String,
        email: String,
        envelopeNumber: Int,
        memberNames: String,
        pledgeAmount: Double,
        weeklyEnvelopesSent: Boolean,
        capitalEnvelopesSent: Boolean
    ) {
        viewModelScope.launch {
            val hh = Household(
                familyName = familyName.trim(),
                headName = headName.trim(),
                phone = phone.trim(),
                email = email.trim(),
                envelopeNumber = envelopeNumber,
                memberNames = memberNames.trim(),
                pledgeAmount = pledgeAmount,
                pledgeRemaining = pledgeAmount, // initially remaining is pledge limit
                weeklyEnvelopesSent = weeklyEnvelopesSent,
                capitalEnvelopesSent = capitalEnvelopesSent
            )
            repository.insertHousehold(hh)
        }
    }

    fun updateHousehold(household: Household) {
        viewModelScope.launch {
            repository.updateHousehold(household)
        }
    }

    fun deleteHousehold(household: Household) {
        viewModelScope.launch {
            repository.deleteHousehold(household)
        }
    }

    // Contribution management
    fun addContribution(
        householdId: Int?,
        envelopeNumber: Int?,
        amount: Double,
        type: String,
        paymentMethod: String,
        date: Long,
        notes: String?
    ) {
        viewModelScope.launch {
            val c = Contribution(
                householdId = householdId,
                envelopeNumber = envelopeNumber,
                amount = amount,
                type = type,
                paymentMethod = paymentMethod,
                date = date,
                notes = notes?.trim()
            )
            repository.addContribution(c)
        }
    }

    fun deleteContribution(contribution: Contribution) {
        viewModelScope.launch {
            repository.deleteContribution(contribution)
        }
    }

    // Ceremony management
    fun addCeremony(
        type: String,
        date: Long,
        primaryPerson: String,
        additionalPerson: String?,
        sponsorGodmother: String?,
        sponsorGodfather: String?,
        officiant: String,
        infantOrAdult: String?,
        classesCompleted: Boolean,
        weeksCompleted: Int,
        notes: String?
    ) {
        viewModelScope.launch {
            val cr = Ceremony(
                type = type,
                date = date,
                primaryPerson = primaryPerson.trim(),
                additionalPerson = additionalPerson?.trim(),
                sponsorGodmother = sponsorGodmother?.trim(),
                sponsorGodfather = sponsorGodfather?.trim(),
                officiant = officiant,
                infantOrAdult = infantOrAdult,
                classesCompleted = classesCompleted,
                weeksCompleted = weeksCompleted,
                notes = notes?.trim()
            )
            repository.insertCeremony(cr)
        }
    }

    fun updateCeremony(ceremony: Ceremony) {
        viewModelScope.launch {
            repository.updateCeremony(ceremony)
        }
    }

    fun deleteCeremony(ceremony: Ceremony) {
        viewModelScope.launch {
            repository.deleteCeremony(ceremony)
        }
    }

    // Submit report mock for Detroit Regional Office
    fun submitDetroitReport(month: Int, year: Int) {
        val key = "$month-$year"
        val currentMap = _submittedReports.value.toMutableMap()
        currentMap[key] = System.currentTimeMillis()
        _submittedReports.value = currentMap
    }
}
