package com.mustafa.talabatidaily

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class MedicineStorage(context: Context) {

    private val preferences =
        context.getSharedPreferences(
            "talabati_daily_database",
            Context.MODE_PRIVATE
        )

    companion object {
        private const val KEY_MEDICINES = "medicines"
        private const val KEY_NEXT_ID = "next_medicine_id"
    }

    fun getMedicines(): MutableList<Medicine> {

        val json =
            preferences.getString(KEY_MEDICINES, null)
                ?: return mutableListOf()

        return try {

            val array = JSONArray(json)

            MutableList(array.length()) { i ->

                val item = array.getJSONObject(i)

                Medicine(
                    id = item.optLong("id"),
                    name = item.optString("name"),
                    defaultSupplier = item.optString("supplier"),
                    defaultQuantity = item.optInt("quantity"),
                    notes = item.optString("notes")
                )
            }

        } catch (_: Exception) {
            mutableListOf()
        }
    }

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

    fun updateMedicine(
        updatedMedicine: Medicine
    ): Boolean {

        val medicines = getMedicines()

        val index =
            medicines.indexOfFirst {
                it.id == updatedMedicine.id
            }

        if (index < 0) {
            return false
        }

        medicines[index] = updatedMedicine

        saveMedicines(medicines)

        return true
    }

    fun deleteMedicine(
        id: Long
    ): Boolean {

        val medicines = getMedicines()

        val removed =
            medicines.removeAll {
                it.id == id
            }

        if (removed) {
            saveMedicines(medicines)
        }

        return removed
    }

    fun moveUp(
        id: Long
    ): Boolean {

        val medicines = getMedicines()

        val index =
            medicines.indexOfFirst {
                it.id == id
            }

        if (index <= 0) {
            return false
        }

        val previous =
            medicines[index - 1]

        medicines[index - 1] =
            medicines[index]

        medicines[index] =
            previous

        saveMedicines(medicines)

        return true
    }

    fun moveDown(
        id: Long
    ): Boolean {

        val medicines = getMedicines()

        val index =
            medicines.indexOfFirst {
                it.id == id
            }

        if (
            index < 0 ||
            index >= medicines.lastIndex
        ) {
            return false
        }

        val next =
            medicines[index + 1]

        medicines[index + 1] =
            medicines[index]

        medicines[index] =
            next

        saveMedicines(medicines)

        return true
    }

    fun searchMedicines(
        query: String
    ): List<Medicine> {

        if (query.isBlank()) {
            return getMedicines()
        }

        val text =
            query.trim()

        return getMedicines().filter {

            it.name.contains(
                text,
                ignoreCase = true
            ) ||

            it.notes.contains(
                text,
                ignoreCase = true
            )
        }
    }

    fun count(): Int {

        return getMedicines().size
    }

    fun exportJson(): JSONObject {

        return JSONObject().apply {

            put(
                "medicines",
                preferences.getString(
                    KEY_MEDICINES,
                    "[]"
                ) ?: "[]"
            )

            put(
                "nextId",
                preferences.getLong(
                    KEY_NEXT_ID,
                    1L
                )
            )
        }
    }

    fun importJson(
        obj: JSONObject
    ) {

        val raw =
            obj.optString(
                "medicines",
                "[]"
            )

        // التأكد من أن البيانات صحيحة
        JSONArray(raw)

        preferences
            .edit()
            .putString(
                KEY_MEDICINES,
                raw
            )
            .putLong(
                KEY_NEXT_ID,
                obj.optLong(
                    "nextId",
                    1L
                )
            )
            .apply()
    }

    private fun saveMedicines(
        medicines: List<Medicine>
    ) {

        val array =
            JSONArray()

        medicines.forEach { medicine ->

            array.put(

                JSONObject().apply {

                    put(
                        "id",
                        medicine.id
                    )

                    put(
                        "name",
                        medicine.name
                    )

                    put(
                        "supplier",
                        medicine.defaultSupplier
                    )

                    put(
                        "quantity",
                        medicine.defaultQuantity
                    )

                    put(
                        "notes",
                        medicine.notes
                    )
                }
            )
        }

        preferences
            .edit()
            .putString(
                KEY_MEDICINES,
                array.toString()
            )
            .apply()
    }

    private fun getNextId(): Long {

        val current =
            preferences.getLong(
                KEY_NEXT_ID,
                1L
            )

        preferences
            .edit()
            .putLong(
                KEY_NEXT_ID,
                current + 1
            )
            .apply()

        return current
    }
}