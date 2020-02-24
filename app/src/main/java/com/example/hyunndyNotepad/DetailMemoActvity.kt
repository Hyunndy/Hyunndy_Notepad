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
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
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

import kotlinx.android.synthetic.main.activity_detail_memo.*
import kotlinx.android.synthetic.main.content_detail_memo.*
import java.io.ByteArrayOutputStream

/* --------------------------------------------------------------------------------------------------
작성자: HYEONJIYOO
작성일: 2020.02.24
클래스명: DetailMemoActvity
클래스기능:
1. 메모 상세히보기/편집/삭제를 담당하는 Activity.
2. MainActivity에서 리스트뷰 항목을 선택하면 이동하는 Activity.
3. 편집버튼을 누르면 이미지 첨부/삭제 버튼이 나타남.
-------------------------------------------------------------------------------------------------- */

class DetailMemoActvity : AppCompatActivity() {

    // 메모가 수정되었는지의 flag
    private var isModified = false

    // 리스트뷰에서 넘어온 메모의 정보를 저장하는 객체
    private lateinit var detailMemo:DetailMemoClass

    // DB
    private var helper: NotepadDBHelper? = null
    private var imagedb: SQLiteDatabase? = null

    // N개의 이미지를 위한 변수
    private var newImageByteCode = arrayListOf<ByteArray>()
    private var numImages:Int = 0

    // URL
    private var imageURL = ""

