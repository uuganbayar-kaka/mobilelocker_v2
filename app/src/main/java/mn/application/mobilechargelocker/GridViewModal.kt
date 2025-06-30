package mn.application.mobilechargelocker

data class GridViewModal(
    // we are creating a modals class with 2 member
    // one is course name as string and
    // other course img as int.
    val lockerId: Int,
    val command: String,
    var isUsed: Boolean
)