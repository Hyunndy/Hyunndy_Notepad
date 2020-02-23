package com.example.hyunndy_notepad

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
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.core.view.marginTop
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

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

    private var imageURL = ""


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


        //**HYEONJIY** 버튼이벤트를 만든다!
        // 추가 이벤트.
        addImageBtn_detail.setOnClickListener { view ->
            // 1. AlertDialogue
            val builder = AlertDialog.Builder(this)

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
                            // 1. 여기서 버튼 클릭 버튼 만들고, 리스너 세팅해서 getImageFromURL()
                            url_new_detail.visibility =  View.VISIBLE
                            url_new_detail.setOnEditorActionListener { v, actionId, event ->
                                if(actionId == EditorInfo.IME_ACTION_DONE)
                                {
                                    url_new_detail.visibility = View.GONE
                                    imageURL = v.text.toString()
                                    if(imageURL.isEmpty())
                                    {
                                        false
                                    }
                                    else
                                    {
                                        getImageFromURL()
                                        true
                                    }
                                }
                                else
                                {
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

        deleteImgBtn_detail.setOnClickListener { view ->
            if(nimage > 0)
            {
                nimage--
                var deletedImageView:ImageView? = linear_image_detail[5+nimage] as ImageView
                if(deletedImageView != null)
                {
                    linear_image_detail.removeView(deletedImageView)
                    newImageByteCode.removeAt(newImageByteCode.size-1)
                }
            }
        }
    }

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_memo ->
            {
                // 이미지 첨부란이 갑자기 띠용
                detail_edit_Image.visibility = View.VISIBLE
                isModified = true
                editMemo()
                true
            }
            R.id.save_memo_detail ->
            {
                detail_edit_Image.visibility = View.GONE
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

    private fun showMemo()
    {
        detailMemo = DetailMemoClass()
        detailMemo = intent.getParcelableExtra<DetailMemoClass>("DetailMemo")!!

        detail_title.text = detailMemo.title
        detail_desc.text = detailMemo.desc

        // **HYEONJIY** 썸네일 이제 없앨거니까!
       if(detailMemo.thumbnailsrc != null)
       {
           var bitmap = BitmapFactory.decodeByteArray(detailMemo.thumbnailsrc, 0, detailMemo.thumbnailsrc?.size!!)
           //bitmap = resizeBitmap(480, bitmap)

           readDB()
           addImageView()
       }
    }

    // **HYEONJIY** DB읽기
    private fun readDB() {
        var title = (detail_title.text.toString())
        var c: Cursor = imagedb?.rawQuery("select * from imagelist where title =?", arrayOf(title))!!
        var imageCount = c?.count

         while(c.moveToNext()) {

             val Imagepos = c.getColumnIndex("image")
             val idxpos = c.getColumnIndex("imageIdx")

             val imageData = c.getBlob(Imagepos)
             val idxData = c.getInt(idxpos)

             newImageByteCode.add(idxData, imageData)
         }
    }

    // **HYEONJIY** 썸네일 빼고 VIEW숫자만큼.
    private fun addImageView()
    {
        for((idx, image) in newImageByteCode.withIndex())
        {
            var addedImageView = ImageView(this)
            var Bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)

            if(Bitmap!= null)
            {
                //Bitmap = resizeBitmap(480, Bitmap) // 이미지 조절 추가
                addedImageView.setImageBitmap(Bitmap)

                linear_image_detail.addView(addedImageView)
                nimage++
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
        //**HYEONJIY** 이미지가 존재 한다면
        if(nimage > 0)
        {
            var thumbnailView:ImageView? = linear_image_detail[5] as ImageView
            if(thumbnailView != null)
            {
                val thumbnail:BitmapDrawable? = thumbnailView.drawable as BitmapDrawable
                if(thumbnail != null)
                {
                    val bitmap = thumbnail.bitmap
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)

                    detailMemo.thumbnailsrc = stream.toByteArray()
                }
            }
        }
        else
        {
            detailMemo.thumbnailsrc = null
        }
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

    private fun updateImageDB()
    {
        // 1. 일단 날린다.
        val deletedTitle = detail_title.text.toString()
        imagedb?.delete("imagelist", "title=?",arrayOf(deletedTitle))

        // 2. 그 다음 넣는다.
        var contentValues = ContentValues()

        for ((idx, image) in newImageByteCode.withIndex()) {
            // 1. title 등록.
            contentValues.put("title", detail_title.text.toString())

            // 2. 이미지 등록
            contentValues.put("image", image)

            // 3. 인덱스 등록
            contentValues.put("imageIdx", idx)

            // 4. db에 넣자! 그리고 이걸 detailmemo에서 꺼내쓰면 된다.
            imagedb?.insert("imagelist", null, contentValues)
        }

    }

    override fun onBackPressed() {

        // detailImage를 또 클릭했을 경우를 위해 소통을 위해 imagedb업뎃해줘야함.
        updateImageDB()

        // 메인에 보내줄 애들.
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
    private fun resizeBitmap(targetWidth: Int, source: Bitmap, isURL:Boolean=false): Bitmap {
        var ratio = source.height.toDouble() / source.width.toDouble()
        var targetHeight = (targetWidth * ratio).toInt()

        if(targetHeight == source.height)
        {
            targetHeight/=2
        }

        var result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false)
        if (result != source && !isURL)
        {
            source.recycle()
        }
        return result
    }

    // 3. 사진 선택 뷰에서 돌아왔을 때.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 앨범선택칸에서 다시 돌아왔을 때.
        when (requestCode) {
            REQUESTCODE.OPEN_GALLERY.value -> {
                if (resultCode == Activity.RESULT_OK) {
                    var c = contentResolver.query(data?.data!!, null, null, null, null)
                    c?.moveToNext()

                    var index = c?.getColumnIndex(MediaStore.Images.Media.DATA)
                    var source = c?.getString(index!!)

                    val stream = ByteArrayOutputStream()

                    var option = BitmapFactory.Options()
                    option.inSampleSize = 1
                    var bitmap = BitmapFactory.decodeFile(source, option)
                    bitmap = resizeBitmap(480, bitmap)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)

                    selectImageView(bitmap)

                    newImageByteCode.add(nimage, stream.toByteArray())
                    nimage++
                }
            }

            REQUESTCODE.OPEN_CAMERA.value -> {
                // 정상코드
                if (resultCode == Activity.RESULT_OK) {
                    val stream = ByteArrayOutputStream()

                    // ** HYEONJIY ** 일단 안되니까 깨져도 이걸로 가자.
                    var bitmap = data?.getParcelableExtra<Bitmap>("data")!!
                    bitmap = resizeBitmap(480, bitmap)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
                    selectImageView(bitmap)
                    newImageByteCode.add(nimage, stream.toByteArray())
                    nimage++
                }
            }
        }
    }

    //{{ @HYEONJIY 5. 외부 URL 관련은 GLIDE
    //---------------------------------------------------------------------------------------------------------------
    private fun getImageFromURL()
    {
        Glide.with(this).asBitmap().load(imageURL).error(R.mipmap.ic_launcher).into( object : CustomTarget<Bitmap>()
        {
            override fun onLoadCleared(placeholder: Drawable?)
            {
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                super.onLoadFailed(errorDrawable)

                Toast.makeText(applicationContext, "잘못된 URL 입니다.", Toast.LENGTH_LONG).show()
            }

            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?)
            {
                var stream = ByteArrayOutputStream()
                var bitmap = resizeBitmap(480, resource, true)
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)

                selectImageView(bitmap)

                newImageByteCode.add(nimage, stream.toByteArray())
                nimage++

            }
        })
    }
    //---------------------------------------------------------------------------------------------------------------
    //}}

    private fun selectImageView(bitmap: Bitmap) {

        var addedImageView = ImageView(this)
        addedImageView.setImageBitmap(bitmap)

        linear_image_detail.addView(addedImageView)
        //}
    }


}