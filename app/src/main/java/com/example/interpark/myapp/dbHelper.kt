package com.example.interpark.myapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class dbHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE user (id TEXT PRIMARY KEY, name TEXT, passwd TEXT, phone TEXT);")
        db.execSQL("""CREATE TABLE diary (
  dno     INTEGER PRIMARY KEY AUTOINCREMENT, 
  ddate   TEXT, 
  dtitle TEXT,
  dimgpath TEXT,
  dcontent text, 
  id TEXT,
  FOREIGN KEY(id) REFERENCES user(id)
);""")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    companion object {
        private const val DATABASE_NAME = "myDiary.db"
        private const val DATABASE_VERSION = 1
    }
}