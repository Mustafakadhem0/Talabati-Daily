package com.mustafa.talabatidaily

data class OrderItem(
    var medicineId: Long = 0L,
    var medicineName: String = "",
    var quantity: Int = 0,
    var supplier: String = "",
    var notes: String = ""
)