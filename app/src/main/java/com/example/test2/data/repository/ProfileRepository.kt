package com.example.test2.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.test2.core.Outcome
import com.example.test2.data.remote.ApiService
import com.example.test2.data.dto.CustomerDocumentDto
import com.example.test2.data.dto.CustomerProfileDto
import com.example.test2.data.dto.ProfileUpdateRequestDto

class ProfileRepository(
    private val api: ApiService,
    private val contentResolver: ContentResolver,
) {

    suspend fun myProfile(): Outcome<CustomerProfileDto> =
        apiCall { api.myProfile() }.unwrapEnvelope()

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

    /** Upload or replace one identity paper (KTP / KK / SELFIE). */
    suspend fun uploadDocument(documentType: String, uri: Uri): Outcome<CustomerDocumentDto> {

        val part = when (val prepared = filePartFrom(contentResolver, uri, documentType)) {
            is Outcome.Failure -> return prepared
            is Outcome.Success -> prepared.value
        }

        return apiCall { api.uploadProfileDocument(documentType, part) }.unwrapEnvelope()
    }
}
