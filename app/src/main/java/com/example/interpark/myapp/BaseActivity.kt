package com.example.interpark.myapp

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.view.View
import android.widget.Toast

open class BaseActivity : Activity(), View.OnClickListener {
    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ibMenu -> Toast.makeText(applicationContext, "메뉴버튼", Toast.LENGTH_SHORT).show()
            R.id.ibLogout -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    companion object {
        private val typeface: Typeface? = null
    }
}