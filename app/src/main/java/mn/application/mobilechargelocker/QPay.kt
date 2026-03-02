package mn.application.mobilechargelocker

import kotlinx.coroutines.*
import mn.application.mobilechargelocker.qpay.model.InvoiceRequest
import mn.application.mobilechargelocker.qpay.model.InvoiceResponse
import mn.application.mobilechargelocker.qpay.model.QPayApiService
import retrofit2.Response

class InvoiceManager(private val service: QPayApiService) {

    suspend fun processPayment(token: String, request: InvoiceRequest) {
        val createRes = service.createInvoice("Bearer $token", request)
        if (createRes.isSuccessful) {
            val invoiceId = createRes.body()?.invoiceId ?: return
            println("Invoice амжилттай үүслээ: $invoiceId")
            pollPaymentStatus(
                token = token,
                invoiceId = invoiceId,
                totalDurationMs = 30_000,
                delayMs = 5_000
            )
        } else {
            println("Invoice үүсгэхэд алдаа гарлаа: ${createRes.errorBody()?.string()}")
        }
    }

    private suspend fun pollPaymentStatus(token: String, id: String, totalDurationMs: Long, delayMs: Long) {
        val startTime = System.currentTimeMillis()

        while (System.currentTimeMillis() - startTime < totalDurationMs) {
            val response = service.getPayment("Bearer $token", id)

            if (response.isSuccessful) {
                val status = response.body()?.status
                println("Төлбөрийн төлөв: $status")

                if (status == "PAID") {
                    println("Төлбөр амжилттай төлөгдлөө!")
                    return
                }
            }
            println("Дахин шалгаж байна...")
            delay(delayMs)
        }
        println("Хүлээх хугацаа дууслаа (Timeout).")
    }
}