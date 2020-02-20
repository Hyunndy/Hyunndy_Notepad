package com.example.hyunndy_notepad

import android.os.Parcel
import android.os.Parcelable

// Parcelable 인터페이스 구현
class DetailMemoClass : Parcelable{

    var imagesrc:Int? = 0
    var title:String? = null
    var desc:String? = null

    companion object
    {
        @JvmField

        val CREATOR:Parcelable.Creator<DetailMemoClass> = object : Parcelable.Creator<DetailMemoClass>
        {
            // 객체 복원 메서드
            override fun createFromParcel(source: Parcel?): DetailMemoClass {

                val memo = DetailMemoClass()

                memo.imagesrc = source?.readInt()!!
                memo.title = source?.readString()
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

        //데이터를 넣자.
        //이거 0 값이 들어가면 우짜노?
        dest?.writeInt(imagesrc!!)
        dest?.writeString(title)
        dest?.writeString(desc)
    }

    override fun describeContents(): Int {
        return 0
    }
}