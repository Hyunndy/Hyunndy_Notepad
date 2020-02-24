package com.example.hyunndyNotepad

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

import kotlinx.android.synthetic.main.activity_new_memo.*
import kotlinx.android.synthetic.main.content_new_memo.*
import java.io.ByteArrayOutputStream

import com.example.hyunndyNotepad.MemoHelper

/* --------------------------------------------------------------------------------------------------
작성자: HYEONJIYOO
작성일: 2020.02.24
클래스명: NewMemoActivity
클래스기능: 
1. 메모 추가를 담당하는 Activity
2. MainActivity의 +(이미지 추가)버튼을 누르면 생성된다.
3. 제목/본문/이미지 추가/수정 가능.
4. 이미지는 N개 업로드 가능
5. save 버튼을 누르면 MainActivity에 보낼 intent를 세팅하고, n개의 이미지리스트를 담는 imagelist DB를 갱신한다.
-------------------------------------------------------------------------------------------------- */

class NewMemoActivity : AppCompatActivity() {

    //{{ 이미지
    private var newImageByteCode = arrayListOf<ByteArray>()
    private var numImages: Int = 0
    private lateinit var imageInflater: LayoutInflater
    private var imageURL:String? = null
    //}}

    // DB
    private var helper: NotepadDBHelper? = null
    private var imagedb: SQLiteDatabase? = null

