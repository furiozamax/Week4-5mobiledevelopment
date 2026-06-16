package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class ChurchRepository(private val db: AppDatabase) {
    val households: Flow<List<Household>> = db.householdDao.getAllHouseholds()
    val contributions: Flow<List<Contribution>> = db.contributionDao.getAllContributions()
    val ceremonies: Flow<List<Ceremony>> = db.ceremonyDao.getAllCeremonies()

    suspend fun getHouseholdById(id: Int): Household? = db.householdDao.getHouseholdById(id)
    suspend fun getHouseholdByEnvelope(envelope: Int): Household? = db.householdDao.getHouseholdByEnvelope(envelope)

    suspend fun insertHousehold(household: Household) = db.householdDao.insertHousehold(household)
    suspend fun updateHousehold(household: Household) = db.householdDao.updateHousehold(household)
    suspend fun deleteHousehold(household: Household) = db.householdDao.deleteHousehold(household)

    // Inserts a contribution, and if it is for Capital Improvement, decreases the pledge remaining balance
    suspend fun addContribution(contribution: Contribution) {
        db.contributionDao.insertContribution(contribution)
        if (contribution.type == "Capital Improvement") {
            val hh = when {
                contribution.householdId != null -> db.householdDao.getHouseholdById(contribution.householdId)
                contribution.envelopeNumber != null -> db.householdDao.getHouseholdByEnvelope(contribution.envelopeNumber)
                else -> null
            }
            if (hh != null) {
                val newRemaining = (hh.pledgeRemaining - contribution.amount).coerceAtLeast(0.0)
                db.householdDao.updateHousehold(hh.copy(pledgeRemaining = newRemaining))
            }
        }
    }

    suspend fun deleteContribution(contribution: Contribution) {
        db.contributionDao.deleteContribution(contribution)
        // If deleting a Capital Improvement contribution, restore the pledge balance
        if (contribution.type == "Capital Improvement") {
            val hh = when {
                contribution.householdId != null -> db.householdDao.getHouseholdById(contribution.householdId)
                contribution.envelopeNumber != null -> db.householdDao.getHouseholdByEnvelope(contribution.envelopeNumber)
                else -> null
            }
            if (hh != null) {
                val newRemaining = (hh.pledgeRemaining + contribution.amount).coerceAtRootLimit(hh.pledgeAmount)
                db.householdDao.updateHousehold(hh.copy(pledgeRemaining = newRemaining))
            }
        }
    }

    private fun Double.coerceAtRootLimit(limit: Double): Double {
        return if (this > limit) limit else this
    }

    suspend fun insertCeremony(ceremony: Ceremony) = db.ceremonyDao.insertCeremony(ceremony)
    suspend fun updateCeremony(ceremony: Ceremony) = db.ceremonyDao.updateCeremony(ceremony)
    suspend fun deleteCeremony(ceremony: Ceremony) = db.ceremonyDao.deleteCeremony(ceremony)

    suspend fun replaceLocalDatabaseContent(
        householdsList: List<Household>,
        contributionsList: List<Contribution>,
        ceremoniesList: List<Ceremony>
    ) {
        db.householdDao.deleteAllHouseholds()
        db.contributionDao.deleteAllContributions()
        db.ceremonyDao.deleteAllCeremonies()

        for (hh in householdsList) {
            db.householdDao.insertHousehold(hh)
        }
        for (c in contributionsList) {
            db.contributionDao.insertContribution(c)
        }
        for (cr in ceremoniesList) {
            db.ceremonyDao.insertCeremony(cr)
        }
    }

    // Prepopulate some starting church information if database is empty
    suspend fun prepopulateIfEmpty() {
        val count = db.householdDao.getAllHouseholds().first().size
        if (count > 0) return

        // 1. Households
        val list = listOf(
            Household(
                familyName = "Vanderbilt",
                headName = "Hendrik Vanderbilt",
                phone = "(616) 555-0143",
                email = "hendrik.v@grnet.org",
                envelopeNumber = 101,
                memberNames = "Hendrik Vanderbilt, Gertrude Vanderbilt, Jan Vanderbilt, Wilhelmina Vanderbilt",
                pledgeAmount = 5000.0,
                pledgeRemaining = 3500.0,
                weeklyEnvelopesSent = true,
                capitalEnvelopesSent = true
            ),
            Household(
                familyName = "De Jong",
                headName = "Cornelius De Jong",
                phone = "(616) 555-0294",
                email = "cornelius.dej@comcast.net",
                envelopeNumber = 102,
                memberNames = "Cornelius De Jong, Maria De Jong, Pieter De Jong",
                pledgeAmount = 3000.0,
                pledgeRemaining = 2400.0,
                weeklyEnvelopesSent = true,
                capitalEnvelopesSent = true
            ),
            Household(
                familyName = "Smith",
                headName = "Randall Smith",
                phone = "(616) 555-0311",
                email = "rsmith@gmail.com",
                envelopeNumber = 103,
                memberNames = "Randall Smith, Sharon Smith, Tommy Smith, Clara Smith",
                pledgeAmount = 1500.0,
                pledgeRemaining = 1100.0,
                weeklyEnvelopesSent = true,
                capitalEnvelopesSent = true
            ),
            Household(
                familyName = "McConahey",
                headName = "Mabel McConahey",
                phone = "(616) 555-0199",
                email = "mabel.m@abcchurch.org",
                envelopeNumber = 104,
                memberNames = "Mabel McConahey",
                pledgeAmount = 1000.0,
                pledgeRemaining = 1000.0,
                weeklyEnvelopesSent = true,
                capitalEnvelopesSent = false
            ),
            Household(
                familyName = "Robbens",
                headName = "Margie Robbens",
                phone = "(616) 555-1212",
                email = "margie.robbens@abcchurch.org",
                envelopeNumber = 105,
                memberNames = "Margie Robbens, David Robbens",
                pledgeAmount = 2500.0,
                pledgeRemaining = 1500.0,
                weeklyEnvelopesSent = true,
                capitalEnvelopesSent = true
            ),
            Household(
                familyName = "Strickly",
                headName = "Leroy Strickly",
                phone = "(616) 555-7382",
                email = "leroy.maintenance@gmail.com",
                envelopeNumber = 106,
                memberNames = "Leroy Strickly, Clara Strickly",
                pledgeAmount = 500.0,
                pledgeRemaining = 500.0,
                weeklyEnvelopesSent = true,
                capitalEnvelopesSent = false
            ),
            Household(
                familyName = "Howard",
                headName = "Rev. Jason Howard",
                phone = "(616) 555-4921",
                email = "jason.howard@abcchurch.org",
                envelopeNumber = 107,
                memberNames = "Jason Howard, Sarah Howard, Elizabeth Howard",
                pledgeAmount = 2000.0,
                pledgeRemaining = 1800.0,
                weeklyEnvelopesSent = true,
                capitalEnvelopesSent = true
            )
        )

        for (hh in list) {
            db.householdDao.insertHousehold(hh)
        }

        // 2. Contributions
        // Let's create contributions for April, May, and June of 2026.
        val cal = Calendar.getInstance()
        
        // Let's add multiple offerings
        val contributionsList = listOf(
            // Weekly Sunday offerings
            Contribution(householdId = 1, envelopeNumber = 101, amount = 150.0, type = "Regular Sunday Offering", paymentMethod = "Check", date = getPastDate(cal, 1, 10)),
            Contribution(householdId = 2, envelopeNumber = 102, amount = 100.0, type = "Regular Sunday Offering", paymentMethod = "Check", date = getPastDate(cal, 1, 10)),
            Contribution(householdId = 3, envelopeNumber = 103, amount = 50.0, type = "Regular Sunday Offering", paymentMethod = "Cash", date = getPastDate(cal, 1, 10)),
            // Loose offerings
            Contribution(householdId = null, envelopeNumber = null, amount = 220.0, type = "Loose Offering", paymentMethod = "Cash", date = getPastDate(cal, 1, 10)),
            
            // Second week of month - Capital Improvement collections
            Contribution(householdId = 1, envelopeNumber = 101, amount = 500.0, type = "Capital Improvement", paymentMethod = "Check", date = getPastDate(cal, 1, 14)),
            Contribution(householdId = 2, envelopeNumber = 102, amount = 300.0, type = "Capital Improvement", paymentMethod = "Check", date = getPastDate(cal, 1, 14)),
            Contribution(householdId = 5, envelopeNumber = 105, amount = 500.0, type = "Capital Improvement", paymentMethod = "Check", date = getPastDate(cal, 1, 14)),
            Contribution(householdId = null, envelopeNumber = null, amount = 120.0, type = "Loose Offering", paymentMethod = "Cash", date = getPastDate(cal, 1, 14)),

            // Another Sunday collection
            Contribution(householdId = 1, envelopeNumber = 101, amount = 150.0, type = "Regular Sunday Offering", paymentMethod = "Check", date = getPastDate(cal, 1, 17)),
            Contribution(householdId = 3, envelopeNumber = 103, amount = 75.0, type = "Regular Sunday Offering", paymentMethod = "Check", date = getPastDate(cal, 1, 17)),
            Contribution(householdId = 5, envelopeNumber = 105, amount = 100.0, type = "Regular Sunday Offering", paymentMethod = "Check", date = getPastDate(cal, 1, 17)),
            Contribution(householdId = null, envelopeNumber = null, amount = 185.0, type = "Loose Offering", paymentMethod = "Cash", date = getPastDate(cal, 1, 17)),

            // Another month's Capital Campaign collections (e.g. May)
            Contribution(householdId = 1, envelopeNumber = 101, amount = 1000.0, type = "Capital Improvement", paymentMethod = "Check", date = getPastDate(cal, 0, 11)),
            Contribution(householdId = 2, envelopeNumber = 102, amount = 300.0, type = "Capital Improvement", paymentMethod = "Check", date = getPastDate(cal, 0, 11)),
            Contribution(householdId = 3, envelopeNumber = 103, amount = 400.0, type = "Capital Improvement", paymentMethod = "Check", date = getPastDate(cal, 0, 11)),
            Contribution(householdId = 5, envelopeNumber = 105, amount = 500.0, type = "Capital Improvement", paymentMethod = "Check", date = getPastDate(cal, 0, 11)),
            Contribution(householdId = 7, envelopeNumber = 107, amount = 200.0, type = "Capital Improvement", paymentMethod = "Check", date = getPastDate(cal, 0, 11))
        )

        for (ctb in contributionsList) {
            db.contributionDao.insertContribution(ctb)
        }

        // 3. Ceremonies
        val ceremoniesList = listOf(
            Ceremony(
                type = "Baptism",
                date = getPastDate(cal, 1, 5),
                primaryPerson = "Alexander De Jong",
                additionalPerson = "Cornelius & Maria De Jong (Parents)",
                sponsorGodmother = "Wilhelmina Vanderbilt",
                sponsorGodfather = "Hendrik Vanderbilt",
                officiant = "Reverend Timothy Beck",
                infantOrAdult = "Infant",
                classesCompleted = true,
                weeksCompleted = 12,
                notes = "Beautiful morning family service. Sponsors was present."
            ),
            Ceremony(
                type = "Wedding",
                date = getPastDate(cal, 0, 18),
                primaryPerson = "David Robbens",
                additionalPerson = "Clara Smith",
                officiant = "Reverend Jason Howard",
                classesCompleted = true,
                weeksCompleted = 8,
                notes = "David is Margie Robbens' son, Clara is Randall Smith's daughter. Wonderful celebration of two active church households!"
            ),
            Ceremony(
                type = "Funeral",
                date = getPastDate(cal, 0, 2),
                primaryPerson = "Arthur Strickly",
                additionalPerson = "Brother of Leroy Strickly",
                officiant = "Reverend Timothy Beck",
                classesCompleted = false,
                notes = "Private graveside memorial followed by modest reception in church basement."
            ),
            Ceremony(
                type = "Baptism",
                date = System.currentTimeMillis() + 86400000 * 3, // Upcoming
                primaryPerson = "Theresa Vanderhoof",
                additionalPerson = "Adult convert",
                sponsorGodmother = "Gertrude Vanderbilt",
                sponsorGodfather = "Randall Smith",
                officiant = "Reverend Timothy Beck",
                infantOrAdult = "Adult",
                classesCompleted = false,
                weeksCompleted = 10, // still finishing classes!
                notes = "Adult baptism. Currently on week 10 of her 12-week (3 months) introductory prep course."
            )
        )

        for (crm in ceremoniesList) {
            db.ceremonyDao.insertCeremony(crm)
        }
    }

    private fun getPastDate(cal: Calendar, monthsAgo: Int, dayOfMonth: Int): Long {
        val clone = cal.clone() as Calendar
        clone.add(Calendar.MONTH, -monthsAgo)
        clone.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        return clone.timeInMillis
    }
}
