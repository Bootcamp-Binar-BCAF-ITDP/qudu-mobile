package com.example.test2.data.local.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.test2.data.dto.CustomerSummaryDto
import com.example.test2.data.dto.LoanApplicationDto
import com.example.test2.data.dto.LoanDecisionDto
import com.example.test2.data.dto.LoanDisbursementDto
import com.example.test2.data.dto.LoanDocumentDto
import com.example.test2.data.dto.LoanReviewDto
import com.example.test2.data.dto.LoanVerificationDto
import java.math.BigDecimal

@Entity(tableName = "loan_applications")
data class LoanApplicationEntity(

    @PrimaryKey
    val applicationId: String,

    @ColumnInfo(index = true)
    val customerId: String?,

    val requestedAmount: BigDecimal?,
    val tenor: Int?,
    val purpose: String?,
    val income: BigDecimal?,
    val status: String?,
    val submissionDate: String?,
    val bank: String?,
    val bankAccountNumber: String?,
    val bankAccountName: String?,

    val customer: CustomerSummaryDto?,
    val documents: List<LoanDocumentDto>?,
    val review: LoanReviewDto?,
    val bmdecision: LoanDecisionDto?,
    val verifications: List<LoanVerificationDto>?,
    val disbursement: LoanDisbursementDto?,

    val fetchedAt: Long,
)

fun LoanApplicationEntity.toDto(): LoanApplicationDto = LoanApplicationDto(
    applicationId = applicationId,
    customer = customer,
    requestedAmount = requestedAmount,
    tenor = tenor,
    purpose = purpose,
    income = income,
    status = status,
    submissionDate = submissionDate,
    bank = bank,
    bankAccountNumber = bankAccountNumber,
    bankAccountName = bankAccountName,
    documents = documents,
    review = review,
    bmdecision = bmdecision,
    verifications = verifications,
    disbursement = disbursement,
)

fun LoanApplicationDto.toEntity(fetchedAt: Long): LoanApplicationEntity? {
    val id = applicationId ?: return null

    return LoanApplicationEntity(
        applicationId = id,
        customerId = customer?.customerId,
        requestedAmount = requestedAmount,
        tenor = tenor,
        purpose = purpose,
        income = income,
        status = status,
        submissionDate = submissionDate,
        bank = bank,
        bankAccountNumber = bankAccountNumber,
        bankAccountName = bankAccountName,
        customer = customer,
        documents = documents,
        review = review,
        bmdecision = bmdecision,
        verifications = verifications,
        disbursement = disbursement,
        fetchedAt = fetchedAt,
    )
}
