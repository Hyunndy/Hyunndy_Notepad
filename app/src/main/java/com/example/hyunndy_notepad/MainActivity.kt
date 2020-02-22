package com.example.hyunndy_notepad

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
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
import com.example.hyunndy_notepad.MemoItem as RecyclerItem
import com.example.hyunndy_notepad.NotepadAdapter as RecyclerAdapter

enum class UPDATEITEM
{
    READ,
    ADD,
    EDIT,
    DELETE
}

enum class REQUESTCODE(val value: Int)
{
    DETAIL_MEMO(100),
    NEW_MEMO(200),
    OPEN_GALLERY(300)
}

enum class RESULTCODE(val value: Int)
{
    MODIFY_MEMO(10),
    DELETE_MEMO(20)
}

class MainActivity : AppCompatActivity() {

    //권한
    var permission_list = arrayOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    // 어뎁터, 리사이클러뷰에 지정.
    var mRecyclerView: RecyclerView? = null
    var mRecyclerAdapter: RecyclerAdapter? = null
    var mList = arrayListOf<RecyclerItem>()

    // DB관련
    var helper: NotepadDBHelper? = null
    var memodb: SQLiteDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.newmemo_toolbar))

        initOnCreate()
    }

    // 1. 초기화 관련
    //-----------------------------------------------------------------------------------------------------
    private fun initOnCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permission_list, 0)
        }

        helper = NotepadDBHelper(this)
        memodb = helper?.writableDatabase

        mRecyclerView = findViewById(R.id.recyclerView)
        mRecyclerView?.layoutManager = LinearLayoutManager(this)

        mRecyclerAdapter = RecyclerAdapter{ openDetailMemo(it) }

        readDB()

        mRecyclerView?.adapter = mRecyclerAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    //-------------------------------------------------------------------------------------------------------

    // 2. 다른 Activity와의 상호작용 관련
    //-------------------------------------------------------------------------------------------------------
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_settings -> {
                true
            }
            R.id.add_memo -> {
                var intent = Intent(this, NewMemoActivity::class.java)
                startActivityForResult(intent, REQUESTCODE.NEW_MEMO.value)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun openDetailMemo(simpleMemo: RecyclerItem) {
        var detail_memo = DetailMemoClass()

        detail_memo.idx = simpleMemo.getIdx()
        detail_memo.imagesrc = simpleMemo.getIcon()
        detail_memo.title = simpleMemo.getTitle()
        detail_memo.desc = simpleMemo.getDesc()

        Log.d("test1", "open 상세메모에서의 인덱스 = ${detail_memo.idx}")

        var intent = Intent(this, DetailMemoActvity::class.java)
        intent.putExtra("DetailMemo", detail_memo)

        startActivityForResult(intent, REQUESTCODE.DETAIL_MEMO.value)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode)
        {
            REQUESTCODE.DETAIL_MEMO.value ->
            {
                when(resultCode)
                {
                    RESULTCODE.MODIFY_MEMO.value ->
                    {
                        updateDB(data!!)
                    }
                    RESULTCODE.DELETE_MEMO.value ->
                    {
                        deleteDB(data!!)
                    }
                }

            }
            REQUESTCODE.NEW_MEMO.value ->
            {
                insertDB(data)
            }
        }
    }
    //-------------------------------------------------------------------------------------------------------

    // 3. DB관련(read, insert, update, delete)
    //-------------------------------------------------------------------------------------------------------
    private fun deleteDB(data : Intent)
    {
        var deleteIdx = data.getIntExtra("deleteMemo", -1)
        if(deleteIdx < 0)
        {
            return
        }

        var temp = memodb?.delete("memolist","idx=?", arrayOf(deleteIdx.toString()))

        updateItem(deleteIdx, null, null, null, UPDATEITEM.DELETE)
    }
    private fun updateItem(memoIdx:Int, iconPath: ByteArray?, title: String?, desc: String?, updateCode:UPDATEITEM) {

        if(updateCode == UPDATEITEM.DELETE)
        {
            if(mList.size == 1)
            {
                mList.clear()
            }
            else
            {
                mList.removeAt(memoIdx)
            }
        }
        else
        {
            var item = RecyclerItem()

            item.setIdx(memoIdx)
            item.setIcon(iconPath)
            item.setTitle(title)
            item.setDesc(desc)

            when(updateCode)
            {
                UPDATEITEM.READ, UPDATEITEM.ADD ->
                {
                    Log.d("test1", "인덱스 시발 관리하기 존나귀찮아 = ${memoIdx}")
                    mList.add(item)
                }
                UPDATEITEM.EDIT ->
                {
                    mList[memoIdx] = item
                }
            }
        }

        mRecyclerAdapter?.setMemolist(mList)
    }

    private fun readDB() {
        val sql = "select * from memolist"

        var c: Cursor? = memodb?.rawQuery(sql, null)

        while (c?.moveToNext()!!) {
            var idx_pos = c.getColumnIndex("idx")
            var img_pos = c.getColumnIndex("image")
            var title_pos = c.getColumnIndex("title")
            var desc_pos = c.getColumnIndex("description")

            var idx = c.getInt(idx_pos)
            var imgData = c.getBlob(img_pos)
            var titleData = c.getString(title_pos)
            var descData = c.getString(desc_pos)

            // 리스트 출력!
            updateItem(idx, imgData, titleData, descData, UPDATEITEM.READ)
        }
    }

    private fun updateDB(modifiedMemo : Intent?) {
        //객체 받아오기
        var memo = modifiedMemo?.getParcelableExtra<DetailMemoClass>("modifiedMemo")!!

        var contentValues = ContentValues()
        contentValues.put("title", memo?.title)
        contentValues.put("description", memo?.desc)

        var nameArr = arrayOf(memo.idx.toString())

        var change = memodb?.update("memolist", contentValues, "idx=?", nameArr)

        Log.d("test1", "update 쿼리문에서의 인덱스 = ${memo.idx}")

        updateItem(memo.idx, memo.imagesrc, memo.title, memo.desc,UPDATEITEM.EDIT)

    }

    private fun insertDB(modifiedMemo: Intent?)
    {
        var memo = modifiedMemo?.getParcelableExtra<DetailMemoClass>("newMemo")

        var contentValues = ContentValues()
        contentValues.put("idx", mList.size)
        contentValues.put("image", memo?.imagesrc)
        contentValues.put("title", memo?.title)
        contentValues.put("description", memo?.desc)

        memodb?.insert("memolist", null, contentValues)

        var c: Cursor? = memodb?.rawQuery("select * from memolist where title = ?", arrayOf(memo?.title))

        while (c?.moveToNext()!!) {
            var idx_pos = c.getColumnIndex("idx")
            var img_pos = c.getColumnIndex("image")
            var title_pos = c.getColumnIndex("title")
            var desc_pos = c.getColumnIndex("description")

            var idx = c.getInt(idx_pos)
            var imgData = c.getBlob(img_pos)
            var titleData = c.getString(title_pos)
            var descData = c.getString(desc_pos)

            updateItem(idx, imgData, titleData, descData, UPDATEITEM.ADD)
        }
    }

    fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        var Bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        return Bitmap
    }
    //-----------------------------------------------------------------------------------------------------

    override fun onDestroy() {
        super.onDestroy()

        memodb?.close()
    }
}

