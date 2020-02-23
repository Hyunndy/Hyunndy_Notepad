package com.example.hyunndy_notepad

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_new_memo.*
import kotlinx.android.synthetic.main.content_new_memo.*
import java.io.ByteArrayOutputStream

// 뉴 메모.
class NewMemoActivity : AppCompatActivity() {

    private var newImageByteCode = arrayListOf<ByteArray>()
    private var nimage:Int = 0
    private lateinit var imageInflater:LayoutInflater


    // **HYEONJIY** DB 추가
    var helper: NotepadDBHelper? = null
    var imagedb: SQLiteDatabase? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_memo)
        setSupportActionBar(newmemo_toolbar)

        imageInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        imageInflater.inflate(R.layout.content_new_memo, linear_image, false)

        // **HYEONJIY** DB 추가
        helper = NotepadDBHelper(this)
        imagedb = helper?.writableDatabase

        // ~앨범 추가칸~
        addImageBtn.setOnClickListener {
            // 1. AlertDialogue
            val builder = AlertDialog.Builder(this)

            builder.setTitle("이미지를 무엇으로 추가하시겠습니까?").setItems(
                arrayOf("GALLERY", "CAMERA", "URL"),
                DialogInterface.OnClickListener { _, which ->
                    when(which)
                    {
                        0 ->
                        {
                            getPicturefromGallery()
                        }
                        1 ->
                        {

                        }
                        2 ->
                        {

                        }
                    }
                })
            builder.create()
            builder.show()
        }
    }

    private fun selectImageView(bitmap: Bitmap)
    {
        // 이 때는 썸네일
        if(nimage  == 0 ) {
            newImage.setImageBitmap(bitmap)
        }
        else
        {
            val addedImageView = ImageView(this)
            addedImageView.setImageBitmap(bitmap)

            linear_image.addView(addedImageView)
        }
    }

    private fun getPicturefromGallery()
    {
        var intent = Intent(Intent.ACTION_PICK)
        intent.type = android.provider.MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, REQUESTCODE.OPEN_GALLERY.value)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 앨범선택칸에서 다시 돌아왔을 때.
        if(requestCode == REQUESTCODE.OPEN_GALLERY.value)
        {
            if(data == null)
            {
                return
            }

            var c = contentResolver.query(data.data!!, null, null, null, null)

            c?.moveToNext()

            var index = c?.getColumnIndex(MediaStore.Images.Media.DATA)
            var source = c?.getString(index!!)
            var bitmap = BitmapFactory.decodeFile(source)
            val stream = ByteArrayOutputStream()

            bitmap = resizeBitmap(480, bitmap)

            selectImageView(bitmap)

            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)

            newImageByteCode.add(nimage, stream.toByteArray())
            nimage++
        }
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

    // **HYEONJIY** 1. db에 테이블 새로 추가해서 2. n개의 이미지 로딩하기. 3. 안되면 이 주석이 달린걸 삭제하세용
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save_memo_new -> {
                if(newtitle.text.isNotEmpty())
                {
                    var newMemo = DetailMemoClass()

                    newMemo.title = newtitle.text.toString()
                    newMemo.desc = newdesc.text.toString()

                    if(nimage > 0)
                    {
                        newMemo.thumbnailsrc = newImageByteCode[0]

                        // **HYEONJIY**
                        addImagetoDB(newMemo)
                    }
                    else
                    {
                        newMemo.thumbnailsrc = null
                    }

                    var intent = Intent()
                    intent.putExtra("newMemo", newMemo)
                    setResult(REQUESTCODE.NEW_MEMO.value, intent)

                    Toast.makeText(applicationContext, "메모가 저장되었습니다.", Toast.LENGTH_LONG).show();
                }
                true
            }
            else ->
            {
                super.onOptionsItemSelected(item)
            }
        }
    }

    // **HYEONJIY**  마지막에 나갈 때 imagelist DB에 1.메모 타이틀, 2. BLOB, 3. 인덱스를 추가한다.
    private fun addImagetoDB(newMemo : DetailMemoClass)
    {
        var contentValues = ContentValues()

        // 2. 이미지, 인덱스 등록
        for((idx, image) in newImageByteCode.withIndex())
        {
            // 1. title 등록.
            contentValues.put("title", newMemo.title)

            // 2. 이미지 등록
            contentValues.put("image", image)

            // 3. 인덱스 등록
            contentValues.put("imageIdx", idx)

            // 4. db에 넣자! 그리고 이걸 detailmemo에서 꺼내쓰면 된다.
            imagedb?.insert("imagelist", null, contentValues)

            Log.d("test2", "뉴 메모에서 이미지가 추가됩니다.")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_new, menu)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()

        imagedb?.close()
    }
}
