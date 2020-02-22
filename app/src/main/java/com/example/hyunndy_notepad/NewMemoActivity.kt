package com.example.hyunndy_notepad

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import com.google.android.material.snackbar.Snackbar

import kotlinx.android.synthetic.main.activity_new_memo.*
import kotlinx.android.synthetic.main.content_new_memo.*
import java.io.ByteArrayOutputStream

// 뉴 메모.
class NewMemoActivity : AppCompatActivity() {

    private lateinit var thumbnail:ImageView
    private lateinit var newTitle:EditText
    private lateinit var newDesc:EditText
    private var newImageByteCode = arrayListOf<ByteArray>()
    private var imageIdx:Int = 0
    private lateinit var imageInflater:LayoutInflater
    private lateinit var imagelinear:LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_memo)
        setSupportActionBar(newmemo_toolbar)

        thumbnail = findViewById(R.id.newImage)
        newTitle = findViewById(R.id.newtitle)
        newDesc  = findViewById(R.id.newdesc)

        imageInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        imagelinear = findViewById(R.id.linear_image)
        imageInflater.inflate(R.layout.content_new_memo, imagelinear, false)

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
        if(imageIdx  == 0 ) {
            thumbnail.setImageBitmap(bitmap)
        }
        else
        {
            val addedImageView = ImageView(this)
            addedImageView.setImageBitmap(bitmap)

            imagelinear.addView(addedImageView)
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
            // 음??!?!
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

            newImageByteCode.add(imageIdx, stream.toByteArray())
            imageIdx++
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_memo -> {
                if(newtitle.text.isNotEmpty())
                {
                    var newMemo = DetailMemoClass()
                    if(imageIdx > 0)
                    {
                        newMemo.imagesrc = newImageByteCode[0]
                    }
                    else
                    {
                        newMemo.imagesrc = null
                    }

                    newMemo.title = newTitle.text.toString()
                    newMemo.desc = newTitle.text.toString()

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_new, menu)
        return true
    }

}
