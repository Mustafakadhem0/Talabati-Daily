package com.mustafa.talabatidaily

data class Medicine(
    var id: Long = 0L,
    var name: String = "",
    var defaultSupplier: String = "",
    var defaultQuantity: Int = 0,
    var notes: String = ""
)