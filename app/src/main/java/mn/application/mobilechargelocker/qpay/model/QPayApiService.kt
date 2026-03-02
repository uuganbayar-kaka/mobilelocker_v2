package mn.application.mobilechargelocker.qpay.model

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface QPayApiService {

    @POST("v2/auth/token")
    suspend fun auth(
        @Header("Authorization") basicToken: String?
    ): Call<AuthResponse?>?
    @POST("v2/invoice")
    suspend fun createInvoice(
        @Header("Authorization") token: String,
        @Body body: InvoiceRequest
    ): Response<InvoiceResponse>

    @POST("v2/payment/{id}")
    suspend fun getPayment(
        @Header("Authorization") token: String,
        @Path("id") invoiceId: String
    ): Response<PaymentResponse>
}