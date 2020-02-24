package com.example.hyunndyNotepad

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/* --------------------------------------------------------------------------------------------------
작성자: HYEONJIYOO
작성일: 2020.02.24
클래스명: NotepadDBHelper
클래스기능:
1. 내부 DB를 사용하기 위한 DBHelper.
2. MainActivity의 리스트뷰를 위한 memolist
3. NewMemoActivity, DetailMemoActivity 에서의 n개의 이미지 출력을 위한 imagelist
-------------------------------------------------------------------------------------------------- */
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