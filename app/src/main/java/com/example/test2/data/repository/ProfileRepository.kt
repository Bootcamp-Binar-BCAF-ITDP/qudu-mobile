package com.example.test2.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.test2.core.Outcome
import com.example.test2.data.local.CachedList
import com.example.test2.data.local.ProfileCache
import com.example.test2.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import com.example.test2.data.dto.CustomerDocumentDto
import com.example.test2.data.dto.CustomerProfileDto
import com.example.test2.data.dto.ProfileUpdateRequestDto

class ProfileRepository(
    private val api: ApiService,
    private val contentResolver: ContentResolver,
    private val cache: ProfileCache,
) {

    val cachedProfile: Flow<CachedList<CustomerProfileDto>> = cache.profile

    suspend fun myProfile(): Outcome<CustomerProfileDto> {
        val result = apiCall { api.myProfile() }.unwrapEnvelope()
        if (result is Outcome.Success) cache.replace(result.value)
        return result
    }

    suspend fun updateProfile(
        phoneNumber: String,
        address: String,
        occupation: String,
    ): Outcome<CustomerProfileDto> =
        apiCall {
            api.updateProfile(ProfileUpdateRequestDto(phoneNumber, address, occupation))
        }.unwrapEnvelope()

    suspend fun myDocuments(): Outcome<List<CustomerDocumentDto>> =
        when (val result = apiCall { api.myProfileDocuments() }) {
            is Outcome.Failure -> result
            is Outcome.Success -> Outcome.Success(result.value.data.orEmpty())
        }

    suspend fun uploadDocument(documentType: String, uri: Uri): Outcome<CustomerDocumentDto> {

        val part = when (val prepared = filePartFrom(contentResolver, uri, documentType)) {
            is Outcome.Failure -> return prepared
            is Outcome.Success -> prepared.value
        }

        return apiCall { api.uploadProfileDocument(documentType, part) }.unwrapEnvelope()
    }
}
