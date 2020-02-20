package com.example.hyunndy_notepad

import android.graphics.drawable.Drawable


// 아이템 데이터 클래스 정의
// 1개의 이미지 뷰, 두 개의 텍스트뷰.
public class MemoItem {

    private var MemoIcon:Int = 0
    private var MemoTitle:String = ""
    private var MemoDesc:String = ""

    public fun setIcon(IconPath:Int)
    {
        MemoIcon = IconPath
    }

    public fun setTitle(title:String)
    {
        MemoTitle = title
    }

    public  fun setDesc(desc:String)
    {
        MemoDesc = desc
    }

    public fun getIcon() : Int{
        return MemoIcon
    }

    public fun getTitle() : String{
        return MemoTitle
    }

    public  fun getDesc() : String{
        return MemoDesc
    }
}