package com.example.hyunndy_notepad

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_detail_memo_actvity.*
import kotlinx.android.synthetic.main.content_detail_memo_actvity.*
import java.io.ByteArrayOutputStream

class DetailMemoActvity : AppCompatActivity() {

    var isModified = false
    var modifiedmemo:DetailMemoClass? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_memo_actvity)
        setSupportActionBar(toolbar)

        // 메모 보여줌
        showMemo()

       //detail_editdesc.setOnEditorActionListener { v, actionId, event ->
       //    completeDesc()
       //}

    }

    // 상세 내용 편집/삭제/저장
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
            }
            R.id.complete_memo ->
            {
                completeModification()
            }
            else ->
            {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun showMemo()
    {
        //{{ 4. 20200221 hyeonjiy : 전달받은 텍스트 내용 출력하기
        var detail_memo = intent.getParcelableExtra<DetailMemoClass>("DetailMemo")

        var image = BitmapFactory.decodeByteArray(detail_memo.imagesrc, 0, detail_memo.imagesrc?.size!!)


        detail_image.setImageBitmap(image)
        detail_title.text = detail_memo.title
        detail_desc.text = detail_memo.desc
        //}}

        modifiedmemo = DetailMemoClass()
        modifiedmemo?.idx = detail_memo.idx
    }

    // 사진 편집

    // 메모 편집( TEXTVIEW -> EDITTEXT )
    private fun editMemo() : Boolean
    {
        detail_title.visibility = View.GONE
        detail_desc.visibility = View.GONE
        detail_edittitle.visibility = View.VISIBLE
        detail_editdesc.visibility = View.VISIBLE

        detail_edittitle.setText(detail_title.text)
        detail_editdesc.setText(detail_desc.text)

        return true
    }

    private fun completeModification() : Boolean
    {

        completeImage()
        completeTitle()
        completeDesc()

        return true
    }

    // 이미지 수정 완료
    private fun completeImage()
    {
        val tempImage = detail_image.drawable as BitmapDrawable
        val bitmap = tempImage.bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)

        modifiedmemo?.imagesrc = stream.toByteArray()

       // var compressThread = ThreadClass()
       // compressThread.start()
    }

    // 메모 편집 완료( EDITTEXT -> TEXTVIEW )
    private fun completeTitle()
    {
        detail_title.visibility = View.VISIBLE
        detail_edittitle.visibility = View.GONE

        detail_title.text = detail_edittitle.text

        modifiedmemo?.title = detail_title.text.toString()
    }

    // 메모 편집 완료
    private  fun completeDesc()
    {
        detail_desc.visibility = View.VISIBLE
        detail_editdesc.visibility = View.GONE

        detail_desc.text = detail_editdesc.text

        modifiedmemo?.desc = detail_desc.text.toString()
    }


    // 뒤로가기 해서 액티비티 전환 시 Main에 Intent전달.
    override fun onBackPressed() {

        if(isModified)
        {
            var intent = Intent()
            intent.putExtra("modifiedMemo", modifiedmemo)

            setResult(Activity.RESULT_OK, intent)
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
}
