package com.example.hyunndyNotepad

import java.util.ArrayList

/* --------------------------------------------------------------------------------------------------
작성자: HYEONJIYOO
작성일: 2020.02.24
클래스명: MemoItem
클래스기능:
1. 리스트뷰 하나의 항목을 구성하는 아이템 데이터 정의 클래스.
2. 리스트뷰 항목 하나에 제목/본문/이미지가 있다.
-------------------------------------------------------------------------------------------------- */

class MemoItem {

    private var memoTitle:String = ""
    private var memoIcon = arrayListOf<ByteArray>()
    private var memoDesc:String? = ""

    fun setThumbnail(IconPath: ByteArray?) {
        if(IconPath != null) {
            memoIcon.add(0, IconPath)
        }
    }

    fun setImageList(images: ArrayList<ByteArray>) {
        for((idx, image) in images.withIndex()) {
            memoIcon.add(idx, image)
        }
    }

    fun setTitle(title: String) {
        memoTitle = title
    }

    fun setDesc(desc: String?) {
        memoDesc = desc
    }

    fun getThumbnail(): ByteArray? {
        if(memoIcon.isNullOrEmpty()) {
            return null
        }
        else {
            return memoIcon[0]
        }
    }

    fun getTitle(): String {
        return memoTitle
    }

    fun getDesc(): String? {
        return memoDesc
    }
}