    // memo 관련 동작(Bitmap 변환 등)등을 모아둔 MemoHelper 클래스 객체
    private var memoHelper = MemoHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_memo)
        setSupportActionBar(toolbar)

        var imageInflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        imageInflater.inflate(R.layout.content_detail_memo, linear_image_detail, false)

        helper = NotepadDBHelper(this)
        imagedb = helper?.writableDatabase

        // MainActivity에서 클릭한 항목의 메모를 자세히 본다.
        showMemo()

        // 버튼 리스너 세팅
        setButtonListner()

    }

    //{{ @HYEONJIY: 이미지 첨부란의 버튼들(이미지 추가, 삭제)에 리스너를 세팅한다
    private fun setButtonListner()
    {
        // 이미지 추가 버튼 (0:갤러리/1:카메라/2:URL입력칸 등장)
        addImageBtn_detail.setOnClickListener {
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
                            url_new_detail.visibility =  View.VISIBLE

                            //URL 입력 후 완료 버튼을 눌러야 이미지 출력 가능하게 구현.
                            url_new_detail.setOnEditorActionListener { v, actionId, event ->
                                if(actionId == EditorInfo.IME_ACTION_DONE) {
                                    url_new_detail.visibility = View.GONE
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
                                    url_new_detail.visibility = View.GONE
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
        deleteImgBtn_detail.setOnClickListener {
            if(numImages > 0) {
                numImages--

                var deletedImageView:ImageView? = linear_image_detail[5+numImages] as ImageView
                if(deletedImageView != null) {
                    linear_image_detail.removeView(deletedImageView)
                    newImageByteCode.removeAt(newImageByteCode.size-1)
                }
            }
        }
    }
    //}} @HYEONJIY

    //{{ @HYEONJIY: MainActivity에서 넘어온 intent를 받아와 메모 상세화면 세팅 / imagelist DB를 읽어 이미지 출력
    private fun showMemo()
    {
        detailMemo = DetailMemoClass()
        detailMemo = intent.getParcelableExtra<DetailMemoClass>("DetailMemo")!!

        detail_title.text = detailMemo.title
        detail_desc.text = detailMemo.desc

        if(detailMemo.thumbnailSrc != null) {
            var bitmap = BitmapFactory.decodeByteArray(detailMemo.thumbnailSrc, 0, detailMemo.thumbnailSrc?.size!!)

            readDB()
            addImageView()
        }
    }
    //}} @HYEONJIY

    //{{ @HYEONJIY: imagelist를 읽어서 이미지 출력
    private fun readDB() {
        var title = (detail_title.text.toString())
        var c: Cursor = imagedb?.rawQuery("select * from imagelist where title =?", arrayOf(title))!!

        while(c.moveToNext()) {
            var imagepos = c.getColumnIndex("image")
            var idxpos = c.getColumnIndex("imageIdx")

            var imageData = c.getBlob(imagepos)
            var idxData = c.getInt(idxpos)

            newImageByteCode.add(idxData, imageData)
        }
    }
    //}} @HYEONJIY

    //{{ @HYEONJIY: 이미지뷰 동적 생성
    private fun addImageView() {
        for((idx, image) in newImageByteCode.withIndex()) {
            var addedImageView = ImageView(this)
            var bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)

            if(bitmap!= null) {
                //Bitmap = resizeBitmap(480, Bitmap) // 이미지 조절 추가
                addedImageView.setImageBitmap(bitmap)

                linear_image_detail.addView(addedImageView)
                numImages++
            }
        }
    }
    //}} @HYEONJIY

    //{{ @HYEONJIY: 액션바의 이미지 편집/저장/삭제가 눌렸을 때의 처리
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_memo -> {
                detail_edit_Image.visibility = View.VISIBLE
                isModified = true
                editMemo()
                true
            }
            R.id.save_memo_detail -> {
                detail_edit_Image.visibility = View.GONE
                completeModification()
                true
            }
            R.id.delete_memo_detail -> {
                deleteMemo()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
    //}} @HYEONJIY

    //{{ @HYEONJIY: 제목/본문 편집
    private fun editMemo() {
        detail_title.visibility = View.GONE
        detail_desc.visibility = View.GONE

        detail_edittitle.visibility = View.VISIBLE
        detail_editdesc.visibility = View.VISIBLE

        detail_edittitle.setText(detail_title.text)
        detail_editdesc.setText(detail_desc.text)
    }
    //}} @HYEONJIY

    //{{ @HYEONJIY: 메모 삭제 후 MainActivity로 돌아갑니다.
    private fun deleteMemo() {
        var intent = Intent()
        intent.putExtra("deleteMemo", detailMemo.title)

        setResult(RESULTCODE.DELETE_MEMO.value, intent)
        finish()
    }
    //}} @HYEONJIY

    //{{ @HYEONJIY: 메모 저장 관련 함수
    private fun completeModification() {
        completeImage()
        completeTitle()
        completeDesc()
    }

    private fun completeImage() {
        // 첫번째 ImageView에 있는 이미지는 썸네일이 되어야하므로 detailMemo(MainActivity로 보낼 객체)로 전달한다.
        if(numImages > 0) {
            var thumbnailView:ImageView? = linear_image_detail[5] as ImageView
            if(thumbnailView != null) {
                var thumbnail:BitmapDrawable? = thumbnailView.drawable as BitmapDrawable
                if(thumbnail != null) {
                    var bitmap = thumbnail.bitmap
                    var stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)

                    detailMemo.thumbnailSrc = stream.toByteArray()
                }
            }
        }
        else {
            detailMemo.thumbnailSrc = null
        }
    }

    private fun completeTitle() {
        detail_title.visibility = View.VISIBLE
        detail_edittitle.visibility = View.GONE
        detail_title.text = detail_edittitle.text

        detailMemo.title = detail_title.text.toString()
    }

    private fun completeDesc() {
        detail_desc.visibility = View.VISIBLE
        detail_editdesc.visibility = View.GONE

        detail_desc.text = detail_editdesc.text

        detailMemo.desc = detail_desc.text.toString()
    }
    //}} @HYEONJIY

    //{{ @HYEONJIY: Back 버튼을 통해 MainActivity로 돌아갈 경우에 대한 처리
    override fun onBackPressed() {
        // 수정된 경우가 아니면 DB나 MainActivity에 전달할 객체가 없다.
        if(isModified) {
            updateImageDB()

            var intent = Intent()
            intent.putExtra("modifiedMemo", detailMemo)

            setResult(RESULTCODE.MODIFY_MEMO.value, intent)
            finish()
        }
        else {
            super.onBackPressed()
        }
    }
    //}} @HYEONJIY

    //{{ @HYEONJIY: 이미지 추가/삭제한 경우 imagelist DB를 갱신해줘야 한다.
    private fun updateImageDB() {
        var deletedTitle = detail_title.text.toString()
        imagedb?.delete("imagelist", "title=?",arrayOf(deletedTitle))

        var contentValues = ContentValues()
        for ((idx, image) in newImageByteCode.withIndex()) {

            contentValues.put("title", detail_title.text.toString())
            contentValues.put("image", image)
            contentValues.put("imageIdx", idx)

            imagedb?.insert("imagelist", null, contentValues)
        }
    }
    //}} @HYEONJIY

    //{{ @HYEONJIY: 이미지 추가 버튼 중 하나 선택 후 이미지를 가져오는 부분.
    //---------------------------------------------------------------------------------------------------------------
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
                if (bitmap != null) {
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
    //---------------------------------------------------------------------------------------------------------------
    //}} @HYEONJIY

    //{{ @HYEONJIY: N개 이미지 출력을 위한 이미지뷰 동적 생성
    private fun createImageView(bitmap: Bitmap) {
        var addedImageView = ImageView(this)
        addedImageView.setImageBitmap(bitmap)

        linear_image_detail.addView(addedImageView)
    }
    //}} @HYEONJIY

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }
    override fun onDestroy() {
        super.onDestroy()

        isModified = false
        imagedb?.close()
    }
}