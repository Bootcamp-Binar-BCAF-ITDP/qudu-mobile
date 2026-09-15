package com.example.test2.data.remote

import com.example.test2.data.dto.ApiEnvelope
import com.example.test2.data.dto.AuthResponseDto
import com.example.test2.data.dto.CustomerDocumentDto
import com.example.test2.data.dto.CustomerPlafondDto
import com.example.test2.data.dto.CustomerProfileDto
import com.example.test2.data.dto.DeviceTokenRequestDto
import com.example.test2.data.dto.ForgotPasswordRequestDto
import com.example.test2.data.dto.LoanApplicationCreateRequestDto
import com.example.test2.data.dto.LoanApplicationDto
import com.example.test2.data.dto.LoanDocumentDto
import com.example.test2.data.dto.LoginRequestDto
import com.example.test2.data.dto.NotificationDto
import com.example.test2.data.dto.PageEnvelope
import com.example.test2.data.dto.PlafondDto
import com.example.test2.data.dto.PlafondRequestDto
import com.example.test2.data.dto.PlafondUpgradeRequestDto
import com.example.test2.data.dto.ProfileUpdateRequestDto
import com.example.test2.data.dto.RefreshTokenRequestDto
import com.example.test2.data.dto.RegisterRequestDto
import com.example.test2.data.dto.RegistrationOtpRequestDto
import com.example.test2.data.dto.ResetPasswordRequestDto
import com.example.test2.data.dto.UnreadCountDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequestDto): Response<ApiEnvelope<Unit>>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequestDto): Response<AuthResponseDto>

    @POST("api/auth/logout")
    suspend fun logout(@Body body: RefreshTokenRequestDto): Response<ApiEnvelope<Unit>>

    @POST("api/auth/register/otp")
    suspend fun requestRegistrationOtp(
        @Body body: RegistrationOtpRequestDto,
    ): Response<ApiEnvelope<Unit>>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequestDto): Response<ApiEnvelope<Unit>>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequestDto): Response<ApiEnvelope<Unit>>

    @POST("api/customer")
    suspend fun createApplication(
        @Body body: LoanApplicationCreateRequestDto,
    ): Response<ApiEnvelope<LoanApplicationDto>>

    @GET("api/customer/customer/{customerId}")
    suspend fun listApplications(
        @Path("customerId") customerId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
    ): Response<ApiEnvelope<PageEnvelope<LoanApplicationDto>>>

    @Multipart
    @POST("api/customer/applications/{applicationId}/documents")
    suspend fun uploadDocument(
        @Path("applicationId") applicationId: String,
        @Query("documentType") documentType: String,
        @Part file: MultipartBody.Part,
    ): Response<ApiEnvelope<LoanDocumentDto>>

    @GET("api/customer/profile")
    suspend fun myProfile(): Response<ApiEnvelope<CustomerProfileDto>>

    @PUT("api/customer/profile")
    suspend fun updateProfile(
        @Body body: ProfileUpdateRequestDto,
    ): Response<ApiEnvelope<CustomerProfileDto>>

    @GET("api/customer/documents")
    suspend fun myProfileDocuments(): Response<ApiEnvelope<List<CustomerDocumentDto>>>

    @Multipart
    @POST("api/customer/documents")
    suspend fun uploadProfileDocument(
        @Query("documentType") documentType: String,
        @Part file: MultipartBody.Part,
    ): Response<ApiEnvelope<CustomerDocumentDto>>

    @GET("api/customer/notifications")
    suspend fun myNotifications(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
    ): Response<ApiEnvelope<PageEnvelope<NotificationDto>>>

    @GET("api/customer/notifications/unread-count")
    suspend fun unreadNotificationCount(): Response<ApiEnvelope<UnreadCountDto>>

    @PUT("api/customer/notifications/{notificationId}/read")
    suspend fun markNotificationRead(
        @Path("notificationId") notificationId: Long,
    ): Response<ApiEnvelope<NotificationDto>>

    @PUT("api/customer/notifications/read")
    suspend fun markAllNotificationsRead(): Response<ApiEnvelope<Map<String, Int>>>

    @GET("api/customer/plafond")
    suspend fun myPlafond(): Response<ApiEnvelope<CustomerPlafondDto>>

    @POST("api/customer/plafond")
    suspend fun requestPlafondUpgrade(
        @Body body: PlafondUpgradeRequestDto,
    ): Response<ApiEnvelope<PlafondRequestDto>>

    @GET("api/customer/plafond/requests")
    suspend fun myPlafondRequests(): Response<ApiEnvelope<List<PlafondRequestDto>>>

    @GET("api/plafonds/catalog")
    suspend fun plafondCatalog(): Response<ApiEnvelope<List<PlafondDto>>>

    @POST("api/customer/device-tokens")
    suspend fun registerDeviceToken(@Body body: DeviceTokenRequestDto): Response<ApiEnvelope<Unit>>

    @DELETE("api/customer/device-tokens")
    suspend fun removeDeviceToken(@Query("token") token: String): Response<ApiEnvelope<Unit>>
}
