package com.example.hyunndy_notepad

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_detail_memo_actvity.*
import kotlinx.android.synthetic.main.content_detail_memo_actvity.*
import kotlinx.android.synthetic.main.content_new_memo.*
import java.io.ByteArrayOutputStream

class DetailMemoActvity : AppCompatActivity() {

    var isModified = false
    private lateinit var detailMemo:DetailMemoClass

    //** HYEONJIY ** DB를 읽어서 IMAGEVIEW에 뿌려야한다. 그리고 그게 스크롤이되어야한다.
    private lateinit var imageInflater:LayoutInflater

    var helper: NotepadDBHelper? = null
    var imagedb: SQLiteDatabase? = null

    private var newImageByteCode = arrayListOf<ByteArray>()
    private var nimage:Int = 0
    //**


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_memo_actvity)
        setSupportActionBar(toolbar)

        //**HYEONJIY** 스크롤되는 이미지뷰를 위한 Inflater
        imageInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        imageInflater.inflate(R.layout.content_detail_memo_actvity, linear_image_detail, false)


        //**HYEONJIY** DB추가
        helper = NotepadDBHelper(this)
        imagedb = helper?.writableDatabase

        // **HYEONJIY** DB를 읽어서 BLOB를 비트맵으로 전환해서 IMAGEVIEW추가되는거에 넣어야한다.
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
            R.id.save_memo_detail ->
            {
                completeModification()
                true
            }
            R.id.delete_memo_detail ->
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

    //** HYEONJIY** 여기서 imagelist DB를 읽어서 image를 읽어야한다.
    // 그러려면 얘도 ImageView를 추가해줘야겠지? ㅆㅄㅂㅆㅃ
    private fun showMemo()
    {
        detailMemo = DetailMemoClass()
        detailMemo = intent.getParcelableExtra<DetailMemoClass>("DetailMemo")!!

        detail_title.text = detailMemo.title
        detail_desc.text = detailMemo.desc

        // **HYEONJIY**
       if(detailMemo.thumbnailsrc != null)
       {
           var bitmap = BitmapFactory.decodeByteArray(detailMemo.thumbnailsrc, 0, detailMemo.thumbnailsrc?.size!!)
           bitmap = resizeBitmap(480, bitmap)

           // **HYEONJIY** 메인으로부터받은 썸네일은 그냥 여기다가 둔다.
           detail_image.setImageBitmap(bitmap)

           //**HYEONJIY** 썸네일을 선택했으면 이제 DB를 읽어서 이미지뷰를 추가한다.
           readDB()
           addImageView()
       }
    }

    // **HYEONJIY** DB읽기
    private fun readDB()
    {
        var title = (detail_title.text.toString())
        var c: Cursor? = imagedb?.rawQuery("select * from imagelist where title =?", arrayOf(title))
        var imageCount = c?.count

         while(c?.moveToNext()!!)
         {
             var imag_pos = c.getColumnIndex("image")
             var idx_pos = c.getColumnIndex("imageIdx")

             var imageData = c.getBlob(imag_pos)
             var idxData = c.getInt(idx_pos)

             newImageByteCode.add(idxData, imageData)
         }
    }

    // **HYEONJIY** 썸네일 빼고 VIEW숫자만큼.
    private fun addImageView()
    {
        for((idx, image) in newImageByteCode.withIndex())
        {
            if(idx==0)
            {
                continue
            }

            var addedImageView = ImageView(this)
            var Bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)

            if(Bitmap!= null)
            {
                Bitmap = resizeBitmap(480, Bitmap) // 이미지 조절 추가
                addedImageView.setImageBitmap(Bitmap)

                linear_image_detail.addView(addedImageView)
            }
            Log.d("test2", "디테일에서 이미지가 추가됩니다.")
        }
    }

    // 메모 삭제
    private fun deleteMemo()
    {
        var intent = Intent()
        intent.putExtra("deleteMemo", detailMemo.title)

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

        detailMemo.thumbnailsrc = stream.toByteArray()
    }

    // 메모 편집 완료( EDITTEXT -> TEXTVIEW )
    private fun completeTitle()
    {
        detail_title.visibility = View.VISIBLE
        detail_edittitle.visibility = View.GONE
        detail_title.text = detail_edittitle.text

        detailMemo.title = detail_title.text.toString()
    }

    // 메모 편집 완료
    private fun completeDesc()
    {
        detail_desc.visibility = View.VISIBLE
        detail_editdesc.visibility = View.GONE

        detail_desc.text = detail_editdesc.text

        detailMemo.desc = detail_desc.text.toString()
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
        imagedb?.close()
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