package com.example.hyunndy_notepad

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_detail_memo_actvity.*
import kotlinx.android.synthetic.main.content_detail_memo_actvity.*
import java.io.ByteArrayOutputStream

class DetailMemoActvity : AppCompatActivity() {

    var isModified = false
    var detailMemo = DetailMemoClass()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_memo_actvity)
        setSupportActionBar(toolbar)

        showMemo()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_memo ->
            {
                isModified = true
                editMemo()
                true
            }
            R.id.save_memo ->
            {
                completeModification()
                true
            }
            R.id.delete_memo ->
            {
                deleteMemo()
                true
            }
            else ->
            {
                super.onOptionsItemSelected(item)
            }
        }
    }
    private fun showMemo()
    {
        detailMemo = intent.getParcelableExtra<DetailMemoClass>("DetailMemo")

        var bitmap = BitmapFactory.decodeByteArray(detailMemo.imagesrc, 0, detailMemo.imagesrc?.size!!)
        bitmap = resizeBitmap(480, bitmap)

        detail_image.setImageBitmap(bitmap)
        detail_title.text = detailMemo.title
        detail_desc.text = detailMemo.desc
    }
    // 메모 삭제
    private fun deleteMemo()
    {
        var intent = Intent()
        intent.putExtra("deleteMemo", detailMemo.idx)

        setResult(RESULTCODE.DELETE_MEMO.value, intent)
        finish()
    }


    // 추가해야할것 사진 편집

    // 메모 편집( TEXTVIEW -> EDITTEXT )
    private fun editMemo()
    {
        detail_title.visibility = View.GONE
        detail_desc.visibility = View.GONE

        detail_edittitle.visibility = View.VISIBLE
        detail_editdesc.visibility = View.VISIBLE

        detail_edittitle.setText(detail_title.text)
        detail_editdesc.setText(detail_desc.text)
    }

    private fun completeModification()
    {
        completeImage()
        completeTitle()
        completeDesc()
    }

    // 이미지 수정 완료
    private fun completeImage()
    {
        val tempImage = detail_image.drawable as BitmapDrawable
        val bitmap = tempImage.bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)

        detailMemo?.imagesrc = stream.toByteArray()
    }

    // 메모 편집 완료( EDITTEXT -> TEXTVIEW )
    private fun completeTitle()
    {
        detail_title.visibility = View.VISIBLE
        detail_edittitle.visibility = View.GONE
        detail_title.text = detail_edittitle.text

        detailMemo?.title = detail_title.text.toString()
    }

    // 메모 편집 완료
    private fun completeDesc()
    {
        detail_desc.visibility = View.VISIBLE
        detail_editdesc.visibility = View.GONE

        detail_desc.text = detail_editdesc.text

        detailMemo?.desc = detail_desc.text.toString()
    }

    override fun onBackPressed() {
        if(isModified)
        {
            var intent = Intent()
            intent.putExtra("modifiedMemo", detailMemo)

            setResult(RESULTCODE.MODIFY_MEMO.value, intent)
            finish()
        }
        else
        {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        isModified = false
    }

    // 이미지가 너무 크면 튕기기때문에 이미지 리사이즈 작업이 필요.
    private fun resizeBitmap(targetWidth : Int, source: Bitmap) : Bitmap
    {
        var ratio = source.height.toDouble() / source.width.toDouble()
        var targetHeight = (targetWidth * ratio).toInt()
        var result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false)
        if(result != source)
        {
            source.recycle()
        }
        return result
    }
}
