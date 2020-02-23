package com.example.hyunndy_notepad

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// SQLite를 사용하기 위한 DBHelper.

// DB구조 이미지주소

class NotepadDBHelper(context : Context) : SQLiteOpenHelper(context, "Notepad.db", null, 1)
{
    override fun onCreate(db: SQLiteDatabase?) {
        val query = "create table memolist(" +
                "title text primary key," +
                "image BLOB," +
                "description text" +
                ");"
        db?.execSQL(query)

        val query2 = "create table imagelist(" +
                "title text not null," +
                "image BLOB," +
                "imageIdx integer" +
                ");"
        db?.execSQL(query2)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}