package com.example.hyunndyNotepad

import android.os.Parcel
import android.os.Parcelable

// Parcelable 인터페이스 구현
class DetailMemoClass : Parcelable{

    //썸네일만
    var thumbnailsrc:ByteArray? = null
    var title:String = ""!!
    var desc:String? = null

    companion object
    {
        @JvmField

        val CREATOR:Parcelable.Creator<DetailMemoClass> = object : Parcelable.Creator<DetailMemoClass>
        {
            // 객체 복원 메서드
            override fun createFromParcel(source: Parcel?): DetailMemoClass {

                val memo = DetailMemoClass()

                memo.thumbnailsrc = source?.createByteArray()
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

        dest?.writeByteArray(thumbnailsrc)
        dest?.writeString(title)
        dest?.writeString(desc)
    }

    override fun describeContents(): Int {
        return 0
    }
}