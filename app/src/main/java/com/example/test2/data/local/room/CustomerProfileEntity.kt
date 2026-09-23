package com.example.test2.data.local.room

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import com.example.test2.data.dto.CustomerDocumentDto
import com.example.test2.data.dto.CustomerProfileDto
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "customer_profile")
data class CustomerProfileEntity(

    @PrimaryKey
    val customerId: String,

    val customerName: String?,
    val email: String?,
    val phoneNumber: String?,
    val nik: String?,
    val address: String?,
    val sex: String?,
    val birthPlace: String?,
    val birthDate: String?,
    val occupation: String?,
    val citizenship: String?,

    val documents: List<CustomerDocumentDto>?,
    val profileComplete: Boolean,
    val missingDocuments: List<String>?,
    val missingForSubmission: List<String>?,
    val staleForSubmission: List<String>?,
    val submissionFreshnessDays: Int,
    val readyToSubmit: Boolean,

    val fetchedAt: Long,
)

fun CustomerProfileEntity.toDto(): CustomerProfileDto = CustomerProfileDto(
    customerId = customerId,
    customerName = customerName,
    email = email,
    phoneNumber = phoneNumber,
    nik = nik,
    address = address,
    sex = sex,
    birthPlace = birthPlace,
    birthDate = birthDate,
    occupation = occupation,
    citizenship = citizenship,
    approvedLimit = null,
    usedLimit = null,
    availableLimit = null,
    plafond = null,
    documents = documents.orEmpty(),
    profileComplete = profileComplete,
    missingDocuments = missingDocuments.orEmpty(),
    missingForSubmission = missingForSubmission.orEmpty(),
    staleForSubmission = staleForSubmission.orEmpty(),
    submissionFreshnessDays = submissionFreshnessDays,
    readyToSubmit = readyToSubmit,
)

fun CustomerProfileDto.toEntity(fetchedAt: Long): CustomerProfileEntity? {
    val id = customerId ?: return null

    return CustomerProfileEntity(
        customerId = id,
        customerName = customerName,
        email = email,
        phoneNumber = phoneNumber,
        nik = nik,
        address = address,
        sex = sex,
        birthPlace = birthPlace,
        birthDate = birthDate,
        occupation = occupation,
        citizenship = citizenship,
        documents = documents,
        profileComplete = profileComplete,
        missingDocuments = missingDocuments,
        missingForSubmission = missingForSubmission,
        staleForSubmission = staleForSubmission,
        submissionFreshnessDays = submissionFreshnessDays,
        readyToSubmit = readyToSubmit,
        fetchedAt = fetchedAt,
    )
}

@Dao
interface CustomerProfileDao {

    @Query("SELECT * FROM customer_profile LIMIT 1")
    fun observeProfile(): Flow<CustomerProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: CustomerProfileEntity)

    @Query("DELETE FROM customer_profile")
    suspend fun deleteAll()

    @Transaction
    suspend fun replace(profile: CustomerProfileEntity) {
        deleteAll()
        insert(profile)
    }
}
