package mn.application.mobilechargelocker.qpay.model
import com.google.gson.annotations.SerializedName

data class InvoiceResponse(
    @SerializedName("invoice_id")
    val invoiceId: String,

    @SerializedName("qPay_shortUrl")
    val qPayShortUrl: String,

    @SerializedName("qr_text")
    val qrText: String,

    @SerializedName("qr_image")
    val qrImage: String
)