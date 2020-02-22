package com.example.hyunndy_notepad

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_new_memo.*
import kotlinx.android.synthetic.main.content_new_memo.*
import org.w3c.dom.Text
import java.io.ByteArrayOutputStream

// 뉴 메모.
class NewMemoActivity : AppCompatActivity() {

    var newImageByteCode:ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_memo)
        setSupportActionBar(newmemo_toolbar)

        var newImage:ImageView = findViewById(R.id.newImage)
        var newTitle:EditText = findViewById(R.id.newtitle)
        var newDesc:EditText = findViewById(R.id.newdesc)


        // 이미지 추가칸
        fab.setOnClickListener { view ->
            Snackbar.make(view, "저장버튼", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()

            if(newImageByteCode != null)
            {
                var newMemo = DetailMemoClass()
                newMemo.imagesrc = newImageByteCode
                newMemo.title = newTitle.text.toString()
                newMemo.desc = newTitle.text.toString()

                var intent = Intent()
                intent.putExtra("newMemo", newMemo)
                setResult(Activity.RESULT_OK, intent)
            }
        }

        // 앨범 추가칸.
        button.setOnClickListener { view ->
            var intent = Intent(Intent.ACTION_PICK)
            intent.type = android.provider.MediaStore.Images.Media.CONTENT_TYPE
            startActivityForResult(intent, REQUESTCODE.OPEN_GALLERY.value)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 앨범선택칸에서 다시 돌아왔을 때.
        if(requestCode == REQUESTCODE.OPEN_GALLERY.value)
        {
            var c = contentResolver.query(data?.data!!, null, null, null, null)
            c?.moveToNext()

            var index = c?.getColumnIndex(MediaStore.Images.Media.DATA)
            var source = c?.getString(index!!)

            var bitmap = BitmapFactory.decodeFile(source)
            bitmap = resizeBitmap(480, bitmap)

            newImage.setImageBitmap(bitmap)

            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
            newImageByteCode = stream.toByteArray()
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

}
