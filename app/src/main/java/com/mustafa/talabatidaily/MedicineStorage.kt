package com.mustafa.talabatidaily

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class MedicineStorage(context: Context) {

    private val preferences = context.getSharedPreferences(
        "talabati_daily_database",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_MEDICINES = "medicines"
        private const val KEY_NEXT_ID = "next_medicine_id"
    }

    // جلب جميع العلاجات
    fun getMedicines(): MutableList<Medicine> {

        val json = preferences.getString(KEY_MEDICINES, null)
            ?: return mutableListOf()

        return try {

            val array = JSONArray(json)

            val medicines = mutableListOf<Medicine>()

            for (i in 0 until array.length()) {

                val item = array.getJSONObject(i)

                medicines.add(
                    Medicine(
                        id = item.optLong("id"),
                        name = item.optString("name"),
                        defaultSupplier = item.optString("supplier"),
                        defaultQuantity = item.optInt("quantity"),
                        notes = item.optString("notes")
                    )
                )
            }

            medicines

        } catch (e: Exception) {

            mutableListOf()
        }
    }

    // إضافة علاج جديد
    fun addMedicine(
        name: String,
        supplier: String = "",
        quantity: Int = 0,
        notes: String = ""
    ): Medicine {

        val medicines = getMedicines()

        val medicine = Medicine(
            id = getNextId(),
            name = name.trim(),
            defaultSupplier = supplier,
            defaultQuantity = quantity,
            notes = notes.trim()
        )

        medicines.add(medicine)

        saveMedicines(medicines)

        return medicine
    }

    // تعديل علاج موجود
    fun updateMedicine(updatedMedicine: Medicine): Boolean {

        val medicines = getMedicines()

        val index = medicines.indexOfFirst {
            it.id == updatedMedicine.id
        }

        if (index == -1) {
            return false
        }

        medicines[index] = updatedMedicine

        saveMedicines(medicines)

        return true
    }

    // حذف علاج
    fun deleteMedicine(id: Long): Boolean {

        val medicines = getMedicines()

        val removed = medicines.removeAll {
            it.id == id
        }

        if (removed) {
            saveMedicines(medicines)
        }

        return removed
    }

    // البحث عن علاج
    fun searchMedicines(query: String): List<Medicine> {

        if (query.isBlank()) {
            return getMedicines()
        }

        return getMedicines().filter {

            it.name.contains(
                query.trim(),
                ignoreCase = true
            ) ||

            it.notes.contains(
                query.trim(),
                ignoreCase = true
            )
        }
    }

    // حذف جميع العلاجات
    fun deleteAllMedicines() {

        preferences.edit()
            .remove(KEY_MEDICINES)
            .apply()
    }

    // عدد العلاجات
    fun count(): Int {
        return getMedicines().size
    }

    // حفظ القائمة كاملة
    private fun saveMedicines(
        medicines: List<Medicine>
    ) {

        val array = JSONArray()

        medicines.forEach { medicine ->

            val item = JSONObject()

            item.put("id", medicine.id)
            item.put("name", medicine.name)
            item.put(
                "supplier",
                medicine.defaultSupplier
            )
            item.put(
                "quantity",
                medicine.defaultQuantity
            )
            item.put(
                "notes",
                medicine.notes
            )

            array.put(item)
        }

        preferences.edit()
            .putString(
                KEY_MEDICINES,
                array.toString()
            )
            .apply()
    }

    // إنشاء رقم خاص لكل علاج
    private fun getNextId(): Long {

        val current = preferences.getLong(
            KEY_NEXT_ID,
            1L
        )

        preferences.edit()
            .putLong(
                KEY_NEXT_ID,
                current + 1
            )
            .apply()

        return current
    }
}