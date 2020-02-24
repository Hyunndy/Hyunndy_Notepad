package com.example.hyunndyNotepad

import android.os.Parcel
import android.os.Parcelable

/* --------------------------------------------------------------------------------------------------
작성자: HYEONJIYOO
작성일: 2020.02.24
클래스명: DetailMemoClass
클래스기능:
1. 각 Activity 사이 객체 전달을 위한 Parcelable 인터페이스 구현 클래스.
-------------------------------------------------------------------------------------------------- */
class DetailMemoClass : Parcelable {

    var thumbnailSrc:ByteArray? = null
    var title:String = ""!!
    var desc:String? = null

    companion object
    {
        @JvmField

        val CREATOR:Parcelable.Creator<DetailMemoClass> = object : Parcelable.Creator<DetailMemoClass> {
            override fun createFromParcel(source: Parcel?): DetailMemoClass {

                var memo = DetailMemoClass()

                memo.thumbnailSrc = source?.createByteArray()
                memo.title = source?.readString()!!
                memo.desc = source?.readString()

                return memo
            }

            override fun newArray(size: Int): Array<DetailMemoClass?> {
                return arrayOfNulls<DetailMemoClass>(size)
            }
        }
    }

    // 데이터쓰는것
    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeByteArray(thumbnailSrc)
        dest?.writeString(title)
        dest?.writeString(desc)
    }

    override fun describeContents(): Int {
        return 0
    }
}