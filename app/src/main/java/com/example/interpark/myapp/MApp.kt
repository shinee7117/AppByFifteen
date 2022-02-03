package com.example.interpark.myapp

import android.app.Application
import android.content.Context

class MApp : Application() {
    var currentContext: Context? = null
    var userId: String? = null
    var userName: String? = null
    var userPhone: String? = null

}