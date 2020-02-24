package com.example.hyunndyNotepad

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hyunndyNotepad.MemoItem as RecyclerItem
import com.example.hyunndyNotepad.NotepadAdapter as RecyclerAdapter

//<<!! 각 클래스 파일에 해당 클래스에 대한 설명이 적혀있음 !!>>
/* --------------------------------------------------------------------------------------------------
작성자: HYEONJIYOO
작성일: 2020.02.24
클래스명: MainActivity
클래스기능:
1. 리스트뷰로 간단한 메모 화면을 보여주는 Activity.
2. 내장 DB의 memolist 항목을 읽어 화면을 구성한다.
-------------------------------------------------------------------------------------------------- */

// @HYEONJIY: 다른 Activity와의 작용으로 메모 구성이 바뀌었을 때 리스트뷰 항목 갱신을 위한 열거변수.
enum class UPDATEITEM
{
    READ,
    ADD,
    EDIT,
    DELETE
}

// @HYEONJIY: 다른 Activity에게 보내는 REQUESTCODE 열거변수
enum class REQUESTCODE(val value: Int)
{
    DETAIL_MEMO(100),
    NEW_MEMO(200),
    OPEN_GALLERY(300),
    OPEN_CAMERA(400)
}

// @HYEONJIY: 다른 Activity에서 오는 RESULTCODE 열거변수
enum class RESULTCODE(val value: Int)
{
    MODIFY_MEMO(10),
    DELETE_MEMO(20),
}

class MainActivity : AppCompatActivity() {

