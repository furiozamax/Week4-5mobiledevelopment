package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HouseholdDao {
    @Query("SELECT * FROM households ORDER BY familyName ASC")
    fun getAllHouseholds(): Flow<List<Household>>

    @Query("SELECT * FROM households WHERE envelopeNumber = :envelopeNumber LIMIT 1")
    suspend fun getHouseholdByEnvelope(envelopeNumber: Int): Household?

    @Query("SELECT * FROM households WHERE id = :id LIMIT 1")
    suspend fun getHouseholdById(id: Int): Household?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHousehold(household: Household): Long

    @Query("DELETE FROM households")
    suspend fun deleteAllHouseholds()

    @Update
    suspend fun updateHousehold(household: Household)

    @Delete
    suspend fun deleteHousehold(household: Household)
}

@Dao
interface ContributionDao {
    @Query("SELECT * FROM contributions ORDER BY date DESC")
    fun getAllContributions(): Flow<List<Contribution>>

    @Query("SELECT * FROM contributions WHERE householdId = :householdId ORDER BY date DESC")
    fun getContributionsByHousehold(householdId: Int): Flow<List<Contribution>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContribution(contribution: Contribution): Long

    @Query("DELETE FROM contributions")
    suspend fun deleteAllContributions()

    @Delete
    suspend fun deleteContribution(contribution: Contribution)
}

@Dao
interface CeremonyDao {
    @Query("SELECT * FROM ceremonies ORDER BY date DESC")
    fun getAllCeremonies(): Flow<List<Ceremony>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCeremony(ceremony: Ceremony): Long

    @Query("DELETE FROM ceremonies")
    suspend fun deleteAllCeremonies()

    @Update
    suspend fun updateCeremony(ceremony: Ceremony)

    @Delete
    suspend fun deleteCeremony(ceremony: Ceremony)
}
