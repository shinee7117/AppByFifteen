package com.example.interpark.myapp

import android.app.AlertDialog
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import com.example.interpark.myapp.DiaryReadActivity

class DiaryReadActivity : BaseActivity(), View.OnClickListener {
    private val mImageCaptureUri: Uri? = null
    private var ivPic: ImageView? = null
    private val id_view = 0
    private var no: String? = null
    private var date: String? = null
    private var dimgpath: String? = null
    private var content: String? = null
    private var id: String? = null
    private val absoultePath: String? = null
    private var tvDate: TextView? = null
    private var tvRead: TextView? = null
    private var tvNo: TextView? = null
    private var btPre: Button? = null
    private var btHome: Button? = null
    private var btModify: Button? = null
    private var btNext: Button? = null
    private var btDelete: Button? = null
    var helper: dbHelper? = null
    var db: SQLiteDatabase? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read)
        val textView = findViewById(R.id.icHeader) as View
        val title = textView.findViewById<View>(R.id.tvHeader) as TextView
        val ibMenu = textView.findViewById<View>(R.id.ibMenu) as ImageButton
        val ibLogout = textView.findViewById<View>(R.id.ibLogout) as ImageButton
        title.text = "일기보기"
        tvDate = findViewById<View>(R.id.tvDate) as TextView
        ivPic = findViewById<View>(R.id.ivPic) as ImageView
        tvNo = findViewById<View>(R.id.tvNo) as TextView
        btHome = findViewById<View>(R.id.btHome) as Button
        tvRead = findViewById<View>(R.id.tvRead) as TextView
        btPre = findViewById<View>(R.id.btPre) as Button
        btNext = findViewById<View>(R.id.btNext) as Button
        btModify = findViewById<View>(R.id.btModify) as Button
        btDelete = findViewById<View>(R.id.btDelete) as Button
        val intent = intent
        no = intent.extras.getString("no")
        date = intent.extras.getString("date")
        dimgpath = intent.extras.getString("dimgpath")
        content = intent.extras.getString("content")
        id = intent.extras.getString("id")
        if (dimgpath != null) {
            ivPic!!.setImageURI(Uri.parse(dimgpath))
        }
        if (content != null) {
            tvRead!!.text = content
        }
        tvDate!!.text = date
        tvNo!!.text = no
        btHome!!.setOnClickListener(this)
        btNext!!.setOnClickListener(this)
        btPre!!.setOnClickListener(this)
        btModify!!.setOnClickListener(this)
        btDelete!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        super.onClick(v)
        when (v.id) {
            R.id.btHome -> {
                finish()
            }
            R.id.btPre -> {
                val diary = DiaryVO()
                diary.id = id.toString()
                diary.ddate = tvDate!!.text.toString()
                val flag = "P"
                changeDiaryData(diary, flag)
            }
            R.id.btNext -> {
                val diary = DiaryVO()
                diary.id = id.toString()
                diary.ddate = tvDate!!.text.toString()
                val flag = "N"
                changeDiaryData(diary, flag)
            }
            R.id.btModify -> {
                val diary = DiaryVO()
                diary.id = id.toString()
                diary.no = tvNo!!.text.toString()
                modifyDiaryData(diary)
            }
            R.id.btDelete -> {
                val diary = DiaryVO()
                diary.no = no.toString()
                deleteDiaryData(diary)
            }
        }
    }

    private fun deleteDiaryData(diary: DiaryVO) {
        val alertDialogBuilder = AlertDialog.Builder(this@DiaryReadActivity)
        alertDialogBuilder
                .setMessage("정말 삭제하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("예") { dialog, which ->
                    try {
                        helper = dbHelper(this@DiaryReadActivity)
                        db = helper!!.writableDatabase
                    } catch (e: SQLiteException) {
                    }
                    val sb = StringBuffer()
                    sb.append("DELETE FROM DIARY WHERE dno = #no#")
                    var query = sb.toString()
                    query = query.replace("#no#", diary.no)
                    db!!.execSQL(query)
                    db!!.close()
                    finish()
                }
                .setNegativeButton("아니요") { dialog, which -> dialog.cancel() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun changeDiaryData(diary: DiaryVO, flag: String) {
        try {
            helper = dbHelper(this@DiaryReadActivity)
            db = helper!!.readableDatabase
        } catch (e: SQLiteException) {
        }
        val sb = StringBuffer()
        if (flag == "N") {
            sb.append("SELECT * FROM DIARY WHERE id = #id# AND ddate > #date# ORDER BY ddate ASC LIMIT 1")
        } else if (flag == "P") {
            sb.append("SELECT * FROM DIARY WHERE id = #id# AND ddate < #date# ORDER BY ddate DESC LIMIT 1")
        }
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
            diary.no = no
            diary.ddate = date
            diary.dimgpath = dimgpath
            diary.dcontent = content
            diary.id = id
            if (dimgpath != null) {
                ivPic!!.setImageURI(Uri.parse(dimgpath))
            }
            if (content != null) {
                tvRead!!.text = content
            }
            tvDate!!.text = date
            tvNo!!.text = no
        } else {
            Toast.makeText(applicationContext, "오늘의 일기가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
    }

    private fun modifyDiaryData(diary: DiaryVO) {
        try {
            helper = dbHelper(this@DiaryReadActivity)
            db = helper!!.readableDatabase
        } catch (e: SQLiteException) {
        }
        val sb = StringBuffer()
        sb.append("SELECT * FROM DIARY WHERE id = #id# AND dno = #no#")
        var query = sb.toString()
        query = query.replace("#id#", "'" + diary.id + "'")
        query = query.replace("#no#", diary.no)
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
            val updatIntent = Intent(this@DiaryReadActivity, DiaryWriteActivity::class.java)
            updatIntent.putExtra("no", no)
            updatIntent.putExtra("date", date)
            updatIntent.putExtra("dimgpath", dimgpath)
            updatIntent.putExtra("content", content)
            updatIntent.putExtra("id", id)
            updatIntent.putExtra("FLAG", "UPDATESIGN")
            MainpageActivity.UPDATEIS = "O"
            startActivityForResult(updatIntent, UPDATESIGN)
        }
    }

    companion object {
        const val UPDATESIGN = 3
    }
}