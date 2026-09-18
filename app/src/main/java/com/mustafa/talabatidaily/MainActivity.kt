package com.mustafa.talabatidaily

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var root: LinearLayout
    private lateinit var medicineStorage: MedicineStorage
    private lateinit var orderStorage: OrderStorage

    private var currentMedicines = mutableListOf<Medicine>()
    private var currentIndex = 0

    private val blue = Color.rgb(21, 101, 192)
    private val green = Color.rgb(46, 125, 50)
    private val red = Color.rgb(198, 40, 40)
    private val background = Color.rgb(247, 248, 250)
    private val darkText = Color.rgb(30, 30, 30)
    private val grayText = Color.rgb(100, 100, 100)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        medicineStorage = MedicineStorage(this)
        orderStorage = OrderStorage(this)

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 45, 32, 50)
            setBackgroundColor(background)
        }

        val scrollView = ScrollView(this).apply {
    isFillViewport = true
    addView(
        root,
        LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    )
}

        setContentView(scrollView)

        showHome()
    }

    // =========================================================
    // الصفحة الرئيسية
    // =========================================================

    private fun showHome() {
        hideKeyboard()
        root.removeAllViews()

        addSpace(35)

        addTitle("طلباتي اليومية")

        addText(
            "إدارة طلبات الصيدلية",
            18f,
            grayText
        )

        addSpace(15)

        val count = medicineStorage.count()

        addText(
            "قاعدة البيانات: $count علاج",
            15f,
            grayText
        )

        val orderCount = orderStorage.getOrderCount()

        if (orderCount > 0) {
            addText(
                "الطلبية الحالية: $orderCount مادة",
                15f,
                green
            )
        }

        addSpace(45)

        addButton(
            text = "هيا نبدأ",
            color = blue
        ) {
            startOrder()
        }

        addSpace(15)

        addButton(
            text = "قاعدة بيانات العلاجات",
            color = green
        ) {
            showDatabase()
        }

        addSpace(15)

        addButton(
            text = "الطلبية الحالية",
            color = Color.rgb(90, 70, 160)
        ) {
            showCurrentOrder()
        }

        addSpace(15)

        if (orderCount > 0) {
            addButton(
                text = "بدء طلبية جديدة",
                color = red
            ) {
                confirmNewOrder()
            }
        }
    }

    // =========================================================
    // قاعدة البيانات
    // =========================================================

    private fun showDatabase(search: String = "") {
        hideKeyboard()
        root.removeAllViews()

        addTopBar(
            title = "قاعدة بيانات العلاجات",
            onBack = { showHome() }
        )

        addSpace(15)

        val searchInput = EditText(this).apply {
            hint = "بحث عن علاج"
            textSize = 17f
            setSingleLine(true)
            setText(search)
            setPadding(25, 10, 25, 10)
        }

        root.addView(
            searchInput,
            matchWrap()
        )

        addSpace(10)

        addButton(
            "بحث",
            blue
        ) {
            showDatabase(
                searchInput.text.toString().trim()
            )
        }

        addSpace(10)

        addButton(
            "إضافة علاج جديد",
            green
        ) {
            showMedicineEditor(null)
        }

        addSpace(20)

        val medicines =
            if (search.isBlank()) {
                medicineStorage.getMedicines()
            } else {
                medicineStorage.searchMedicines(search)
            }

        addText(
            "عدد النتائج: ${medicines.size}",
            15f,
            grayText
        )

        addSpace(10)

        if (medicines.isEmpty()) {

            addText(
                if (search.isBlank())
                    "لا توجد علاجات بعد.\nاضغط «إضافة علاج جديد» وابدأ بإدخال قاعدة البيانات."
                else
                    "لم يتم العثور على علاج بهذا الاسم.",
                17f,
                grayText
            )

            return
        }

        medicines.forEachIndexed { index, medicine ->

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(22, 18, 22, 18)
                setBackgroundColor(Color.WHITE)
            }

            val name = TextView(this).apply {
                text = "${index + 1}. ${medicine.name}"
                textSize = 19f
                setTextColor(darkText)
                gravity = Gravity.START
            }

            card.addView(name, matchWrap())

            if (medicine.defaultSupplier.isNotBlank()) {
                val supplier = TextView(this).apply {
                    text = "المذخر الافتراضي: ${medicine.defaultSupplier}"
                    textSize = 14f
                    setTextColor(grayText)
                }
                card.addView(supplier, matchWrap())
            }

            if (medicine.defaultQuantity > 0) {
                val qty = TextView(this).apply {
                    text = "الكمية الافتراضية: ${medicine.defaultQuantity}"
                    textSize = 14f
                    setTextColor(grayText)
                }
                card.addView(qty, matchWrap())
            }

            if (medicine.notes.isNotBlank()) {
                val notes = TextView(this).apply {
                    text = medicine.notes
                    textSize = 14f
                    setTextColor(grayText)
                }
                card.addView(notes, matchWrap())
            }

            val buttons = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            val edit = Button(this).apply {
                text = "تعديل"
                isAllCaps = false
                setOnClickListener {
                    showMedicineEditor(medicine)
                }
            }

            val delete = Button(this).apply {
                text = "حذف"
                isAllCaps = false
                setTextColor(red)
                setOnClickListener {
                    confirmDeleteMedicine(medicine)
                }
            }

            buttons.addView(
                edit,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            buttons.addView(
                delete,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            card.addView(buttons, matchWrap())

            root.addView(card, matchWrap())

            addSpace(10)
        }
    }

    // =========================================================
    // إضافة / تعديل علاج
    // =========================================================

    private fun showMedicineEditor(
        medicine: Medicine?
    ) {
        hideKeyboard()
        root.removeAllViews()

        val editing = medicine != null

        addTopBar(
            if (editing) "تعديل العلاج"
            else "إضافة علاج جديد"
        ) {
            showDatabase()
        }

        addSpace(20)

        addText(
            "اسم العلاج",
            16f,
            darkText
        )

        val nameInput = EditText(this).apply {
            hint = "مثال: Neurobin amp"
            textSize = 18f
            setSingleLine(true)
            setText(medicine?.name ?: "")
        }

        root.addView(nameInput, matchWrap())

        addSpace(15)

        addText(
            "المذخر الافتراضي",
            16f,
            darkText
        )

        var selectedSupplier =
            medicine?.defaultSupplier ?: ""

        val supplierStatus = TextView(this).apply {
            text =
                if (selectedSupplier.isBlank())
                    "غير محدد"
                else
                    selectedSupplier

            textSize = 17f
            gravity = Gravity.CENTER
            setPadding(10, 15, 10, 15)
        }

        root.addView(supplierStatus, matchWrap())

        val supplierRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val amazon = Button(this).apply {
            text = "أمازون"
            isAllCaps = false
            setOnClickListener {
                selectedSupplier = "أمازون"
                supplierStatus.text = "أمازون"
                supplierStatus.setTextColor(blue)
            }
        }

        val raya = Button(this).apply {
            text = "راية"
            isAllCaps = false
            setOnClickListener {
                selectedSupplier = "راية"
                supplierStatus.text = "راية"
                supplierStatus.setTextColor(green)
            }
        }

        supplierRow.addView(
            amazon,
            LinearLayout.LayoutParams(
                0,
                130,
                1f
            )
        )

        supplierRow.addView(
            raya,
            LinearLayout.LayoutParams(
                0,
                130,
                1f
            )
        )

        root.addView(supplierRow, matchWrap())

        addSpace(15)

        addText(
            "الكمية الافتراضية - اختياري",
            16f,
            darkText
        )

        val quantityInput = EditText(this).apply {
            hint = "0"
            inputType = InputType.TYPE_CLASS_NUMBER
            textSize = 18f

            if (
                medicine != null &&
                medicine.defaultQuantity > 0
            ) {
                setText(
                    medicine.defaultQuantity.toString()
                )
            }
        }

        root.addView(quantityInput, matchWrap())

        addSpace(15)

        addText(
            "ملاحظات - اختياري",
            16f,
            darkText
        )

        val notesInput = EditText(this).apply {
            hint = "مثلاً الشركة أو شكل العبوة"
            textSize = 17f
            minLines = 2
            setText(medicine?.notes ?: "")
        }

        root.addView(notesInput, matchWrap())

        addSpace(25)

        addButton(
            if (editing) "حفظ التعديل"
            else "إضافة العلاج",
            green
        ) {

            val name =
                nameInput.text.toString().trim()

            if (name.isBlank()) {
                toast("اكتب اسم العلاج أولاً")
                return@addButton
            }

            val quantity =
                quantityInput.text
                    .toString()
                    .trim()
                    .toIntOrNull() ?: 0

            val notes =
                notesInput.text
                    .toString()
                    .trim()

            if (medicine == null) {

                medicineStorage.addMedicine(
                    name = name,
                    supplier = selectedSupplier,
                    quantity = quantity,
                    notes = notes
                )

                toast("تمت إضافة العلاج")

            } else {

                medicineStorage.updateMedicine(
                    medicine.copy(
                        name = name,
                        defaultSupplier =
                            selectedSupplier,
                        defaultQuantity =
                            quantity,
                        notes = notes
                    )
                )

                toast("تم حفظ التعديل")
            }

            showDatabase()
        }

        addSpace(10)

        addButton(
            "إلغاء",
            Color.DKGRAY
        ) {
            showDatabase()
        }
    }

    private fun confirmDeleteMedicine(
        medicine: Medicine
    ) {
        AlertDialog.Builder(this)
            .setTitle("حذف العلاج")
            .setMessage(
                "هل تريد حذف:\n${medicine.name} ؟"
            )
            .setPositiveButton("حذف") { _, _ ->

                medicineStorage.deleteMedicine(
                    medicine.id
                )

                orderStorage.removeOrderItem(
                    medicine.id
                )

                toast("تم حذف العلاج")
                showDatabase()
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    // =========================================================
    // بدء الطلبية
    // =========================================================

    private fun startOrder() {

        currentMedicines =
            medicineStorage
                .getMedicines()
                .toMutableList()

        if (currentMedicines.isEmpty()) {

            AlertDialog.Builder(this)
                .setTitle("قاعدة البيانات فارغة")
                .setMessage(
                    "أضف العلاجات أولاً ثم ابدأ الطلبية."
                )
                .setPositiveButton(
                    "إضافة علاج"
                ) { _, _ ->
                    showMedicineEditor(null)
                }
                .setNegativeButton(
                    "إلغاء",
                    null
                )
                .show()

            return
        }

        currentIndex =
            orderStorage.getCurrentIndex()

        if (
            currentIndex < 0 ||
            currentIndex >= currentMedicines.size
        ) {
            currentIndex = 0
        }

        showOrderMedicine()
    }

    // =========================================================
    // شاشة العلاج الواحد
    // =========================================================

    private fun showOrderMedicine() {
        hideKeyboard()
        root.removeAllViews()

        if (currentMedicines.isEmpty()) {
            showHome()
            return
        }

        if (currentIndex >= currentMedicines.size) {
            currentIndex =
                currentMedicines.size - 1
        }

        if (currentIndex < 0) {
            currentIndex = 0
        }

        val medicine =
            currentMedicines[currentIndex]

        val existingItem =
            orderStorage
                .getOrderItems()
                .firstOrNull {
                    it.medicineId == medicine.id
                }

        addTopBar(
            "الطلبية",
            onBack = { showHome() }
        )

        addSpace(10)

        addText(
            "${currentIndex + 1} / ${currentMedicines.size}",
            16f,
            grayText
        )

        addSpace(30)

        val medicineName = TextView(this).apply {
            text = medicine.name
            textSize = 29f
            gravity = Gravity.CENTER
            setTextColor(darkText)
            setPadding(15, 30, 15, 30)
            setBackgroundColor(Color.WHITE)
        }

        root.addView(
            medicineName,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        if (medicine.notes.isNotBlank()) {
            addSpace(10)
            addText(
                medicine.notes,
                15f,
                grayText
            )
        }

        addSpace(25)

        addText(
            "الكمية المطلوبة",
            17f,
            darkText
        )

        val quantityInput = EditText(this).apply {
            hint = "اكتب العدد"
            inputType = InputType.TYPE_CLASS_NUMBER
            textSize = 25f
            gravity = Gravity.CENTER
            setSingleLine(true)

            val quantity =
                existingItem?.quantity
                    ?: medicine.defaultQuantity

            if (quantity > 0) {
                setText(quantity.toString())
                selectAll()
            }
        }

        root.addView(
            quantityInput,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                140
            )
        )

        addSpace(20)

        addText(
            "اختر المذخر",
            17f,
            darkText
        )

        var selectedSupplier =
            existingItem?.supplier
                ?: medicine.defaultSupplier

        val supplierStatus = TextView(this).apply {
            text =
                if (selectedSupplier.isBlank())
                    "لم يتم اختيار مذخر"
                else
                    "المذخر: $selectedSupplier"

            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(10, 18, 10, 18)

            when (selectedSupplier) {
                "أمازون" -> setTextColor(blue)
                "راية" -> setTextColor(green)
                else -> setTextColor(grayText)
            }
        }

        root.addView(supplierStatus, matchWrap())

        val suppliers = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val amazonButton = Button(this).apply {
            text = "أمازون"
            textSize = 18f
            isAllCaps = false

            setOnClickListener {
                selectedSupplier = "أمازون"
                supplierStatus.text =
                    "المذخر: أمازون"
                supplierStatus.setTextColor(blue)
            }
        }

        val rayaButton = Button(this).apply {
            text = "راية"
            textSize = 18f
            isAllCaps = false

            setOnClickListener {
                selectedSupplier = "راية"
                supplierStatus.text =
                    "المذخر: راية"
                supplierStatus.setTextColor(green)
            }
        }

        suppliers.addView(
            amazonButton,
            LinearLayout.LayoutParams(
                0,
                140,
                1f
            )
        )

        suppliers.addView(
            rayaButton,
            LinearLayout.LayoutParams(
                0,
                140,
                1f
            )
        )

        root.addView(suppliers, matchWrap())

        addSpace(25)

        addButton(
            "حفظ والذهاب للتالي",
            green
        ) {

            saveMedicineToOrder(
                medicine,
                quantityInput,
                selectedSupplier
            ) {
                goNext()
            }
        }

        addSpace(10)

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val previous = Button(this).apply {
            text = "السابق"
            isAllCaps = false
            isEnabled = currentIndex > 0

            setOnClickListener {

                saveOptionalOrderItem(
                    medicine,
                    quantityInput,
                    selectedSupplier
                )

                if (currentIndex > 0) {
                    currentIndex--
                    orderStorage.saveCurrentIndex(
                        currentIndex
                    )
                    showOrderMedicine()
                }
            }
        }

        val skip = Button(this).apply {
            text = "تخطي"
            isAllCaps = false

            setOnClickListener {
                goNext()
            }
        }

        nav.addView(
            previous,
            LinearLayout.LayoutParams(
                0,
                125,
                1f
            )
        )

        nav.addView(
            skip,
            LinearLayout.LayoutParams(
                0,
                125,
                1f
            )
        )

        root.addView(nav, matchWrap())

        addSpace(15)

        addButton(
            "مراجعة الطلبية",
            Color.rgb(90, 70, 160)
        ) {
            saveOptionalOrderItem(
                medicine,
                quantityInput,
                selectedSupplier
            )

            showCurrentOrder()
        }
    }

    private fun saveMedicineToOrder(
        medicine: Medicine,
        quantityInput: EditText,
        supplier: String,
        afterSave: () -> Unit
    ) {

        val quantity =
            quantityInput.text
                .toString()
                .trim()
                .toIntOrNull() ?: 0

        if (quantity <= 0) {
            toast("اكتب الكمية المطلوبة")
            return
        }

        if (supplier.isBlank()) {
            toast("اختر أمازون أو راية")
            return
        }

        orderStorage.saveOrderItem(
            OrderItem(
                medicineId = medicine.id,
                medicineName = medicine.name,
                quantity = quantity,
                supplier = supplier,
                notes = medicine.notes
            )
        )

        afterSave()
    }

    private fun saveOptionalOrderItem(
        medicine: Medicine,
        quantityInput: EditText,
        supplier: String
    ) {

        val quantity =
            quantityInput.text
                .toString()
                .trim()
                .toIntOrNull() ?: 0

        if (
            quantity > 0 &&
            supplier.isNotBlank()
        ) {
            orderStorage.saveOrderItem(
                OrderItem(
                    medicineId = medicine.id,
                    medicineName = medicine.name,
                    quantity = quantity,
                    supplier = supplier,
                    notes = medicine.notes
                )
            )
        }
    }

    private fun goNext() {

        if (
            currentIndex <
            currentMedicines.size - 1
        ) {

            currentIndex++

            orderStorage.saveCurrentIndex(
                currentIndex
            )

            showOrderMedicine()

        } else {

            orderStorage.saveCurrentIndex(0)

            AlertDialog.Builder(this)
                .setTitle("اكتملت القائمة")
                .setMessage(
                    "وصلت إلى آخر علاج.\nهل تريد مراجعة الطلبية؟"
                )
                .setPositiveButton(
                    "مراجعة الطلبية"
                ) { _, _ ->
                    showCurrentOrder()
                }
                .setNegativeButton(
                    "الصفحة الرئيسية"
                ) { _, _ ->
                    showHome()
                }
                .show()
        }
    }

    // =========================================================
    // الطلبية الحالية
    // =========================================================

    private fun showCurrentOrder() {
        hideKeyboard()
        root.removeAllViews()

        addTopBar(
            "الطلبية الحالية",
            onBack = { showHome() }
        )

        val items =
            orderStorage.getOrderItems()

        addSpace(10)

        addText(
            "عدد المواد: ${items.size}",
            16f,
            grayText
        )

        addSpace(15)

        if (items.isEmpty()) {

            addText(
                "لا توجد مواد في الطلبية حالياً.",
                18f,
                grayText
            )

            addSpace(20)

            addButton(
                "العودة إلى الطلبية",
                blue
            ) {
                startOrder()
            }

            return
        }

        val amazon =
            items.filter {
                it.supplier == "أمازون"
            }

        val raya =
            items.filter {
                it.supplier == "راية"
            }

        if (amazon.isNotEmpty()) {

            addSectionTitle(
                "أمازون (${amazon.size})",
                blue
            )

            amazon.forEach {
                addOrderItemCard(it)
            }

            addSpace(15)
        }

        if (raya.isNotEmpty()) {

            addSectionTitle(
                "راية (${raya.size})",
                green
            )

            raya.forEach {
                addOrderItemCard(it)
            }

            addSpace(15)
        }

        addButton(
            "نسخ الطلبية كاملة",
            blue
        ) {
            copyText(
                orderStorage.buildOrderText()
            )
        }

        addSpace(10)

        if (amazon.isNotEmpty()) {
            addButton(
                "نسخ طلبية أمازون",
                blue
            ) {
                copySupplierOrder(
                    "أمازون",
                    amazon
                )
            }

            addSpace(10)
        }

        if (raya.isNotEmpty()) {
            addButton(
                "نسخ طلبية راية",
                green
            ) {
                copySupplierOrder(
                    "راية",
                    raya
                )
            }

            addSpace(10)
        }

        addButton(
            "مشاركة الطلبية",
            Color.rgb(90, 70, 160)
        ) {
            shareOrder(
                orderStorage.buildOrderText()
            )
        }

        addSpace(10)

        addButton(
            "متابعة المرور على العلاجات",
            Color.DKGRAY
        ) {
            startOrder()
        }

        addSpace(10)

        addButton(
            "طلبية جديدة",
            red
        ) {
            confirmNewOrder()
        }
    }

    private fun addOrderItemCard(
        item: OrderItem
    ) {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 15, 20, 15)
            setBackgroundColor(Color.WHITE)
        }

        val name = TextView(this).apply {
            text =
                "${item.medicineName}  ×  ${item.quantity}"

            textSize = 18f
            setTextColor(darkText)
        }

        card.addView(name, matchWrap())

        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val edit = Button(this).apply {
            text = "تعديل"
            isAllCaps = false

            setOnClickListener {
                editOrderItem(item)
            }
        }

        val delete = Button(this).apply {
            text = "حذف"
            isAllCaps = false
            setTextColor(red)

            setOnClickListener {

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("حذف من الطلبية")
                    .setMessage(item.medicineName)
                    .setPositiveButton(
                        "حذف"
                    ) { _, _ ->

                        orderStorage.removeOrderItem(
                            item.medicineId
                        )

                        showCurrentOrder()
                    }
                    .setNegativeButton(
                        "إلغاء",
                        null
                    )
                    .show()
            }
        }

        buttons.addView(
            edit,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        buttons.addView(
            delete,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        card.addView(buttons, matchWrap())

        root.addView(card, matchWrap())

        addSpace(8)
    }

    // =========================================================
    // تعديل مادة داخل الطلبية
    // =========================================================

    private fun editOrderItem(
        item: OrderItem
    ) {

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(45, 15, 45, 10)
        }

        val quantity = EditText(this).apply {
            hint = "الكمية"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(item.quantity.toString())
            selectAll()
        }

        layout.addView(quantity)

        var supplier = item.supplier

        val status = TextView(this).apply {
            text = "المذخر: $supplier"
            textSize = 17f
            gravity = Gravity.CENTER
            setPadding(5, 15, 5, 15)
        }

        layout.addView(status)

        val supplierRow =
            LinearLayout(this).apply {
                orientation =
                    LinearLayout.HORIZONTAL
            }

        val amazon = Button(this).apply {
            text = "أمازون"
            isAllCaps = false
            setOnClickListener {
                supplier = "أمازون"
                status.text =
                    "المذخر: أمازون"
            }
        }

        val raya = Button(this).apply {
            text = "راية"
            isAllCaps = false
            setOnClickListener {
                supplier = "راية"
                status.text =
                    "المذخر: راية"
            }
        }

        supplierRow.addView(
            amazon,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        supplierRow.addView(
            raya,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        layout.addView(supplierRow)

        AlertDialog.Builder(this)
            .setTitle(item.medicineName)
            .setView(layout)
            .setPositiveButton(
                "حفظ",
                null
            )
            .setNegativeButton(
                "إلغاء",
                null
            )
            .create()
            .also { dialog ->

                dialog.setOnShowListener {

                    dialog
                        .getButton(
                            AlertDialog.BUTTON_POSITIVE
                        )
                        .setOnClickListener {

                            val qty =
                                quantity.text
                                    .toString()
                                    .toIntOrNull()
                                    ?: 0

                            if (qty <= 0) {
                                toast(
                                    "الكمية غير صحيحة"
                                )
                                return@setOnClickListener
                            }

                            orderStorage.saveOrderItem(
                                item.copy(
                                    quantity = qty,
                                    supplier = supplier
                                )
                            )

                            dialog.dismiss()
                            showCurrentOrder()
                        }
                }

                dialog.show()
            }
    }

    // =========================================================
    // النسخ والمشاركة
    // =========================================================

    private fun copySupplierOrder(
        supplier: String,
        items: List<OrderItem>
    ) {

        val text = StringBuilder()

        text.append("طلبية $supplier\n")
        text.append("--------------------\n")

        items.forEach {

            text.append(it.medicineName)
            text.append(" عدد ")
            text.append(it.quantity)

            if (it.notes.isNotBlank()) {
                text.append(" - ")
                text.append(it.notes)
            }

            text.append("\n")
        }

        copyText(
            text.toString().trim()
        )
    }

    private fun copyText(text: String) {

        val clipboard =
            getSystemService(
                Context.CLIPBOARD_SERVICE
            ) as ClipboardManager

        val clip =
            ClipData.newPlainText(
                "طلباتي اليومية",
                text
            )

        clipboard.setPrimaryClip(clip)

        toast("تم نسخ الطلبية")
    }

    private fun shareOrder(text: String) {

        val intent = Intent(
            Intent.ACTION_SEND
        ).apply {

            type = "text/plain"

            putExtra(
                Intent.EXTRA_TEXT,
                text
            )
        }

        startActivity(
            Intent.createChooser(
                intent,
                "مشاركة الطلبية"
            )
        )
    }

    // =========================================================
    // طلبية جديدة
    // =========================================================

    private fun confirmNewOrder() {

        AlertDialog.Builder(this)
            .setTitle("طلبية جديدة")
            .setMessage(
                "سيتم مسح الطلبية الحالية فقط.\nقاعدة بيانات العلاجات لن تُحذف."
            )
            .setPositiveButton(
                "بدء طلبية جديدة"
            ) { _, _ ->

                orderStorage.clearOrder()

                currentIndex = 0

                toast(
                    "تم بدء طلبية جديدة"
                )

                startOrder()
            }
            .setNegativeButton(
                "إلغاء",
                null
            )
            .show()
    }

    // =========================================================
    // عناصر الواجهة
    // =========================================================

    private fun addTitle(text: String) {

        val view = TextView(this).apply {
            this.text = text
            textSize = 32f
            gravity = Gravity.CENTER
            setTextColor(blue)
            setPadding(10, 20, 10, 20)
        }

        root.addView(view, matchWrap())
    }

    private fun addSectionTitle(
        text: String,
        color: Int
    ) {

        val view = TextView(this).apply {
            this.text = text
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(color)
            setPadding(10, 18, 10, 18)
        }

        root.addView(view, matchWrap())
    }

    private fun addText(
        text: String,
        size: Float,
        color: Int
    ) {

        val view = TextView(this).apply {
            this.text = text
            textSize = size
            gravity = Gravity.CENTER
            setTextColor(color)
            setPadding(10, 8, 10, 8)
        }

        root.addView(view, matchWrap())
    }

    private fun addTopBar(
        title: String,
        onBack: () -> Unit
    ) {

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val back = Button(this).apply {
            text = "رجوع"
            isAllCaps = false
            setOnClickListener {
                onBack()
            }
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 23f
            gravity = Gravity.CENTER
            setTextColor(blue)
        }

        row.addView(
            back,
            LinearLayout.LayoutParams(
                0,
                120,
                0.35f
            )
        )

        row.addView(
            titleView,
            LinearLayout.LayoutParams(
                0,
                120,
                0.65f
            )
        )

        root.addView(row, matchWrap())
    }

    private fun addButton(
        text: String,
        color: Int,
        action: () -> Unit
    ) {

        val button = Button(this).apply {
            this.text = text
            textSize = 18f
            isAllCaps = false
            setTextColor(Color.WHITE)
            setBackgroundColor(color)

            setOnClickListener {
                action()
            }
        }

        root.addView(
            button,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                145
            )
        )
    }

    private fun addSpace(height: Int) {

        val space = View(this)

        root.addView(
            space,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                height
            )
        )
    }

    private fun matchWrap():
        LinearLayout.LayoutParams {

        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun toast(message: String) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun hideKeyboard() {

        val view = currentFocus ?: return

        val imm =
            getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager

        imm.hideSoftInputFromWindow(
            view.windowToken,
            0
        )

        view.clearFocus()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        showHome()
    }
}