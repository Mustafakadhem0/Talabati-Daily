package com.mustafa.talabatidaily

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(40, 70, 40, 40)
            setBackgroundColor(Color.rgb(247, 248, 250))
        }

        val scrollView = ScrollView(this).apply {
            addView(root)
        }

        setContentView(scrollView)

        showHome()
    }

    private fun showHome() {
        root.removeAllViews()

        addSpace(60)

        val title = TextView(this).apply {
            text = "طلباتي اليومية"
            textSize = 32f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(21, 101, 192))
        }

        root.addView(
            title,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        addSpace(15)

        val subtitle = TextView(this).apply {
            text = "إدارة طلبات الصيدلية"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.DKGRAY)
        }

        root.addView(subtitle)

        addSpace(60)

        addMainButton("هيا نبدأ") {
            showOrderStart()
        }

        addSpace(20)

        addMainButton("قاعدة بيانات العلاجات") {
            showDatabase()
        }

        addSpace(20)

        addMainButton("الطلبية الحالية") {
            showCurrentOrder()
        }
    }

    private fun showOrderStart() {
        root.removeAllViews()

        addHeader("إنشاء طلبية")

        addSpace(40)

        val info = TextView(this).apply {
            text = "هنا راح تظهر العلاجات واحداً بعد الآخر حسب قاعدة البيانات."
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.DKGRAY)
            setPadding(20, 20, 20, 20)
        }

        root.addView(info)

        addSpace(30)

        addMainButton("البدء") {
            showMessage(
                "بعد إضافة قاعدة البيانات، يبدأ عرض العلاجات من هنا."
            )
        }

        addSpace(20)

        addBackButton()
    }

    private fun showDatabase() {
        root.removeAllViews()

        addHeader("قاعدة بيانات العلاجات")

        addSpace(30)

        val info = TextView(this).apply {
            text = "من هنا سنضيف ونعدل ونحذف العلاجات."
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.DKGRAY)
        }

        root.addView(info)

        addSpace(30)

        addMainButton("إضافة علاج جديد") {
            showMessage("إضافة علاج جديد")
        }

        addSpace(20)

        addMainButton("عرض جميع العلاجات") {
            showMessage("قائمة العلاجات")
        }

        addSpace(20)

        addBackButton()
    }

    private fun showCurrentOrder() {
        root.removeAllViews()

        addHeader("الطلبية الحالية")

        addSpace(30)

        val empty = TextView(this).apply {
            text = "لا توجد مواد في الطلبية حالياً."
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(Color.DKGRAY)
        }

        root.addView(empty)

        addSpace(30)

        addBackButton()
    }

    private fun addHeader(text: String) {
        val header = TextView(this).apply {
            this.text = text
            textSize = 27f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(21, 101, 192))
            setPadding(10, 20, 10, 20)
        }

        root.addView(
            header,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun addMainButton(
        text: String,
        action: () -> Unit
    ) {
        val button = Button(this).apply {
            this.text = text
            textSize = 18f
            isAllCaps = false
            setOnClickListener {
                action()
            }
        }

        root.addView(
            button,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                150
            )
        )
    }

    private fun addBackButton() {
        val button = Button(this).apply {
            text = "رجوع"
            textSize = 17f
            isAllCaps = false

            setOnClickListener {
                showHome()
            }
        }

        root.addView(
            button,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                130
            )
        )
    }

    private fun showMessage(message: String) {
        root.removeAllViews()

        addHeader(message)

        addSpace(40)

        val text = TextView(this).apply {
            this.text = "هذه الوظيفة سيتم ربطها بقاعدة البيانات في الخطوات التالية."
            textSize = 18f
            gravity = Gravity.CENTER
           