    // memo 관련 동작(Bitmap 변환 등)등을 모아둔 MemoHelper 클래스 객체
    private var memoHelper = MemoHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_memo)
        setSupportActionBar(newmemo_toolbar)

        imageInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        imageInflater.inflate(R.layout.content_new_memo, linear_image, false)

        helper = NotepadDBHelper(this)
        imagedb = helper?.writableDatabase

        setButtonListner()
    }

    //{{ @HYEONJIY: 이미지 첨부란의 버튼들(이미지 추가, 삭제)에 리스너를 세팅한다
    private fun setButtonListner()
    {
        // 이미지 추가 버튼 (0:갤러리/1:카메라/2:URL입력칸 등장)
        addImageBtn.setOnClickListener {
            var builder = AlertDialog.Builder(this)
            builder.setTitle("이미지를 무엇으로 추가하시겠습니까?").setItems(
                arrayOf("GALLERY", "CAMERA", "URL"),
                DialogInterface.OnClickListener { _, which ->
                    when (which) {
                        0 -> {
                            getPicturefromGallery()
                        }
                        1 -> {
                            takePicture()
                        }
                        2 -> {
                            //URL 입력 EditText 처리.
                            url_new.visibility =  View.VISIBLE

                            //URL 입력 후 완료 버튼을 눌러야 이미지 출력 가능하게 구현.
                            url_new.setOnEditorActionListener { v, actionId, event ->
                                if(actionId == EditorInfo.IME_ACTION_DONE) {
                                    url_new.visibility = View.GONE
                                    imageURL = v.text.toString()
                                    if(imageURL == null) {
                                        false
                                    }
                                    else {
                                        getImageFromURL()
                                        true
                                    }
                                }
                                else {
                                    url_new.visibility = View.GONE
                                    false
                                }
                            }
                        }
                    }
                })
            builder.create()
            builder.show()
        }

        // 이미지 삭제 버튼 (마지막에 추가한 이미지부터 삭제)
        deleteImgBtn.setOnClickListener {
            if(numImages > 0) {
                numImages--

                var deletedImageView:ImageView? = linear_image[3+numImages] as ImageView
                if(deletedImageView != null) {
                    linear_image.removeView(deletedImageView)
                    newImageByteCode.removeAt(newImageByteCode.size-1)
                }
            }
        }
    }
    //}} @HYEONJIY

    //{{ @HYEONJIY: N개 이미지 출력을 위한 이미지뷰 동적 생성
    private fun createImageView(bitmap: Bitmap) {

        var addedImageView = ImageView(this)
        addedImageView.setImageBitmap(bitmap)

        linear_image.addView(addedImageView)
        addedImageView.visibility = View.VISIBLE
    }
    //}} @HYEONJIY

    //{{ @HYEONJIY: 메모 작성 후 SAVE 버튼 누르면 후에 메인 Activity에 보내줄 데이터(newMemo)를 세팅한다.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.save_memo_new -> {
                if (checkTitleOverlap()) {
                    Toast.makeText(applicationContext, "<저장불가>제목이 동일한 메모가 있습니다..", Toast.LENGTH_LONG).show()
                    return false
                }
                if (newtitle.text.isNotEmpty()) {

                    var newMemo = DetailMemoClass()
                    newMemo.title = newtitle.text.toString()
                    newMemo.desc = newdesc.text.toString()

                    // 이미지 개수가 1개 이상이라면 썸네일 세팅과 n개 이미지 출력을 위해 imagelist DB갱신을 해준다.
                    if (numImages > 0) {
                        newMemo.thumbnailSrc = newImageByteCode[0]

                        addImagetoDB(newMemo)
                    } else {
                        newMemo.thumbnailSrc = null
                    }

                    var intent = Intent()
                    intent.putExtra("newMemo", newMemo)
                    setResult(REQUESTCODE.NEW_MEMO.value, intent)

                    Toast.makeText(applicationContext, "메모가 저장되었습니다.", Toast.LENGTH_LONG).show()
                }
                return true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
        return false
    }
    //}} @HYEONJIY

    //{{ @HYEONJIY: 메모 작성 후 메인 Activity로 돌아갈 때 n개 이미지 출력을 위해 imagelist DB를 갱신해준다.
    private fun addImagetoDB(newMemo: DetailMemoClass) {
        var contentValues = ContentValues()

        // 추가한 이미지갯수만큼 돈다.
        for ((idx, image) in newImageByteCode.withIndex()) {

            contentValues.put("title", newMemo.title)  // 1. title 등록.
            contentValues.put("image", image) // 2. 이미지 등록
            contentValues.put("imageIdx", idx) // 3. 인덱스 등록

            imagedb?.insert("imagelist", null, contentValues) // 4. db에 삽입.
        }
    }
    //}} @HYEONJIY

    //{{ @HYEONJIY: 이미지 추가 버튼 중 하나 선택 후 이미지를 가져오는 부분.
    // 1. 갤러리에서 사진 가져오기
    private fun getPicturefromGallery() {
        var intent = Intent(Intent.ACTION_PICK)
        intent.type = android.provider.MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, REQUESTCODE.OPEN_GALLERY.value)
    }

    // 2. 카메라로 사진찍기
    private fun takePicture() {
        var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUESTCODE.OPEN_CAMERA.value)
    }

    // 3.  외부 URL로부터 이미지를 로드.
    private fun getImageFromURL() {
        Glide.with(this).asBitmap().load(imageURL).error(R.mipmap.ic_launcher).into( object : CustomTarget<Bitmap>() {
            override fun onLoadCleared(placeholder: Drawable?) {
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                super.onLoadFailed(errorDrawable)

                Toast.makeText(applicationContext, "잘못된 URL 입니다.", Toast.LENGTH_LONG).show()
            }

            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                var stream = ByteArrayOutputStream()
                var bitmap = memoHelper.resizeBitmap(480, resource, true)
                if(bitmap != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)

                    createImageView(bitmap)

                    newImageByteCode.add(numImages, stream.toByteArray())
                    numImages++
                }
            }
        })
    }

    // 4. 갤러리/카메라 Activity에서 돌아와 필요한 처리.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUESTCODE.OPEN_GALLERY.value -> {
                if (resultCode == Activity.RESULT_OK) {
                    var selectedBitmap = memoHelper.setImagefromGallery(this, data)
                    if(selectedBitmap != null) {
                        createImageView(selectedBitmap)
                        newImageByteCode.add(numImages, memoHelper.getImageByteCode()!!)
                        numImages++
                    }
                    else {
                        Toast.makeText(applicationContext, "이미지를 첨부할 수 없습니다.", Toast.LENGTH_LONG).show();
                    }
                }
            }
            REQUESTCODE.OPEN_CAMERA.value -> {
                if (resultCode == Activity.RESULT_OK) {
                    var selectedBitmap = memoHelper.setImageFromCamera(this, data)
                    if(selectedBitmap != null) {
                        createImageView(selectedBitmap)
                        newImageByteCode.add(numImages, memoHelper.getImageByteCode()!!)
                        numImages++
                    }
                }
            }
        }
    }
    //}} @HYEONJIY

    //{{ @HYEONJIY: 현재 imagelist 테이블에서 title이름으로 조회하므로 중복제목을 피하기위해 추가한 함수.
    private fun checkTitleOverlap() : Boolean {
        var title = newtitle.text.toString()
        var c: Cursor? = imagedb?.rawQuery("select * from memolist where title = ?", arrayOf(title))

        return (c?.count!! > 0)
    }
    //}} @HYEONJIY

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