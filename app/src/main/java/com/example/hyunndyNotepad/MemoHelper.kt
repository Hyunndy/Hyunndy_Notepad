package com.example.hyunndyNotepad

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import kotlinx.android.synthetic.main.content_new_memo.*
import java.io.ByteArrayOutputStream

/* --------------------------------------------------------------------------------------------------
작성자: HYEONJIYOO
작성일: 2020.02.24
클래스명: MemoHelper
클래스기능:
1. 메모 작업 관련(Bitmap 세팅 등) 중복되는 함수가 많아 관련 함수를 이 Class에 모아 코드 중복을 개선한다.
-------------------------------------------------------------------------------------------------- */

class MemoHelper {
    // 이미지 추가 시 캐싱하고 액티비티에 넘겨주는 변수.
    private var cachedImageByteCode:ByteArray? = null

    //{{ @HYEONJIY: 이미지 추가 시 갤러리에서 가져온 사진을 Bitmap으로 변환
    fun setImagefromGallery(context : Context, data: Intent?): Bitmap? {
        var cursor:Cursor? = context.contentResolver.query(data?.data!!, null, null, null, null)
            ?: return null

        cursor?.moveToNext()

        var bitmapIdx = cursor?.getColumnIndex(MediaStore.Images.Media.DATA)!!
        var bitmapSource = cursor?.getString(bitmapIdx)

        var bitmapOption = BitmapFactory.Options()
        bitmapOption.inSampleSize = 1

        var bitmap:Bitmap? = BitmapFactory.decodeFile(bitmapSource, bitmapOption)
        if(bitmap != null)
        {
            var stream = ByteArrayOutputStream()

            bitmap = resizeBitmap(480, bitmap)
            bitmap?.compress(Bitmap.CompressFormat.PNG, 90, stream)
            cachedImageByteCode = stream.toByteArray()
        }
        else {
            cachedImageByteCode = null
        }

        return bitmap
    }
    //}} @HYEONJIY

    //{{ @HYEONJIY: 이미지 추가 시 카메라로 사진을 찍은 사진을 Bitmap으로 변환
    fun setImageFromCamera(context : Context, data: Intent?): Bitmap? {
        var bitmap = data?.getParcelableExtra<Bitmap>("data")
        bitmap = resizeBitmap(480, bitmap!!)
        if(bitmap != null) {
            var stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
            cachedImageByteCode = stream.toByteArray()
        }
        else {
            cachedImageByteCode = null
        }

        return bitmap
    }
    //}} @HYEONJIY

    //{{ @HYEONJIY: Bitmap 크기 조정.
    fun resizeBitmap(targetWidth: Int, source: Bitmap, isURL:Boolean=false): Bitmap? {
        var ratio = source.height.toDouble() / source.width.toDouble()
        var targetHeight = (targetWidth * ratio).toInt()

        // @BugFix: targetWidth와 Width가 같으나 Height이 매우 큰 이미지는 crash 발생.
        if(targetHeight == source.height) {
            targetHeight/=2
        }

        var result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false)
        // @BugFix: URL로 가져온 이미지의 경우 recycle() 함수에서 NullPointer crasg 발생.
        if (result != source && !isURL) {
            source.recycle()
        }

        return result
    }
    //}} @HYEONJIY

    //{{ @HYEONJIY: 이미지 추가 시 캐싱해 놓은 비트맵 바이트코드 getter
    fun getImageByteCode(): ByteArray?{
        return if(cachedImageByteCode == null){
            null
        } else{
            cachedImageByteCode
        }
    }
    //}} @HYEONJIY
}