package com.mustafa.talabatidaily

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class OrderStorage(context: Context) {

    private val preferences =
        context.getSharedPreferences(
            "talabati_daily_order",
            Context.MODE_PRIVATE
        )

    companion object {
        private const val KEY_ORDER_ITEMS = "order_items"
        private const val KEY_CURRENT_INDEX = "current_index"
        private const val KEY_HISTORY = "order_history"

        private const val THIRTY_DAYS =
            30L * 24L * 60L * 60L * 1000L
    }

    fun getOrderItems(): MutableList<OrderItem> {

        val json =
            preferences.getString(
                KEY_ORDER_ITEMS,
                null
            ) ?: "[]"

        return decodeItems(json)
    }

    fun saveOrderItem(
        item: OrderItem
    ) {

        val items =
            getOrderItems()

        val index =
            items.indexOfFirst {
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

    fun removeOrderItem(
        medicineId: Long
    ) {

        val items =
            getOrderItems()

        items.removeAll {
            it.medicineId == medicineId
        }

        saveOrderItems(items)
    }

    fun saveCurrentIndex(
        index: Int
    ) {

        preferences
            .edit()
            .putInt(
                KEY_CURRENT_INDEX,
                index
            )
            .apply()
    }

    fun getCurrentIndex(): Int {

        return preferences.getInt(
            KEY_CURRENT_INDEX,
            0
        )
    }

    fun getOrderCount(): Int {

        return getOrderItems().size
    }

    fun clearOrder() {

        preferences
            .edit()
            .remove(KEY_ORDER_ITEMS)
            .putInt(
                KEY_CURRENT_INDEX,
                0
            )
            .apply()
    }

    fun completeCurrentOrder(): Boolean {

        val items =
            getOrderItems()

        if (items.isEmpty()) {
            return false
        }

        val history =
            getHistoryArray()

        val completedOrder =
            JSONObject().apply {

                put(
                    "timestamp",
                    System.currentTimeMillis()
                )

                put(
                    "items",
                    itemsToArray(items)
                )
            }

        history.put(
            completedOrder
        )

        preferences
            .edit()
            .putString(
                KEY_HISTORY,
                history.toString()
            )
            .remove(
                KEY_ORDER_ITEMS
            )
            .putInt(
                KEY_CURRENT_INDEX,
                0
            )
            .apply()

        return true
    }

    fun getMonthlyTotals(): Map<Long, Int> {

        val cutoff =
            System.currentTimeMillis() -
                    THIRTY_DAYS

        val totals =
            linkedMapOf<Long, Int>()

        val history =
            getHistoryArray()

        for (
            i in 0 until history.length()
        ) {

            val order =
                history.optJSONObject(i)
                    ?: continue

            val timestamp =
                order.optLong(
                    "timestamp"
                )

            if (
                timestamp < cutoff
            ) {
                continue
            }

            val items =
                order.optJSONArray(
                    "items"
                ) ?: continue

            for (
                j in 0 until items.length()
            ) {

                val item =
                    items.optJSONObject(j)
                        ?: continue

                val medicineId =
                    item.optLong(
                        "medicineId"
                    )

                val quantity =
                    item.optInt(
                        "quantity"
                    )

                totals[medicineId] =
                    (totals[medicineId] ?: 0) +
                            quantity
            }
        }

        return totals
    }

    fun getCompletedOrderCount(): Int {

        return getHistoryArray().length()
    }

    fun buildOrderText(): String {

        val items =
            getOrderItems()

        if (items.isEmpty()) {

            return "لا توجد مواد في الطلبية"
        }

        val text =
            StringBuilder()

        fun addSection(
            title: String,
            list: List<OrderItem>
        ) {

            if (list.isEmpty()) {
                return
            }

            if (text.isNotEmpty()) {

                text.append(
                    "\n\n"
                )
            }

            text.append(title)

            text.append(
                "\n--------------------\n"
            )

            list.forEach { item ->

                text.append(
                    item.medicineName
                )

                text.append(
                    " عدد "
                )

                text.append(
                    item.quantity
                )

                if (
                    item.notes.isNotBlank()
                ) {

                    text.append(
                        " - "
                    )

                    text.append(
                        item.notes
                    )
                }

                text.append(
                    "\n"
                )
            }
        }

        addSection(
            "طلبية أمازون",
            items.filter {

                it.supplier.equals(
                    "أمازون",
                    ignoreCase = true
                )
            }
        )

        addSection(
            "طلبية راية",
            items.filter {

                it.supplier.equals(
                    "راية",
                    ignoreCase = true
                )
            }
        )

        addSection(
            "بدون مذخر",
            items.filter {

                !it.supplier.equals(
                    "أمازون",
                    ignoreCase = true
                ) &&

                !it.supplier.equals(
                    "راية",
                    ignoreCase = true
                )
            }
        )

        return text
            .toString()
            .trim()
    }

    fun exportJson(): JSONObject {

        return JSONObject().apply {

            put(
                "currentItems",
                preferences.getString(
                    KEY_ORDER_ITEMS,
                    "[]"
                ) ?: "[]"
            )

            put(
                "currentIndex",
                getCurrentIndex()
            )

            put(
                "history",
                preferences.getString(
                    KEY_HISTORY,
                    "[]"
                ) ?: "[]"
            )
        }
    }

    fun importJson(
        obj: JSONObject
    ) {

        val currentItems =
            obj.optString(
                "currentItems",
                "[]"
            )

        val history =
            obj.optString(
                "history",
                "[]"
            )

        // التأكد من صحة البيانات
        JSONArray(currentItems)
        JSONArray(history)

        preferences
            .edit()
            .putString(
                KEY_ORDER_ITEMS,
                currentItems
            )
            .putInt(
                KEY_CURRENT_INDEX,
                obj.optInt(
                    "currentIndex",
                    0
                )
            )
            .putString(
                KEY_HISTORY,
                history
            )
            .apply()
    }

    private fun getHistoryArray(): JSONArray {

        return try {

            JSONArray(
                preferences.getString(
                    KEY_HISTORY,
                    "[]"
                ) ?: "[]"
            )

        } catch (
            _: Exception
        ) {

            JSONArray()
        }
    }

    private fun saveOrderItems(
        items: List<OrderItem>
    ) {

        preferences
            .edit()
            .putString(
                KEY_ORDER_ITEMS,
                itemsToArray(items)
                    .toString()
            )
            .apply()
    }

    private fun itemsToArray(
        items: List<OrderItem>
    ): JSONArray {

        val array =
            JSONArray()

        items.forEach { item ->

            array.put(

                JSONObject().apply {

                    put(
                        "medicineId",
                        item.medicineId
                    )

                    put(
                        "medicineName",
                        item.medicineName
                    )

                    put(
                        "quantity",
                        item.quantity
                    )

                    put(
                        "supplier",
                        item.supplier
                    )

                    put(
                        "notes",
                        item.notes
                    )
                }
            )
        }

        return array
    }

    private fun decodeItems(
        json: String
    ): MutableList<OrderItem> {

        return try {

            val array =
                JSONArray(json)

            MutableList(
                array.length()
            ) { index ->

                val item =
                    array.getJSONObject(
                        index
                    )

                OrderItem(
                    medicineId =
                        item.optLong(
                            "medicineId"
                        ),

                    medicineName =
                        item.optString(
                            "medicineName"
                        ),

                    quantity =
                        item.optInt(
                            "quantity"
                        ),

                    supplier =
                        item.optString(
                            "supplier"
                        ),

                    notes =
                        item.optString(
                            "notes"
                        )
                )
            }

        } catch (
            _: Exception
        ) {

            mutableListOf()
        }
    }
}