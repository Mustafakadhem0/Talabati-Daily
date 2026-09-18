package com.mustafa.talabatidaily

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class OrderStorage(context: Context) {

    private val preferences = context.getSharedPreferences(
        "talabati_daily_order",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_ORDER_ITEMS = "order_items"
        private const val KEY_CURRENT_INDEX = "current_index"
    }

    // جلب الطلبية الحالية
    fun getOrderItems(): MutableList<OrderItem> {

        val json = preferences.getString(
            KEY_ORDER_ITEMS,
            null
        ) ?: return mutableListOf()

        return try {

            val array = JSONArray(json)
            val items = mutableListOf<OrderItem>()

            for (i in 0 until array.length()) {

                val obj = array.getJSONObject(i)

                items.add(
                    OrderItem(
                        medicineId = obj.optLong("medicineId"),
                        medicineName = obj.optString("medicineName"),
                        quantity = obj.optInt("quantity"),
                        supplier = obj.optString("supplier"),
                        notes = obj.optString("notes")
                    )
                )
            }

            items

        } catch (e: Exception) {
            mutableListOf()
        }
    }

    // إضافة أو تحديث علاج داخل الطلبية
    fun saveOrderItem(item: OrderItem) {

        val items = getOrderItems()

        val index = items.indexOfFirst {
            it.medicineId == item.medicineId
        }

        if (index >= 0) {

            if (item.quantity <= 0) {
                items.removeAt(index)
            } else {
                items[index] = item
            }

        } else {

            if (item.quantity > 0) {
                items.add(item)
            }
        }

        saveOrderItems(items)
    }

    // حذف مادة من الطلبية
    fun removeOrderItem(medicineId: Long) {

        val items = getOrderItems()

        items.removeAll {
            it.medicineId == medicineId
        }

        saveOrderItems(items)
    }

    // حفظ القائمة كاملة
    private fun saveOrderItems(
        items: List<OrderItem>
    ) {

        val array = JSONArray()

        items.forEach { item ->

            val obj = JSONObject()

            obj.put(
                "medicineId",
                item.medicineId
            )

            obj.put(
                "medicineName",
                item.medicineName
            )

            obj.put(
                "quantity",
                item.quantity
            )

            obj.put(
                "supplier",
                item.supplier
            )

            obj.put(
                "notes",
                item.notes
            )

            array.put(obj)
        }

        preferences.edit()
            .putString(
                KEY_ORDER_ITEMS,
                array.toString()
            )
            .apply()
    }

    // حفظ رقم العلاج الذي وصلنا إليه
    fun saveCurrentIndex(index: Int) {

        preferences.edit()
            .putInt(
                KEY_CURRENT_INDEX,
                index
            )
            .apply()
    }

    // معرفة أين توقفنا
    fun getCurrentIndex(): Int {

        return preferences.getInt(
            KEY_CURRENT_INDEX,
            0
        )
    }

    // عدد المواد المطلوبة
    fun getOrderCount(): Int {

        return getOrderItems().size
    }

    // مسح الطلبية وبدء طلبية جديدة
    fun clearOrder() {

        preferences.edit()
            .remove(KEY_ORDER_ITEMS)
            .putInt(
                KEY_CURRENT_INDEX,
                0
            )
            .apply()
    }

    // طلبية أمازون فقط
    fun getAmazonItems(): List<OrderItem> {

        return getOrderItems().filter {
            it.supplier.equals(
                "أمازون",
                ignoreCase = true
            )
        }
    }

    // طلبية راية فقط
    fun getRayaItems(): List<OrderItem> {

        return getOrderItems().filter {
            it.supplier.equals(
                "راية",
                ignoreCase = true
            )
        }
    }

    // تجهيز نص الطلبية للنسخ
    fun buildOrderText(): String {

        val items = getOrderItems()

        if (items.isEmpty()) {
            return "لا توجد مواد في الطلبية"
        }

        val amazon = items.filter {
            it.supplier == "أمازون"
        }

        val raya = items.filter {
            it.supplier == "راية"
        }

        val text = StringBuilder()

        if (amazon.isNotEmpty()) {

            text.append("طلبية أمازون\n")
            text.append("--------------------\n")

            amazon.forEach { item ->

                text.append(item.medicineName)
                text.append(" عدد ")
                text.append(item.quantity)

                if (item.notes.isNotBlank()) {
                    text.append(" - ")
                    text.append(item.notes)
                }

                text.append("\n")
            }
        }

        if (
            amazon.isNotEmpty() &&
            raya.isNotEmpty()
        ) {
            text.append("\n")
        }

        if (raya.isNotEmpty()) {

            text.append("طلبية راية\n")
            text.append("--------------------\n")

            raya.forEach { item ->

                text.append(item.medicineName)
                text.append(" عدد ")
                text.append(item.quantity)

                if (item.notes.isNotBlank()) {
                    text.append(" - ")
                    text.append(item.notes)
                }

                text.append("\n")
            }
        }

        return text.toString().trim()
    }
}