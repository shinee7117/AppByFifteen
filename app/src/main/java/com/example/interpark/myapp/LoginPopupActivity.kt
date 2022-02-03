package com.example.interpark.myapp

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class LoginPopupActivity : BaseActivity(), View.OnClickListener {
    var etName: EditText? = null
    var etId: EditText? = null
    var etPw: EditText? = null
    var etCkeckPw: EditText? = null
    var etPhone: EditText? = null
    var btnSave: Button? = null
    var btnCancel: Button? = null
    var helper: dbHelper? = null
    var db: SQLiteDatabase? = null
    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        layoutParams.dimAmount = 0.5f
        window.attributes = layoutParams
        setContentView(R.layout.join_popup)
        val dp = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val width = (dp.width * 0.9).toInt()
        val height = (dp.height * 0.7).toInt()
        window.attributes.width = width
        window.attributes.height = height
        setFinishOnTouchOutside(false)
        etName = findViewById<View>(R.id.etName) as EditText
        etId = findViewById<View>(R.id.etId) as EditText
        etPw = findViewById<View>(R.id.etPw) as EditText
        etCkeckPw = findViewById<View>(R.id.etCheckPw) as EditText
        etPhone = findViewById<View>(R.id.etPhone) as EditText
        btnSave = findViewById<View>(R.id.btnSave) as Button
        btnCancel = findViewById<View>(R.id.btnCancel) as Button
        helper = dbHelper(this)
        db = try {
            helper!!.writableDatabase
        } catch (e: SQLiteException) {
            helper!!.readableDatabase
        }
        btnSave!!.setOnClickListener(this)
        btnCancel!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnSave -> {
                val userInfo = UserVO()
                val name = etName!!.text.toString()
                val id = etId!!.text.toString()
                val pw = etPw!!.text.toString()
                val checkPw = etCkeckPw!!.text.toString()
                val phone = etPhone!!.text.toString()
                if (name == "" || id == "" || pw == "" || checkPw == "" || phone == "") {
                    Toast.makeText(applicationContext, "내용을 입력하세요.", Toast.LENGTH_SHORT).show()
                    return
                }
                userInfo.name = name
                userInfo.id = id
                userInfo.passwd = pw
                userInfo.phone = phone
                if (pw == checkPw) {
                    Toast.makeText(this, "비밀번호가 일치합니다.", Toast.LENGTH_SHORT).show()
                    val sb = StringBuffer()
                    sb.append("INSERT INTO user (")
                    sb.append(" id, name, passwd, phone )")
                    sb.append(" VALUES (?, ?, ?, ?)")
                    db!!.execSQL(sb.toString(), arrayOf<Any>(userInfo.id, userInfo.name, userInfo.passwd, userInfo.phone))
                    db!!.close()
                    finish()
                } else {
                    finish()
                }
            }
            R.id.btnCancel -> {
                val intent = Intent(this@LoginPopupActivity, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }
}