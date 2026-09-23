package com.example.test2.data.local.room

import androidx.room.TypeConverter
import com.example.test2.data.dto.CustomerDocumentDto
import com.example.test2.data.dto.CustomerSummaryDto
import com.example.test2.data.dto.LoanDecisionDto
import com.example.test2.data.dto.LoanDisbursementDto
import com.example.test2.data.dto.LoanDocumentDto
import com.example.test2.data.dto.LoanReviewDto
import com.example.test2.data.dto.LoanVerificationDto
import com.example.test2.data.dto.PlafondDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.math.BigDecimal

class RoomConverters {

    private val gson = Gson()

    @TypeConverter
    fun bigDecimalToString(value: BigDecimal?): String? = value?.toPlainString()

    @TypeConverter
    fun stringToBigDecimal(value: String?): BigDecimal? =
        value?.let { runCatching { BigDecimal(it) }.getOrNull() }

    @TypeConverter
    fun customerToJson(value: CustomerSummaryDto?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun jsonToCustomer(json: String?): CustomerSummaryDto? = decode(json)

    @TypeConverter
    fun documentsToJson(value: List<LoanDocumentDto>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun jsonToDocuments(json: String?): List<LoanDocumentDto>? =
        decodeList(json, object : TypeToken<List<LoanDocumentDto>>() {})

    @TypeConverter
    fun reviewToJson(value: LoanReviewDto?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun jsonToReview(json: String?): LoanReviewDto? = decode(json)

    @TypeConverter
    fun decisionToJson(value: LoanDecisionDto?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun jsonToDecision(json: String?): LoanDecisionDto? = decode(json)

    @TypeConverter
    fun verificationsToJson(value: List<LoanVerificationDto>?): String? =
        value?.let { gson.toJson(it) }

    @TypeConverter
    fun jsonToVerifications(json: String?): List<LoanVerificationDto>? =
        decodeList(json, object : TypeToken<List<LoanVerificationDto>>() {})

    @TypeConverter
    fun disbursementToJson(value: LoanDisbursementDto?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun jsonToDisbursement(json: String?): LoanDisbursementDto? = decode(json)

    @TypeConverter
    fun plafondToJson(value: PlafondDto?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun jsonToPlafond(json: String?): PlafondDto? = decode(json)

    @TypeConverter
    fun customerDocumentsToJson(value: List<CustomerDocumentDto>?): String? =
        value?.let { gson.toJson(it) }

    @TypeConverter
    fun jsonToCustomerDocuments(json: String?): List<CustomerDocumentDto>? =
        decodeList(json, object : TypeToken<List<CustomerDocumentDto>>() {})

    @TypeConverter
    fun stringsToJson(value: List<String>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun jsonToStrings(json: String?): List<String>? =
        decodeList(json, object : TypeToken<List<String>>() {})

    private inline fun <reified T> decode(json: String?): T? {
        if (json.isNullOrBlank()) return null
        return runCatching { gson.fromJson(json, T::class.java) }.getOrNull()
    }

    private fun <T> decodeList(json: String?, token: TypeToken<List<T>>): List<T>? {
        if (json.isNullOrBlank()) return null
        return runCatching { gson.fromJson<List<T>>(json, token.type) }.getOrNull()
    }
}
