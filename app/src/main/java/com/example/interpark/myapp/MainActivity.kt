package com.example.interpark.myapp

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.TextView.OnEditorActionListener

class MainActivity : BaseActivity(), OnEditorActionListener {
    var etId: EditText? = null
    var etPw: EditText? = null
    var tvBtLogin: TextView? = null
    var tvBtChangePw: TextView? = null
    var tvBtJoin: TextView? = null
    var ckSaveId: CheckBox? = null
    var ivPic: ImageView? = null
    var saveId: String? = ""
    var helper: dbHelper? = null
    var db: SQLiteDatabase? = null
    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val textView = findViewById(R.id.icHeader) as View
        val title = textView.findViewById<View>(R.id.tvHeader) as TextView
        val ibMenu = textView.findViewById<View>(R.id.ibMenu) as ImageButton
        val ibLogout = textView.findViewById<View>(R.id.ibLogout) as ImageButton
        ibMenu.visibility = View.INVISIBLE
        ibLogout.visibility = View.INVISIBLE
        title.text = "로그인"
        etId = findViewById<View>(R.id.etId) as EditText
        etPw = findViewById<View>(R.id.etPw) as EditText
        tvBtLogin = findViewById<View>(R.id.tvBtLogin) as TextView
        tvBtChangePw = findViewById<View>(R.id.tvBtChangePw) as TextView
        ckSaveId = findViewById<View>(R.id.ckSaveId) as CheckBox
        tvBtJoin = findViewById<View>(R.id.tvBtJoin) as TextView
        val PTM = PasswordTransformationMethod()
        etPw!!.transformationMethod = PTM
        val auto = getSharedPreferences("auto", MODE_PRIVATE)
        saveId = auto.getString("inputId", null)
        etPw!!.setOnEditorActionListener(this)
        if (saveId != null) {
            etId!!.setText(saveId)
        }
        tvBtLogin!!.setOnClickListener(View.OnClickListener {
            try {
                helper = dbHelper(this@MainActivity)
                db = helper!!.readableDatabase
            } catch (e: SQLiteException) {
            }
            val id = etId!!.text.toString()
            val pw = etPw!!.text.toString()
            val userInfo = UserVO()
            userInfo.id = id
            userInfo.passwd = pw
            val sb = StringBuffer()
            sb.append("SELECT * FROM user WHERE id = #id# AND passwd = #passwd#")
            var query = sb.toString()
            query = query.replace("#id#", "'" + userInfo.id + "'")
            query = query.replace("#passwd#", "'" + userInfo.passwd + "'")

            //
            val cursor: Cursor
            cursor = db!!.rawQuery(query, null)
            if (cursor.moveToNext()) {
                val strId = cursor.getString(0)
                val strName = cursor.getString(1)
                val strPhone = cursor.getString(3)
                val appDel = application as MApp
                appDel.userId = strId
                appDel.userName = strName
                appDel.userPhone = strPhone
                if (strId != null) {
                    if (ckSaveId!!.isChecked == true) {
                        val auto = getSharedPreferences("auto", MODE_PRIVATE)
                        val autoLogin = auto.edit()
                        autoLogin.putString("inputId", strId)
                        autoLogin.commit()
                        startMain(strId, strName, strPhone)
                    } else {
                        val auto = getSharedPreferences("auto", MODE_PRIVATE)
                        val autoLogin = auto.edit()
                        autoLogin.putString("inputId", null)
                        autoLogin.commit()
                        Toast.makeText(applicationContext, "로그인 완료", Toast.LENGTH_SHORT).show()
                        startMain(strId, strName, strPhone)
                    }
                }
            } else {
                Toast.makeText(applicationContext, "입력정보를 확인하세요!", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            cursor.close()
            db!!.close()
        })
        tvBtJoin!!.setOnClickListener {
            val intent = Intent(this@MainActivity, LoginPopupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun startMain(id: String, name: String, phone: String) {
        val intent = Intent(this@MainActivity, MainpageActivity::class.java)
        intent.putExtra("id", id)
        intent.putExtra("name", name)
        intent.putExtra("phone", phone)
        startActivity(intent)
    }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE || event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
            val nextFocus = v.focusSearch(View.FOCUS_DOWN)
            if (nextFocus != null && nextFocus is EditText) {
                nextFocus.requestFocus()
            } else {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                (findViewById<View>(R.id.tvBtLogin) as TextView).performClick()
            }
            return true
        }
        return false
    }
}