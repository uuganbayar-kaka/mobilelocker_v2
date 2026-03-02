package mn.application.mobilechargelocker.qpay.model;
import com.google.gson.annotations.SerializedName;

public class InvoiceRequest {
    @SerializedName("invoice_code")
    private String invoiceCode;
    @SerializedName("sender_invoice_no")
    private String senderInvoiceNo;
    @SerializedName("invoice_receiver_code")
    private String invoiceReceiverCode;
    @SerializedName("invoice_description")
    private String invoiceDescription;
    @SerializedName("sender_branch_code")
    private String senderBranchCode;
    @SerializedName("amount")
    private double amount;
    @SerializedName("callback_url")
    private String callbackUrl;
    public void setInvoiceCode(String invoiceCode) {
        this.invoiceCode = invoiceCode;
    }
    public void setInvoiceNo(String senderInvoiceNo) {
        this.senderInvoiceNo = senderInvoiceNo;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public void setSenderBranchCode(String senderBranchCode) {
        this.senderBranchCode = senderBranchCode;
    }
}
