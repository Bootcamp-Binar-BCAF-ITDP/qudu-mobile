package com.example.test2.data.dto

import java.math.BigDecimal
class ApiEnvelope<T>(
    val timestamp: String? = null,
    val status: Int? = null,
    val message: String? = null,
    val data: T? = null,
)

data class PageEnvelope<T>(
    val content: List<T> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val first: Boolean = true,
    val last: Boolean = true,
    val empty: Boolean = true,
)

data class RegisterRequestDto(
    val accountType: String = "CUSTOMER",
    val otp: String,
    val email: String,
    val password: String,
    val fullName: String,
    val phoneNumber: String,
    val nik: String,
    val address: String,
    val sex: String,
    val birthPlace: String,
    val birthDate: String,
    val occupation: String,
    val citizenship: String,
)

data class LoginRequestDto(
    val usernameOrEmail: String,
    val password: String,
    val accountType: String = "CUSTOMER",
)

data class AuthResponseDto(
    val token: String? = null,
    val refreshToken: String? = null,
    val expiresIn: Long? = null,
    val userId: String? = null,
    val username: String? = null,
    val role: String? = null,
    val email: String? = null,
    val fullName: String? = null,
)

data class RefreshTokenRequestDto(
    val refreshToken: String,
)

data class LoanApplicationCreateRequestDto(
    val customerId: String,
    val requestedAmount: BigDecimal,
    val tenor: Int,
    val purpose: String,
    val bank: String,
    val bankAccountNumber: String,
    val bankAccountName: String,
    val income: BigDecimal,
)

data class CustomerSummaryDto(
    val customerId: String? = null,
    val customerName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val nik: String? = null,
    val address: String? = null,
)

data class UserSummaryDto(
    val userId: String? = null,
    val fullName: String? = null,
    val username: String? = null,
)

data class LoanDocumentDto(
    val documentId: Int? = null,
    val applicationId: String? = null,
    val documentType: String? = null,
    val fileName: String? = null,
    val fileUrl: String? = null,
    val uploadedAt: String? = null,
)

data class LoanReviewDto(
    val reviewId: Int? = null,
    val applicationId: String? = null,
    val marketing: UserSummaryDto? = null,
    val recommendation: String? = null,
    val reviewNote: String? = null,
    val uploadedAt: String? = null,
)

data class LoanDecisionDto(
    val decisionId: Int? = null,
    val applicationId: String? = null,
    val branchManager: UserSummaryDto? = null,
    val decision: String? = null,
    val decisionNote: String? = null,
    val decidedAt: String? = null,
)

data class LoanVerificationDto(
    val verificationId: Int? = null,
    val applicationId: String? = null,
    val verifiedBy: UserSummaryDto? = null,
    val callStatus: String? = null,
    val verificationNote: String? = null,
    val verificationDate: String? = null,
)

data class LoanDisbursementDto(
    val disburseId: Int? = null,
    val applicationId: String? = null,
    val processedBy: UserSummaryDto? = null,
    val disbursedAmount: BigDecimal? = null,
    val bankName: String? = null,
    val accountNumber: String? = null,
    val disbursementDate: String? = null,
    val decision: String? = null,
    val decisionNote: String? = null,
)

data class LoanApplicationDto(
    val applicationId: String? = null,
    val customer: CustomerSummaryDto? = null,
    val requestedAmount: BigDecimal? = null,
    val tenor: Int? = null,
    val purpose: String? = null,
    val income: BigDecimal? = null,
    val status: String? = null,
    val submissionDate: String? = null,
    val bank: String? = null,
    val bankAccountNumber: String? = null,
    val bankAccountName: String? = null,
    val documents: List<LoanDocumentDto>? = null,
    val review: LoanReviewDto? = null,
    val bmdecision: LoanDecisionDto? = null,
    val verifications: List<LoanVerificationDto>? = null,
    val disbursement: LoanDisbursementDto? = null,
)

data class PlafondDto(
    val plafondId: Int? = null,
    val level: Int? = null,
    val description: String? = null,
    val minimumAmount: BigDecimal? = null,
    val maxAmount: BigDecimal? = null,
    val minTenor: Int? = null,
    val maxTenor: Int? = null,
    val interestRate: BigDecimal? = null,
    val adminFee: BigDecimal? = null,
)

data class CustomerPlafondDto(
    val customerId: String? = null,
    val customerName: String? = null,
    val approvedLimit: BigDecimal? = null,
    val usedLimit: BigDecimal? = null,
    val availableLimit: BigDecimal? = null,
    val plafond: PlafondDto? = null,
)

data class PlafondUpgradeRequestDto(
    val requestedAmount: BigDecimal,
)

data class PlafondRequestDto(
    val requestId: String? = null,
    val customerId: String? = null,
    val customerName: String? = null,
    val previousLevel: Int? = null,
    val requestedLevel: Int? = null,
    val requestedAmount: BigDecimal? = null,
    val approvedAmount: BigDecimal? = null,
    val status: String? = null,
    val requestDate: String? = null,
    val decisionDate: String? = null,
    val reviewedBy: String? = null,
    val notes: String? = null,
    val requestedPlafond: PlafondDto? = null,
)

data class DeviceTokenRequestDto(
    val token: String,
    val platform: String = "android",
)

data class CustomerDocumentDto(
    val documentId: Int? = null,
    val documentType: String? = null,
    val label: String? = null,
    val fileName: String? = null,
    val fileUrl: String? = null,
    val uploadedAt: String? = null,
)

data class CustomerProfileDto(
    val customerId: String? = null,
    val customerName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val nik: String? = null,
    val address: String? = null,
    val sex: String? = null,
    val birthPlace: String? = null,
    val birthDate: String? = null,
    val occupation: String? = null,
    val citizenship: String? = null,
    val approvedLimit: BigDecimal? = null,
    val usedLimit: BigDecimal? = null,
    val availableLimit: BigDecimal? = null,
    val plafond: PlafondDto? = null,
    val documents: List<CustomerDocumentDto> = emptyList(),
    val profileComplete: Boolean = false,
    val missingDocuments: List<String> = emptyList(),
    val missingForSubmission: List<String> = emptyList(),
    val staleForSubmission: List<String> = emptyList(),
    val submissionFreshnessDays: Int = 30,
    val readyToSubmit: Boolean = false,
) {
    fun documentOf(type: String): CustomerDocumentDto? =
        documents.firstOrNull { it.documentType == type }

    fun isStale(type: String): Boolean = staleForSubmission.contains(type)

    fun needsUpload(type: String): Boolean =
        missingForSubmission.contains(type) || isStale(type)
}

data class ProfileUpdateRequestDto(
    val phoneNumber: String,
    val address: String,
    val occupation: String,
)

data class NotificationDto(
    val notificationId: Long? = null,
    val type: String? = null,
    val title: String? = null,
    val body: String? = null,
    val referenceId: String? = null,
    val read: Boolean = false,
    val createdAt: String? = null,
)

data class UnreadCountDto(
    val unread: Long = 0,
)


data class ForgotPasswordRequestDto(
    val email: String,
    val accountType: String = "CUSTOMER",
)

data class ResetPasswordRequestDto(
    val email: String,
    val token: String,
    val newPassword: String,
    val confirmPassword: String,
)

data class RegistrationOtpRequestDto(
    val email: String,
)
