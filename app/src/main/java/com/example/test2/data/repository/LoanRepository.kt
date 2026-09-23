package com.example.test2.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.test2.core.Outcome
import com.example.test2.data.dto.CustomerPlafondDto
import com.example.test2.data.dto.LoanApplicationCreateRequestDto
import com.example.test2.data.dto.LoanApplicationDto
import com.example.test2.data.dto.LoanDocumentDto
import com.example.test2.data.dto.PlafondRequestDto
import com.example.test2.data.dto.PlafondUpgradeRequestDto
import com.example.test2.data.local.CachedList
import com.example.test2.data.local.room.LoanApplicationDao
import com.example.test2.data.local.room.PlafondRequestDao
import com.example.test2.data.local.room.toDto
import com.example.test2.data.local.room.toEntity
import com.example.test2.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal

class LoanRepository(
    private val api: ApiService,
    private val contentResolver: ContentResolver,
    private val applicationDao: LoanApplicationDao,
    private val plafondRequestDao: PlafondRequestDao,
) {

    val cachedApplications: Flow<CachedList<LoanApplicationDto>> =
        applicationDao.observeAll().map { rows ->
            CachedList(
                items = rows.map { it.toDto() },
                fetchedAt = rows.minOfOrNull { it.fetchedAt },
            )
        }

    val cachedUpgradeRequests: Flow<CachedList<PlafondRequestDto>> =
        plafondRequestDao.observeAll().map { rows ->
            CachedList(
                items = rows.map { it.toDto() },
                fetchedAt = rows.minOfOrNull { it.fetchedAt },
            )
        }

    suspend fun createApplication(
        body: LoanApplicationCreateRequestDto,
    ): Outcome<LoanApplicationDto> = apiCall { api.createApplication(body) }.unwrapEnvelope()

    suspend fun myApplications(customerId: String): Outcome<List<LoanApplicationDto>> =
        when (val result = apiCall { api.listApplications(customerId) }) {
            is Outcome.Failure -> result
            is Outcome.Success -> {
                val items = result.value.data?.content.orEmpty()
                val now = System.currentTimeMillis()
                applicationDao.replaceAll(items.mapNotNull { it.toEntity(now) })
                Outcome.Success(items)
            }
        }

    suspend fun uploadDocument(
        applicationId: String,
        documentType: String,
        uri: Uri,
    ): Outcome<LoanDocumentDto> {

        val part = when (val prepared = filePartFrom(contentResolver, uri, documentType)) {
            is Outcome.Failure -> return prepared
            is Outcome.Success -> prepared.value
        }

        return apiCall { api.uploadDocument(applicationId, documentType, part) }.unwrapEnvelope()
    }

    suspend fun myPlafond(): Outcome<CustomerPlafondDto> =
        apiCall { api.myPlafond() }.unwrapEnvelope()

    suspend fun requestUpgrade(amount: BigDecimal): Outcome<PlafondRequestDto> =
        apiCall { api.requestPlafondUpgrade(PlafondUpgradeRequestDto(amount)) }.unwrapEnvelope()

    suspend fun myUpgradeRequests(): Outcome<List<PlafondRequestDto>> =
        when (val result = apiCall { api.myPlafondRequests() }) {
            is Outcome.Failure -> result
            is Outcome.Success -> {
                val items = result.value.data.orEmpty()
                val now = System.currentTimeMillis()
                plafondRequestDao.replaceAll(items.mapNotNull { it.toEntity(now) })
                Outcome.Success(items)
            }
        }
}
