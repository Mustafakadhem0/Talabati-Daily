package com.mustafa.talabatidaily

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var root: LinearLayout
    private lateinit var scrollView: ScrollView

    private lateinit var medicineStorage: MedicineStorage
    private lateinit var orderStorage: OrderStorage

    private var currentMedicines = mutableListOf<Medicine>()
    private var currentIndex = 0
    private var databaseScrollY = 0

    private val blue = Color.rgb(21, 101, 192)
    private val green = Color.rgb(46, 125, 50)
    private val red = Color.rgb(198, 40, 40)
    private val purple = Color.rgb(90, 70, 160)
    private val orange = Color.rgb(239, 108, 0)

    private val background =
        Color.rgb(247, 248, 250)

    private val dark =
        Color.rgb(30, 30, 30)

    private val gray =
        Color.rgb(100, 100, 100)

    private val createBackup =
        registerForActivityResult(
            ActivityResultContracts.CreateDocument(
                "application/json"
            )
        ) { uri ->

            if (uri != null) {

                try {

                    val backup =
                        JSONObject().apply {

                            put(
                                "app",
                                "Talabati Daily"
                            )

                            put(
                                "version",
                                2
                            )

                            put(
                                "createdAt",
                                System.currentTimeMillis()
                            )

                            put(
                                "database",
                                medicineStorage.exportJson()
                            )

                            put(
                                "orders",
                                orderStorage.exportJson()
                            )
                        }

                    contentResolver
                        .openOutputStream(uri)
                        ?.bufferedWriter()
                        ?.use {

                            it.write(
                                backup.toString(2)
                            )
                        }

                    toast(
                        "تم إنشاء النسخة الاحتياطية"
                    )

                } catch (
                    e: Exception
                ) {

                    toast(
                        "فشل إنشاء النسخة: ${e.message}"
                    )
                }
            }
        }

    private val restoreBackup =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri != null) {

                try {

                    val text =
                        contentResolver
                            .openInputStream(uri)
                            ?.bufferedReader()
                            ?.use {
                                it.readText()
                            }
                            ?: error(
                                "الملف فارغ"
                            )

                    val backup =
                        JSONObject(text)

                    medicineStorage.importJson(
                        backup.getJSONObject(
                            "database"
                        )
                    )

                    orderStorage.importJson(
                        backup.getJSONObject(
                            "orders"
                        )
                    )

                    toast(
                        "تمت استعادة النسخة الاحتياطية"
                    )

                    showHome()

                } catch (
                    e: Exception
                ) {

                    toast(
                        "ملف النسخة غير صالح: ${e.message}"
                    )
                }
            }
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        medicineStorage =
            MedicineStorage(this)

        orderStorage =
            OrderStorage(this)

        root =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.VERTICAL

                gravity =
                    Gravity.CENTER_HORIZONTAL

                setPadding(
                    32,
                    45,
                    32,
                    50
                )

                setBackgroundColor(
                    background
                )
            }

        scrollView =
            ScrollView(this).apply {

                isFillViewport = true

                addView(root)
            }

        setContentView(
            scrollView
        )

        showHome()
    }

    private fun showHome() {

        hideKeyboard()

        root.removeAllViews()

        scrollView.scrollTo(
            0,
            0
        )

        addSpace(25)

        addTitle(
            "طلباتي اليومية"
        )

        addText(
            "إدارة طلبات الصيدلية",
            18f,
            gray
        )

        addSpace(8)

        addText(
            "عدد العلاجات: ${medicineStorage.count()}",
            15f,
            gray
        )

        val orderCount =
            orderStorage.getOrderCount()

        if (
            orderCount > 0
        ) {

            addText(
                "الطلبية الحالية: $orderCount مادة",
                15f,
                green
            )
        }

        addSpace(30)

        addButton(
            "هيا نبدأ",
            blue
        ) {

            startOrder()
        }

        addSpace(12)

        addButton(
            "قاعدة بيانات العلاجات",
            green
        ) {

            showDatabase()
        }

        addSpace(12)

        addButton(
            "معدل الصرف الشهري",
            orange
        ) {

            showMonthlyUsage()
        }

        addSpace(12)

        addButton(
            "النسخ الاحتياطي والاستعادة",
            Color.DKGRAY
        ) {

            showBackup()
        }

        addSpace(12)

        addButton(
            "الطلبية الحالية",
            purple
        ) {

            showCurrentOrder()
        }

        if (
            orderCount > 0
        ) {

            addSpace(12)

            addButton(
                "بدء طلبية جديدة",
                red
            ) {

                confirmNewOrder()
            }
        }
    }

    private fun showDatabase(
        search: String = "",
        restorePosition: Boolean = false
    ) {

        hideKeyboard()

        root.removeAllViews()

        addTopBar(
            "قاعدة بيانات العلاجات"
        ) {

            showHome()
        }

        addSpace(12)

        val searchInput =
            EditText(this).apply {

                hint =
                    "بحث عن علاج"

                textSize =
                    17f

                setSingleLine(
                    true
                )

                setText(
                    search
                )
            }

        root.addView(
            searchInput,
            matchWrap()
        )

        addSpace(8)

        addButton(
            "بحث",
            blue
        ) {

            showDatabase(
                searchInput
                    .text
                    .toString()
                    .trim()
            )
        }

        addSpace(8)

        addButton(
            "إضافة علاج جديد",
            green
        ) {

            databaseScrollY =
                scrollView.scrollY

            showMedicineEditor(
                null
            )
        }

        addSpace(15)

        val medicines =
            if (
                search.isBlank()
            ) {

                medicineStorage
                    .getMedicines()

            } else {

                medicineStorage
                    .searchMedicines(
                        search
                    )
            }

        addText(
            "عدد النتائج: ${medicines.size}",
            15f,
            gray
        )

        addSpace(8)

        medicines.forEachIndexed {
                index,
                medicine ->

            val card =
                LinearLayout(this).apply {

                    orientation =
                        LinearLayout.VERTICAL

                    setPadding(
                        20,
                        15,
                        20,
                        15
                    )

                    setBackgroundColor(
                        Color.WHITE
                    )
                }

            card.addView(
                TextView(this).apply {

                    text =
                        "${index + 1}. ${medicine.name}"

                    textSize =
                        19f

                    setTextColor(
                        dark
                    )
                },
                matchWrap()
            )

            if (
                medicine
                    .defaultSupplier
                    .isNotBlank()
            ) {

                card.addView(
                    TextView(this).apply {

                        text =
                            "المذخر: ${medicine.defaultSupplier}"

                        textSize =
                            14f

                        setTextColor(
                            gray
                        )
                    },
                    matchWrap()
                )
            }

            if (
                medicine
                    .defaultQuantity > 0
            ) {

                card.addView(
                    TextView(this).apply {

                        text =
                            "الكمية الافتراضية: ${medicine.defaultQuantity}"

                        textSize =
                            14f

                        setTextColor(
                            gray
                        )
                    },
                    matchWrap()
                )
            }

            if (
                medicine
                    .notes
                    .isNotBlank()
            ) {

                card.addView(
                    TextView(this).apply {

                        text =
                            medicine.notes

                        textSize =
                            14f

                        setTextColor(
                            gray
                        )
                    },
                    matchWrap()
                )
            }

            val orderRow =
                LinearLayout(this).apply {

                    orientation =
                        LinearLayout.HORIZONTAL
                }

            val upButton =
                Button(this).apply {

                    text = "↑"

                    isEnabled =
                        search.isBlank() &&
                        index > 0

                    setOnClickListener {

                        databaseScrollY =
                            scrollView.scrollY

                        medicineStorage
                            .moveUp(
                                medicine.id
                            )

                        showDatabase(
                            "",
                            true
                        )
                    }
                }

            val downButton =
                Button(this).apply {

                    text = "↓"

                    isEnabled =
                        search.isBlank() &&
                        index <
                        medicines.lastIndex

                    setOnClickListener {

                        databaseScrollY =
                            scrollView.scrollY

                        medicineStorage
                            .moveDown(
                                medicine.id
                            )

                        showDatabase(
                            "",
                            true
                        )
                    }
                }

            orderRow.addView(
                upButton,
                weightWrap()
            )

            orderRow.addView(
                downButton,
                weightWrap()
            )

            card.addView(
                orderRow,
                matchWrap()
            )

            val actionRow =
                LinearLayout(this).apply {

                    orientation =
                        LinearLayout.HORIZONTAL
                }

            val editButton =
                Button(this).apply {

                    text =
                        "تعديل"

                    isAllCaps =
                        false

                    setOnClickListener {

                        databaseScrollY =
                            scrollView.scrollY

                        showMedicineEditor(
                            medicine
                        )
                    }
                }

            val deleteButton =
                Button(this).apply {

                    text =
                        "حذف"

                    isAllCaps =
                        false

                    setTextColor(
                        red
                    )

                    setOnClickListener {

                        confirmDeleteMedicine(
                            medicine
                        )
                    }
                }

            actionRow.addView(
                editButton,
                weightWrap()
            )

            actionRow.addView(
                deleteButton,
                weightWrap()
            )

            card.addView(
                actionRow,
                matchWrap()
            )

            root.addView(
                card,
                matchWrap()
            )

            addSpace(8)
        }

        if (
            restorePosition
        ) {

            scrollView.post {

                scrollView.scrollTo(
                    0,
                    databaseScrollY
                )
            }
        }
    }

    private fun showMedicineEditor(
        medicine: Medicine?
    ) {

        hideKeyboard()

        root.removeAllViews()

        val editing =
            medicine != null

        addTopBar(
            if (editing)
                "تعديل العلاج"
            else
                "إضافة علاج"
        ) {

            showDatabase(
                "",
                true
            )
        }

        addSpace(15)

        addText(
            "اسم العلاج",
            16f,
            dark
        )

        val nameInput =
            EditText(this).apply {

                setText(
                    medicine?.name
                        ?: ""
                )

                hint =
                    "اسم العلاج"

                setSingleLine(
                    true
                )
            }

        root.addView(
            nameInput,
            matchWrap()
        )

        addSpace(12)

        addText(
            "المذخر الافتراضي",
            16f,
            dark
        )

        var supplier =
            medicine
                ?.defaultSupplier
                ?: ""

        val supplierStatus =
            TextView(this).apply {

                text =
                    if (
                        supplier.isBlank()
                    )
                        "غير محدد"
                    else
                        supplier

                textSize =
                    18f

                gravity =
                    Gravity.CENTER
            }

        root.addView(
            supplierStatus,
            matchWrap()
        )

        val supplierRow =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL
            }

        supplierRow.addView(
            Button(this).apply {

                text =
                    "أمازون"

                setOnClickListener {

                    supplier =
                        "أمازون"

                    supplierStatus.text =
                        supplier
                }
            },
            weightWrap()
        )

        supplierRow.addView(
            Button(this).apply {

                text =
                    "راية"

                setOnClickListener {

                    supplier =
                        "راية"

                    supplierStatus.text =
                        supplier
                }
            },
            weightWrap()
        )

        supplierRow.addView(
            Button(this).apply {

                text =
                    "بدون"

                setOnClickListener {

                    supplier =
                        ""

                    supplierStatus.text =
                        "غير محدد"
                }
            },
            weightWrap()
        )

        root.addView(
            supplierRow,
            matchWrap()
        )

        addSpace(12)

        addText(
            "الكمية الافتراضية",
            16f,
            dark
        )

        val quantityInput =
            EditText(this).apply {

                inputType =
                    InputType.TYPE_CLASS_NUMBER

                hint =
                    "0"

                if (
                    (medicine
                        ?.defaultQuantity
                        ?: 0) > 0
                ) {

                    setText(
                        medicine!!
                            .defaultQuantity
                            .toString()
                    )
                }
            }

        root.addView(
            quantityInput,
            matchWrap()
        )

        addSpace(12)

        addText(
            "ملاحظات",
            16f,
            dark
        )

        val notesInput =
            EditText(this).apply {

                minLines =
                    2

                setText(
                    medicine?.notes
                        ?: ""
                )
            }

        root.addView(
            notesInput,
            matchWrap()
        )

        addSpace(20)

        addButton(
            if (editing)
                "حفظ التعديل"
            else
                "إضافة العلاج",
            green
        ) {

            val medicineName =
                nameInput
                    .text
                    .toString()
                    .trim()

            if (
                medicineName.isBlank()
            ) {

                toast(
                    "اكتب اسم العلاج"
                )

                return@addButton
            }

            val quantity =
                quantityInput
                    .text
                    .toString()
                    .toIntOrNull()
                    ?: 0

            if (
                medicine == null
            ) {

                medicineStorage
                    .addMedicine(
                        medicineName,
                        supplier,
                        quantity,
                        notesInput
                            .text
                            .toString()
                    )

            } else {

                medicineStorage
                    .updateMedicine(

                        medicine.copy(

                            name =
                                medicineName,

                            defaultSupplier =
                                supplier,

                            defaultQuantity =
                                quantity,

                            notes =
                                notesInput
                                    .text
                                    .toString()
                                    .trim()
                        )
                    )
            }

            toast(
                if (editing)
                    "تم حفظ التعديل"
                else
                    "تمت إضافة العلاج"
            )

            showDatabase(
                "",
                true
            )
        }

        addSpace(8)

        addButton(
            "إلغاء",
            Color.DKGRAY
        ) {

            showDatabase(
                "",
                true
            )
        }
    }

    private fun confirmDeleteMedicine(
        medicine: Medicine
    ) {

        AlertDialog
            .Builder(this)
            .setTitle(
                "حذف العلاج"
            )
            .setMessage(
                "هل تريد حذف ${medicine.name}؟"
            )
            .setPositiveButton(
                "حذف"
            ) { _, _ ->

                databaseScrollY =
                    scrollView.scrollY

                medicineStorage
                    .deleteMedicine(
                        medicine.id
                    )

                orderStorage
                    .removeOrderItem(
                        medicine.id
                    )

                toast(
                    "تم حذف العلاج"
                )

                showDatabase(
                    "",
                    true
                )
            }
            .setNegativeButton(
                "إلغاء",
                null
            )
            .show()
    }

    private fun startOrder() {

        currentMedicines =
            medicineStorage
                .getMedicines()
                .toMutableList()

        if (
            currentMedicines.isEmpty()
        ) {

            AlertDialog
                .Builder(this)
                .setTitle(
                    "قاعدة البيانات فارغة"
                )
                .setMessage(
                    "أضف العلاجات أولاً."
                )
                .setPositiveButton(
                    "إضافة علاج"
                ) { _, _ ->

                    showMedicineEditor(
                        null
                    )
                }
                .setNegativeButton(
                    "إلغاء",
                    null
                )
                .show()

            return
        }

        currentIndex =
            orderStorage
                .getCurrentIndex()
                .coerceIn(
                    0,
                    currentMedicines.lastIndex
                )

        showOrderMedicine()
    }

    private fun showOrderMedicine() {

        hideKeyboard()

        root.removeAllViews()

        if (
            currentMedicines.isEmpty()
        ) {

            showHome()
            return
        }

        currentIndex =
            currentIndex.coerceIn(
                0,
                currentMedicines.lastIndex
            )

        val medicine =
            currentMedicines[
                currentIndex
            ]

        val existing =
            orderStorage
                .getOrderItems()
                .firstOrNull {

                    it.medicineId ==
                            medicine.id
                }

        addTopBar(
            "الطلبية"
        ) {

            showHome()
        }

        addText(
            "${currentIndex + 1} / ${currentMedicines.size}",
            16f,
            gray
        )

        addSpace(15)

        addTitle(
            medicine.name
        )

        if (
            medicine
                .notes
                .isNotBlank()
        ) {

            addText(
                medicine.notes,
                15f,
                gray
            )
        }

        addSpace(15)

        addText(
            "الكمية المطلوبة",
            17f,
            dark
        )

        val quantityInput =
            EditText(this).apply {

                inputType =
                    InputType.TYPE_CLASS_NUMBER

                textSize =
                    25f

                gravity =
                    Gravity.CENTER

                setSingleLine(
                    true
                )

                val quantity =
                    existing?.quantity
                        ?: medicine.defaultQuantity

                if (
                    quantity > 0
                ) {

                    setText(
                        quantity.toString()
                    )

                    selectAll()
                }
            }

        root.addView(
            quantityInput,
            LinearLayout.LayoutParams(
                -1,
                140
            )
        )

        addSpace(12)

        var supplier =
            existing?.supplier
                ?: medicine.defaultSupplier

        val supplierStatus =
            TextView(this).apply {

                text =
                    "المذخر: ${
                        if (supplier.isBlank())
                            "غير محدد"
                        else
                            supplier
                    }"

                gravity =
                    Gravity.CENTER

                textSize =
                    17f
            }

        root.addView(
            supplierStatus,
            matchWrap()
        )

        val supplierRow =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL
            }

        supplierRow.addView(
            Button(this).apply {

                text =
                    "أمازون"

                setOnClickListener {

                    supplier =
                        "أمازون"

                    supplierStatus.text =
                        "المذخر: أمازون"
                }
            },
            weightWrap()
        )

        supplierRow.addView(
            Button(this).apply {

                text =
                    "راية"

                setOnClickListener {

                    supplier =
                        "راية"

                    supplierStatus.text =
                        "المذخر: راية"
                }
            },
            weightWrap()
        )

        root.addView(
            supplierRow,
            matchWrap()
        )

        addSpace(15)

        fun saveAndGo(
            change: Int
        ) {

            val quantity =
                quantityInput
                    .text
                    .toString()
                    .toIntOrNull()
                    ?: 0

            orderStorage
                .saveOrderItem(

                    OrderItem(
                        medicineId =
                            medicine.id,

                        medicineName =
                            medicine.name,

                        quantity =
                            quantity,

                        supplier =
                            supplier,

                        notes =
                            medicine.notes
                    )
                )

            currentIndex =
                (currentIndex + change)
                    .coerceIn(
                        0,
                        currentMedicines.lastIndex
                    )

            orderStorage
                .saveCurrentIndex(
                    currentIndex
                )

            showOrderMedicine()
        }

        addButton(
            "حفظ والذهاب للتالي",
            green
        ) {

            val quantity =
                quantityInput
                    .text
                    .toString()
                    .toIntOrNull()
                    ?: 0

            orderStorage
                .saveOrderItem(

                    OrderItem(
                        medicineId =
                            medicine.id,

                        medicineName =
                            medicine.name,

                        quantity =
                            quantity,

                        supplier =
                            supplier,

                        notes =
                            medicine.notes
                    )
                )

            if (
                currentIndex <
                currentMedicines.lastIndex
            ) {

                currentIndex++

                orderStorage
                    .saveCurrentIndex(
                        currentIndex
                    )

                showOrderMedicine()

            } else {

                toast(
                    "وصلت إلى آخر مادة"
                )

                showCurrentOrder()
            }
        }

        addSpace(8)

        val navigationRow =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL
            }

        navigationRow.addView(
            Button(this).apply {

                text =
                    "السابق"

                isEnabled =
                    currentIndex > 0

                setOnClickListener {

                    saveAndGo(
                        -1
                    )
                }
            },
            weightWrap()
        )

        navigationRow.addView(
            Button(this).apply {

                text =
                    "التالي"

                isEnabled =
                    currentIndex <
                    currentMedicines.lastIndex

                setOnClickListener {

                    saveAndGo(
                        1
                    )
                }
            },
            weightWrap()
        )

        root.addView(
            navigationRow,
            matchWrap()
        )
    }

    private fun showCurrentOrder() {

        hideKeyboard()

        root.removeAllViews()

        addTopBar(
            "الطلبية الحالية"
        ) {

            showHome()
        }

        val items =
            orderStorage
                .getOrderItems()

        addText(
            "عدد المواد: ${items.size}",
            16f,
            gray
        )

        addSpace(10)

        if (
            items.isEmpty()
        ) {

            addText(
                "لا توجد مواد في الطلبية الحالية.",
                18f,
                gray
            )

            return
        }

        items.forEachIndexed {
                index,
                item ->

            val card =
                LinearLayout(this).apply {

                    orientation =
                        LinearLayout.VERTICAL

                    setPadding(
                        18,
                        14,
                        18,
                        14
                    )

                    setBackgroundColor(
                        Color.WHITE
                    )
                }

            card.addView(
                TextView(this).apply {

                    text =
                        "${index + 1}. ${item.medicineName} — عدد ${item.quantity}"

                    textSize =
                        18f
                },
                matchWrap()
            )

            card.addView(
                TextView(this).apply {

                    text =
                        "المذخر: ${
                            if (item.supplier.isBlank())
                                "غير محدد"
                            else
                                item.supplier
                        }"

                    textSize =
                        14f

                    setTextColor(
                        gray
                    )
                },
                matchWrap()
            )

            root.addView(
                card,
                matchWrap()
            )

            addSpace(7)
        }

        addButton(
            "نسخ الطلبية",
            blue
        ) {

            val clipboard =
                getSystemService(
                    Context.CLIPBOARD_SERVICE
                ) as ClipboardManager

            clipboard.setPrimaryClip(

                ClipData.newPlainText(
                    "طلباتي اليومية",
                    orderStorage
                        .buildOrderText()
                )
            )

            toast(
                "تم نسخ الطلبية"
            )
        }

        addSpace(10)

        addButton(
            "إكمال الطلبية وحفظها بالسجل",
            green
        ) {

            AlertDialog
                .Builder(this)
                .setTitle(
                    "إكمال الطلبية"
                )
                .setMessage(
                    "سيتم حفظ هذه الطلبية ضمن سجل معدل الصرف لآخر 30 يوم، ثم تبدأ طلبية جديدة."
                )
                .setPositiveButton(
                    "حفظ وإكمال"
                ) { _, _ ->

                    if (
                        orderStorage
                            .completeCurrentOrder()
                    ) {

                        toast(
                            "تم حفظ الطلبية في السجل"
                        )

                        showHome()
                    }
                }
                .setNegativeButton(
                    "إلغاء",
                    null
                )
                .show()
        }
    }

    private fun showMonthlyUsage() {

        hideKeyboard()

        root.removeAllViews()

        addTopBar(
            "معدل الصرف الشهري"
        ) {

            showHome()
        }

        addText(
            "مجموع الكمية المطلوبة لكل مادة خلال آخر 30 يوم",
            15f,
            gray
        )

        addText(
            "عدد الطلبيات المحفوظة: ${orderStorage.getCompletedOrderCount()}",
            14f,
            gray
        )

        addSpace(12)

        val totals =
            orderStorage
                .getMonthlyTotals()

        val medicines =
            medicineStorage
                .getMedicines()

        if (
            medicines.isEmpty()
        ) {

            addText(
                "لا توجد مواد.",
                18f,
                gray
            )

            return
        }

        medicines.forEachIndexed {
                index,
                medicine ->

            val total =
                totals[
                    medicine.id
                ] ?: 0

            val card =
                LinearLayout(this).apply {

                    orientation =
                        LinearLayout.HORIZONTAL

                    gravity =
                        Gravity.CENTER_VERTICAL

                    setPadding(
                        18,
                        16,
                        18,
                        16
                    )

                    setBackgroundColor(
                        Color.WHITE
                    )
                }

            card.addView(
                TextView(this).apply {

                    text =
                        "${index + 1}. ${medicine.name}"

                    textSize =
                        17f
                },
                LinearLayout.LayoutParams(
                    0,
                    -2,
                    1f
                )
            )

            card.addView(
                TextView(this).apply {

                    text =
                        total.toString()

                    textSize =
                        20f

                    gravity =
                        Gravity.CENTER

                    setTextColor(
                        if (total > 0)
                            green
                        else
                            gray
                    )
                },
                LinearLayout.LayoutParams(
                    160,
                    -2
                )
            )

            root.addView(
                card,
                matchWrap()
            )

            addSpace(6)
        }
    }

    private fun showBackup() {

        hideKeyboard()

        root.removeAllViews()

        addTopBar(
            "النسخ الاحتياطي والاستعادة"
        ) {

            showHome()
        }

        addSpace(15)

        addText(
            "النسخة تشمل قاعدة العلاجات وترتيبها والطلبية الحالية وسجل الطلبيات.",
            16f,
            gray
        )

        addSpace(20)

        addButton(
            "إنشاء نسخة احتياطية",
            green
        ) {

            val date =
                SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.US
                ).format(
                    Date()
                )

            createBackup.launch(
                "Talabati-Daily-Backup-$date.json"
            )
        }

        addSpace(12)

        addButton(
            "استعادة نسخة احتياطية",
            orange
        ) {

            AlertDialog
                .Builder(this)
                .setTitle(
                    "استعادة النسخة"
                )
                .setMessage(
                    "الاستعادة ستستبدل البيانات الحالية بالبيانات الموجودة داخل ملف النسخة الاحتياطية."
                )
                .setPositiveButton(
                    "اختيار الملف"
                ) { _, _ ->

                    restoreBackup.launch(
                        arrayOf(
                            "application/json",
                            "text/plain"
                        )
                    )
                }
                .setNegativeButton(
                    "إلغاء",
                    null
                )
                .show()
        }
    }

    private fun confirmNewOrder() {

        AlertDialog
            .Builder(this)
            .setTitle(
                "بدء طلبية جديدة"
            )
            .setMessage(
                "سيتم مسح الطلبية الحالية فقط. لن تُضاف إلى معدل الصرف لأنها غير مكتملة."
            )
            .setPositiveButton(
                "بدء جديدة"
            ) { _, _ ->

                orderStorage
                    .clearOrder()

                toast(
                    "تم بدء طلبية جديدة"
                )

                showHome()
            }
            .setNegativeButton(
                "إلغاء",
                null
            )
            .show()
    }

    private fun addTopBar(
        title: String,
        back: () -> Unit
    ) {

        val row =
            LinearLayout(this).apply {

                orientation =
                    LinearLayout.HORIZONTAL

                gravity =
                    Gravity.CENTER_VERTICAL
            }

        row.addView(
            Button(this).apply {

                text =
                    "رجوع"

                setOnClickListener {

                    back()
                }
            },
            LinearLayout.LayoutParams(
                220,
                -2
            )
        )

        row.addView(
            TextView(this).apply {

                text =
                    title

                textSize =
                    21f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    dark
                )
            },
            LinearLayout.LayoutParams(
                0,
                -2,
                1f
            )
        )

        root.addView(
            row,
            matchWrap()
        )
    }

    private fun addTitle(
        text: String
    ) {

        root.addView(
            TextView(this).apply {

                this.text =
                    text

                textSize =
                    28f

                gravity =
                    Gravity.CENTER

                setTextColor(
                    dark
                )

                setPadding(
                    8,
                    15,
                    8,
                    15
                )
            },
            matchWrap()
        )
    }

    private fun addText(
        text: String,
        size: Float,
        color: Int
    ) {

        root.addView(
            TextView(this).apply {

                this.text =
                    text

                textSize =
                    size

                gravity =
                    Gravity.CENTER

                setTextColor(
                    color
                )

                setPadding(
                    6,
                    5,
                    6,
                    5
                )
            },
            matchWrap()
        )
    }

    private fun addButton(
        text: String,
        color: Int,
        action: () -> Unit
    ) {

        root.addView(
            Button(this).apply {

                this.text =
                    text

                isAllCaps =
                    false

                textSize =
                    17f

                setTextColor(
                    Color.WHITE
                )

                setBackgroundColor(
                    color
                )

                setOnClickListener {

                    action()
                }
            },
            LinearLayout.LayoutParams(
                -1,
                135
            )
        )
    }

    private fun addSpace(
        height: Int
    ) {

        root.addView(
            View(this),
            LinearLayout.LayoutParams(
                1,
                height
            )
        )
    }

    private fun matchWrap():
            LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            -1,
            -2
        )
    }

    private fun weightWrap():
            LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            0,
            -2,
            1f
        )
    }

    private fun toast(
        message: String
    ) {

        Toast
            .makeText(
                this,
                message,
                Toast.LENGTH_SHORT
            )
            .show()
    }

    private fun hideKeyboard() {

        currentFocus?.let {

            val keyboard =
                getSystemService(
                    Context.INPUT_METHOD_SERVICE
                ) as InputMethodManager

            keyboard
                .hideSoftInputFromWindow(
                    it.windowToken,
                    0
                )
        }
    }
}