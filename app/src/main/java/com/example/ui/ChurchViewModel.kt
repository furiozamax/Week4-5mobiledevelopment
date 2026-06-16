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
            FirebaseSyncManager.initialize(application)
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

    // User Authentication state
    private val _currentUser = MutableStateFlow<UserSession?>(null)
    val currentUser: StateFlow<UserSession?> = _currentUser.asStateFlow()

    // Registered users local cache
    private val _registeredUsers = MutableStateFlow<List<RegisteredUser>>(
        listOf(
            RegisteredUser("timbeck", "timbeck@abcchurch.org", "password7777", UserRole.PASTOR_BECK),
            RegisteredUser("jasonh", "jasonh@abcchurch.org", "password1234", UserRole.PASTOR_HOWARD),
            RegisteredUser("margier", "margier@abcchurch.org", "password8888", UserRole.BUSINESS_MANAGER),
            RegisteredUser("mabelm", "mabelm@abcchurch.org", "password2222", UserRole.ASSISTANT_CLERK),
            RegisteredUser("leroys", "leroys@abcchurch.org", "password3333", UserRole.MAINTENANCE),
            RegisteredUser("jackf", "jackf@abcchurch.org", "password5555", UserRole.CHOIR_DIRECTOR)
        )
    )
    val registeredUsers: StateFlow<List<RegisteredUser>> = _registeredUsers.asStateFlow()

    fun loginWithCredentials(username: String, password: String): Boolean {
        val user = _registeredUsers.value.find { 
            it.username.equals(username.trim(), ignoreCase = true) && it.password == password 
        }
        return if (user != null) {
            _currentUser.value = UserSession(user.role.displayName, user.role)
            true
        } else {
            false
        }
    }

    fun registerNewUser(username: String, email: String, password: String, role: UserRole): Boolean {
        val exists = _registeredUsers.value.any { 
            it.username.equals(username.trim(), ignoreCase = true) || it.email.equals(email.trim(), ignoreCase = true) 
        }
        if (exists) return false
        
        val newUser = RegisteredUser(
            username = username.trim(),
            email = email.trim(),
            password = password,
            role = role
        )
        _registeredUsers.value = _registeredUsers.value + newUser
        return true
    }

    fun findUserByEmail(email: String): RegisteredUser? {
        return _registeredUsers.value.find { it.email.equals(email.trim(), ignoreCase = true) }
    }

    fun login(role: UserRole, pin: String): Boolean {
        // Keep compatibility
        val validPin = when(role) {
            UserRole.PASTOR_BECK -> "7777"
            UserRole.PASTOR_HOWARD -> "1234"
            UserRole.BUSINESS_MANAGER -> "8888"
            UserRole.ASSISTANT_CLERK -> "2222"
            UserRole.MAINTENANCE -> "3333"
            UserRole.CHOIR_DIRECTOR -> "5555"
        }
        return if (pin == validPin) {
            _currentUser.value = UserSession(role.displayName, role)
            true
        } else {
            false
        }
    }

    // --- FIREBASE BACKUP & SYNC ---
    private val _firebaseSyncStatus = MutableStateFlow("IDLE") // "IDLE", "SYNCING", "RESTORING", "SUCCESS", "ERROR"
    val firebaseSyncStatus: StateFlow<String> = _firebaseSyncStatus.asStateFlow()

    fun isFirebaseConfigured(): Boolean = FirebaseSyncManager.isConfigured()
    fun isFirebaseActive(): Boolean = FirebaseSyncManager.isInitialized()

    fun syncDataToFirebase(onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _firebaseSyncStatus.value = "SYNCING"
            val successInit = FirebaseSyncManager.initialize(getApplication())
            if (!successInit) {
                _firebaseSyncStatus.value = "ERROR"
                onComplete(false, "Firebase could not be initialized. Please configure credentials in the secrets panel.")
                return@launch
            }

            FirebaseSyncManager.backupToFirestore(
                households = households.value,
                contributions = contributions.value,
                ceremonies = ceremonies.value,
                onSuccess = {
                    _firebaseSyncStatus.value = "SUCCESS"
                    onComplete(true, "Successfully pushed all standard church records to Cloud Firestore!")
                },
                onFailure = { err ->
                    _firebaseSyncStatus.value = "ERROR"
                    onComplete(false, "Cloud synchronization failed: ${err.localizedMessage}")
                }
            )
        }
    }

    fun restoreDataFromFirebase(onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _firebaseSyncStatus.value = "RESTORING"
            val successInit = FirebaseSyncManager.initialize(getApplication())
            if (!successInit) {
                _firebaseSyncStatus.value = "ERROR"
                onComplete(false, "Firebase could not be initialized. Please configure credentials in the secrets panel.")
                return@launch
            }

            FirebaseSyncManager.restoreFromFirestore(
                onSuccess = { rHouseholds, rContributions, rCeremonies ->
                    viewModelScope.launch {
                        try {
                            repository.replaceLocalDatabaseContent(rHouseholds, rContributions, rCeremonies)
                            _firebaseSyncStatus.value = "SUCCESS"
                            onComplete(true, "Local Room database fully restored from Firebase Firestore!")
                        } catch (e: Exception) {
                            _firebaseSyncStatus.value = "ERROR"
                            onComplete(false, "Restoration save failed: ${e.localizedMessage}")
                        }
                    }
                },
                onFailure = { err ->
                    _firebaseSyncStatus.value = "ERROR"
                    onComplete(false, "Firestore fetch failed: ${err.localizedMessage}")
                }
            )
        }
    }

    fun logout() {
        _currentUser.value = null
    }
}

enum class UserRole(val displayName: String, val roleTitle: String, val systemAccess: String) {
    PASTOR_BECK("Rev. Timothy Beck", "First Minister (Lead)", "Full Administrator Access"),
    PASTOR_HOWARD("Rev. Jason Howard", "Second Minister", "Ceremonies & Parish Dashboard"),
    BUSINESS_MANAGER("Margie Robbens", "Business Manager", "Finance & Tax Statements"),
    ASSISTANT_CLERK("Mabel McConahey", "Administrative Assistant", "Household Records & Mailings"),
    MAINTENANCE("Leroy Strickly", "Maintenance Person", "Property & Facilities"),
    CHOIR_DIRECTOR("Jack Fogerty", "Choir Director", "Music & Choir Attendance")
}

data class UserSession(
    val username: String,
    val role: UserRole
)

data class RegisteredUser(
    val username: String,
    val email: String,
    val password: String,
    val role: UserRole
)