    // 권한
    private var permissionList = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.INTERNET
    )

    // 화면 구성
    private var mRecyclerView: RecyclerView? = null
    private var mRecyclerAdapter: RecyclerAdapter? = null
    private var mList = arrayListOf<RecyclerItem>()

    // DB
    private var helper: NotepadDBHelper? = null
    private var memodb: SQLiteDatabase? = null

    // 선택되어 상세 화면으로 넘어간 메모의 Title 캐시 변수
    private var selectedTitle = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.newmemo_toolbar))

        initOnCreate()
    }

    //{{ @HYEONJIY: Create될 때 초기화해야할 항목들.
    private fun initOnCreate() {
        // 권한
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissionList, 0)
        }

        // 화면
        mRecyclerView = findViewById(R.id.recyclerView)
        mRecyclerView?.layoutManager = LinearLayoutManager(this)
        mRecyclerAdapter = RecyclerAdapter{ selectedItem,selectedPosition->
            openDetailMemo(selectedItem, selectedPosition)
        }
        mRecyclerView?.adapter = mRecyclerAdapter

        // DB
        helper = NotepadDBHelper(this)
        memodb = helper?.writableDatabase

        // 리스트항목 업데이트
        readDB()
    }
    //}} @HYEONJIY

    //{{ @HYEONJIY: 액션바 메뉴 클릭 시 이벤트
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_memo_main-> {
                var intent = Intent(this, NewMemoActivity::class.java)
                startActivityForResult(intent, REQUESTCODE.NEW_MEMO.value)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }
    //}} @HYEONJIY

    //{{ @HYEONJIY: 리스트뷰 항목에 연결된 리스너. selectedIdx는 확장성을 고려해 남겨둠.
    private fun openDetailMemo(selectedItem: RecyclerItem, selectedIdx : Int) {
        selectedTitle = selectedItem.getTitle()

        var detailMemo = DetailMemoClass()
        detailMemo.thumbnailSrc = selectedItem.getThumbnail()
        detailMemo.title = selectedItem.getTitle()
        detailMemo.desc = selectedItem.getDesc()

        var intent = Intent(this, DetailMemoActvity::class.java)
        intent.putExtra("DetailMemo", detailMemo)
        startActivityForResult(intent, REQUESTCODE.DETAIL_MEMO.value)
    }
    //}} @HYEONJIY

    //{{ @HYEONJIY: 다른 Activity에서 돌아왔을 때 리스트뷰 항목 구성에 관한 함수
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            REQUESTCODE.DETAIL_MEMO.value -> {
                when(resultCode) {
                    // 메모가 수정되었을 때
                    RESULTCODE.MODIFY_MEMO.value -> {
                        updateDB(data!!)
                    }
                    // 메모가 삭제되었을 때
                    RESULTCODE.DELETE_MEMO.value -> {
                        deleteDB(data!!)
                    }
                }
            }
            REQUESTCODE.NEW_MEMO.value -> {
                    insertDB(data!!)
                }
            }
    }
    //}} @HYEONJIY

    // @HYEONJIY: 다른 Activity에서 오는 CODE에 따른 DB관련(read, insert, update, delete) 함수들
    //-------------------------------------------------------------------------------------------------------
    // 1. 다른 Activity에서 오는 UPDATECODE에 따라 리스트뷰를 구성하는 List를 업데이트한다.
    private fun updateItem(iconPath: ByteArray?, title: String, desc: String?, updateCode:UPDATEITEM) {
        if(updateCode == UPDATEITEM.DELETE) {
            mList.clear()
            readDB()
        }
        else {
            var item = RecyclerItem()
            item.setThumbnail(iconPath)
            item.setTitle(title)
            item.setDesc(desc)

            when(updateCode) {
                UPDATEITEM.READ, UPDATEITEM.ADD -> {
                    mList.add(item)
                }
                UPDATEITEM.EDIT -> {
                    mList.clear()
                    readDB()
                }
            }
        }

        mRecyclerAdapter?.setMemolist(mList)
    }

    // 2. 삭제 요청
    private fun deleteDB(data : Intent) {
        var deleteTitle = data.getStringExtra("deleteMemo")!!
        var temp = memodb?.delete("memolist","title=?", arrayOf(deleteTitle))

        updateItem(null,  deleteTitle, null, UPDATEITEM.DELETE)
    }

    // 3. 리스트 뷰 항목 재구성
    private fun readDB() {
        var sql = "select * from memolist"
        var c: Cursor? = memodb?.rawQuery(sql, null)

        while (c?.moveToNext()!!) {
            var imgPos = c.getColumnIndex("image")
            var titlePos = c.getColumnIndex("title")
            var descPos = c.getColumnIndex("description")

            var imgData = c.getBlob(imgPos)
            var titleData = c.getString(titlePos)
            var descData = c.getString(descPos)

            updateItem(imgData, titleData, descData, UPDATEITEM.READ)
        }
    }

    // 4. 수정
    private fun updateDB(modifiedMemo : Intent?) {

        var memo = modifiedMemo?.getParcelableExtra<DetailMemoClass>("modifiedMemo")!!

        var contentValues = ContentValues()
        contentValues.put("title", memo?.title)
        contentValues.put("image", memo?.thumbnailSrc)
        contentValues.put("description", memo?.desc)

        var nameArr = arrayOf(selectedTitle)
        var change = memodb?.update("memolist", contentValues, "title=?", nameArr)

        updateItem(memo.thumbnailSrc, memo.title, memo.desc,UPDATEITEM.EDIT)
    }

    // 5. 추가
    private fun insertDB(modifiedMemo: Intent?) {
        var memo = modifiedMemo?.getParcelableExtra<DetailMemoClass>("newMemo")

        var contentValues = ContentValues()

        contentValues.put("title", memo?.title)
        contentValues.put("image", memo?.thumbnailSrc)
        contentValues.put("description", memo?.desc)

        memodb?.insert("memolist", null, contentValues)

        val title = (memo?.title)!!

        var c: Cursor? = memodb?.rawQuery("select * from memolist where title = ?", arrayOf(title))

        while (c?.moveToNext()!!) {
            var img_pos = c.getColumnIndex("image")
            var title_pos = c.getColumnIndex("title")
            var desc_pos = c.getColumnIndex("description")

            var imgData = c.getBlob(img_pos)
            var titleData = c.getString(title_pos)
            var descData = c.getString(desc_pos)

            updateItem(imgData, titleData, descData, UPDATEITEM.ADD)
        }
    }
    //-----------------------------------------------------------------------------------------------------
    //@HYEONJIY

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        for(a1 in grantResults) {
            if(a1 == PackageManager.PERMISSION_DENIED) {
                return
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        memodb?.close()
    }
}