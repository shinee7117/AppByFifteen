package com.example.interpark.myapp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.interpark.myapp.DiaryReadActivity
import com.example.interpark.myapp.DiaryWriteActivity
import com.example.interpark.myapp.MainpageActivity
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.CalendarMode
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*

class MainpageActivity : BaseActivity(), View.OnClickListener {
    var calendar = Calendar.getInstance()
    var materialCalendarView: MaterialCalendarView? = null
    var btDate: Button? = null
    var btWrite: Button? = null
    var btRead: Button? = null
    var btAll: Button? = null
    var id: String? = null
    var name: String? = null
    var helper: dbHelper? = null
    var db: SQLiteDatabase? = null
    var lv: ListView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mainpage)
        val textView = findViewById(R.id.icHeader) as View
        val title = textView.findViewById<View>(R.id.tvHeader) as TextView
        val ibMenu = textView.findViewById<View>(R.id.ibMenu) as ImageButton
        val ibLogout = textView.findViewById<View>(R.id.ibLogout) as ImageButton
        title.text = "캘린더"
        btDate = findViewById<View>(R.id.btDate) as Button
        btWrite = findViewById<View>(R.id.btWrite) as Button
        btRead = findViewById<View>(R.id.btRead) as Button
        btAll = findViewById<View>(R.id.btAll) as Button
        materialCalendarView = findViewById<View>(R.id.calendarView) as MaterialCalendarView
        materialCalendarView!!.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setMinimumDate(CalendarDay.from(2010, 0, 1))
                .setMaximumDate(CalendarDay.from(2050, 11, 31))
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit()
        val now = System.currentTimeMillis()
        val date = Date(now)
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val today = sdf.format(date)
        btDate!!.text = today
        ibMenu.setOnClickListener(this)
        ibLogout.setOnClickListener(this)
        btDate!!.setOnClickListener(this)
        btWrite!!.setOnClickListener(this)
        btRead!!.setOnClickListener(this)
        val intent = intent
        name = intent.extras.getString("name")
        id = intent.extras.getString("id")
        Toast.makeText(this, "$name 님 어서오세요!", Toast.LENGTH_SHORT).show()
        val diaryVOS = ArrayList<DiaryVO>()
        diaryVOS.add(DiaryVO())
    }

    override fun onClick(v: View) {
        super.onClick(v)
        try {
            helper = dbHelper(this@MainpageActivity)
            db = helper!!.readableDatabase
        } catch (e: SQLiteException) {
        }
        when (v.id) {
            R.id.btDate -> DatePickerDialog(this@MainpageActivity, AlertDialog.THEME_DEVICE_DEFAULT_DARK, dataSetListener, calendar[Calendar.YEAR], calendar[Calendar.MONTH],
                    calendar[Calendar.DAY_OF_MONTH] + 1).show()
            R.id.btWrite -> {
                val ckDate = btDate!!.text.toString()
                val ckId = id
                val sbCk = StringBuffer()
                sbCk.append("SELECT * FROM diary WHERE id = #id# AND ddate = #date#")
                var queryCk = sbCk.toString()
                queryCk = queryCk.replace("#id#", "'$ckId'")
                queryCk = queryCk.replace("#date#", "'$ckDate'")
                val cursorCk: Cursor
                cursorCk = db!!.rawQuery(queryCk, null)
                if (cursorCk.moveToNext()) {
                    val no = cursorCk.getString(0)
                    val alertDialogBuilder = AlertDialog.Builder(this@MainpageActivity)
                    alertDialogBuilder.setTitle("수정하기")
                    alertDialogBuilder
                            .setMessage(ckDate + "의 일기를 수정하시겠습니까??")
                            .setCancelable(false)
                            .setPositiveButton("예") { dialog, which -> modifyDiaryData(ckDate, ckId) }
                            .setNegativeButton("아니요") { dialog, which -> dialog.cancel() }
                    val alertDialog = alertDialogBuilder.create()
                    alertDialog.show()
                } else {
                    val intent = Intent(this@MainpageActivity, DiaryWriteActivity::class.java)
                    intent.putExtra("date", btDate!!.text.toString())
                    intent.putExtra("Id", id)
                    startActivity(intent)
                }
            }
            R.id.btRead -> {
                val selectDate = btDate!!.text.toString()
                val diary = DiaryVO()
                diary.id = id!!
                diary.ddate = selectDate
                val sb = StringBuffer()
                sb.append("SELECT * FROM diary WHERE id = #id# AND ddate = #date#")
                var query = sb.toString()
                query = query.replace("#id#", "'" + diary.id + "'")
                query = query.replace("#date#", "'" + diary.ddate + "'")
                val cursor: Cursor
                cursor = db!!.rawQuery(query, null)
                if (cursor.moveToNext()) {
                    val no = cursor.getString(0)
                    val date = cursor.getString(1)
                    val dimgpath = cursor.getString(3)
                    val content = cursor.getString(4)
                    val id = cursor.getString(5)
                    if (no != null) {
                        Toast.makeText(applicationContext, no + date + dimgpath + content + id, Toast.LENGTH_SHORT).show()
                    }
                    cursor.close()
                    db!!.close()
                    val readIntent = Intent(this@MainpageActivity, DiaryReadActivity::class.java)
                    readIntent.putExtra("no", no)
                    readIntent.putExtra("date", date)
                    readIntent.putExtra("dimgpath", dimgpath)
                    readIntent.putExtra("content", content)
                    readIntent.putExtra("id", id)
                    startActivity(readIntent)
                } else {
                    Toast.makeText(applicationContext, "오늘의 일기가 없습니다.", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        }
    }

    private val dataSetListener = OnDateSetListener { view, year, month, dayOfMonth ->
        var month = month
        val newYear: String
        val newMonth: String
        val newDayOfMonth: String
        newYear = year.toString()
        month = month + 1
        newMonth = if (month <= 9) {
            "0$month"
        } else {
            month.toString()
        }
        newDayOfMonth = if (dayOfMonth <= 9) {
            "0$dayOfMonth"
        } else {
            dayOfMonth.toString()
        }
        val msg = "$newYear-$newMonth-$newDayOfMonth"
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
        btDate!!.text = msg
    }

    private fun modifyDiaryData(ckDate: String, ckId: String?) {
        try {
            helper = dbHelper(this@MainpageActivity)
            db = helper!!.readableDatabase
        } catch (e: SQLiteException) {
        }
        val sb = StringBuffer()
        sb.append("SELECT * FROM DIARY WHERE id = #id# AND ddate = #ddate#")
        var query = sb.toString()
        query = query.replace("#id#", "'$ckId'")
        query = query.replace("#ddate#", "'$ckDate'")
        val cursor: Cursor
        cursor = db!!.rawQuery(query, null)
        if (cursor.moveToNext()) {
            val no = cursor.getString(0)
            val date = cursor.getString(1)
            val dimgpath = cursor.getString(3)
            val content = cursor.getString(4)
            val id = cursor.getString(5)
            cursor.close()
            db!!.close()
            val updatIntent = Intent(this@MainpageActivity, DiaryWriteActivity::class.java)
            updatIntent.putExtra("no", no)
            updatIntent.putExtra("date", date)
            updatIntent.putExtra("dimgpath", dimgpath)
            updatIntent.putExtra("content", content)
            updatIntent.putExtra("id", id)
            updatIntent.putExtra("FLAG", "UPDATESIGN")
            UPDATEIS = "O"
            startActivity(updatIntent)
        }
    }

    companion object {
        var UPDATEIS = "X"
    }
}