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
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_new_memo.*
import kotlinx.android.synthetic.main.content_new_memo.*
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


        // 버튼 누르면 Title에있는거, 설명에 있는거 묶어서 DB에 저장해야됨.
        // MainActivity로 돌아가면 DB 다시 로드? 있는건 있는대로 하면좋겠는데 다시 로드하지말고..
        fab.setOnClickListener { view ->
            Snackbar.make(view, "저장버튼", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()

            if(newImageByteCode?.isNotEmpty()!!)
            {
                // 일단 해놓고 나중에 전달해보자 ㅎㅎㅎ
                //DB에 저장.
                var values = ContentValues().apply {
                    put("image",newImageByteCode)
                    put("title", newTitle.text.toString())
                    put("description", newDesc.text.toString())
                }

                var memodb:SQLiteDatabase = openOrCreateDatabase("Notepad.db", Context.MODE_PRIVATE, null)
                var newRowId = memodb?.insert("memolist", null, values)
            }
        }

        button.setOnClickListener { view ->
            var intent = Intent(Intent.ACTION_PICK)
            intent.type = android.provider.MediaStore.Images.Media.CONTENT_TYPE
            startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK)
        {
            var c = contentResolver.query(data?.data!!, null, null, null, null)
            c?.moveToNext()

            var index = c?.getColumnIndex(MediaStore.Images.Media.DATA)
            var source = c?.getString(index!!)

            var bitmap = BitmapFactory.decodeFile(source)

            newImage.setImageBitmap(bitmap)

            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
            newImageByteCode = stream.toByteArray()
        }
    }

}
