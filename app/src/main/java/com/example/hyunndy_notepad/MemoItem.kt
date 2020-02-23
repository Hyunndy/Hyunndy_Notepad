package com.example.hyunndy_notepad

import android.graphics.drawable.Drawable
import java.util.ArrayList


// 아이템 데이터 클래스 정의
// 1개의 이미지 뷰, 두 개의 텍스트뷰.
public class MemoItem {

    private var MemoTitle:String = ""
    // private var MemoIcon:ByteArray? = NULL
    private var MemoIcon = arrayListOf<ByteArray>()
    private var MemoDesc:String? = ""

    // **HYEONJIY** 썸네일 얻어오기, 아이콘 얻어오기 따로 해야함
    fun setThumbnail(IconPath:ByteArray?)
    {
        if(IconPath != null)
        {
            MemoIcon.add(0, IconPath)
        }
    }

    // **HYEONHIY** ImageList에 insert해주기 위해 인덱스 마다 image삽입.
    fun setImageList(images : ArrayList<ByteArray>)
    {
        for((idx, image) in images.withIndex())
        {
            MemoIcon.add(idx, image)
        }
    }

    fun setTitle(title:String)
    {
        MemoTitle = title
    }

    fun setDesc(desc:String?)
    {
        MemoDesc = desc
    }

    //**HYEONJIY**
    fun getThumbnail() : ByteArray?{
        return MemoIcon[0]
    }

    //**HYEONJIY**
    fun getImageList() : ArrayList<ByteArray>
    {
        return MemoIcon
    }

    fun getTitle() : String{
        return MemoTitle
    }

    fun getDesc() : String?{
        return MemoDesc
    }
}