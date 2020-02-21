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
import org.w3c.dom.Text
import com.example.hyunndy_notepad.MemoItem as RecyclerItem
import com.example.hyunndy_notepad.NotepadAdapter as RecyclerAdapter

class MainActivity : AppCompatActivity() {

    val DETAILMEMO = 0
    val NEWMEMO = 1

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

        //권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permission_list, 0)
        }

        helper = NotepadDBHelper(this)
        memodb = helper?.writableDatabase

        //{{ 20200220 hyeonjiy : 1. 기본 View 세팅
        // 리사이클러뷰 가져오기
        mRecyclerView = findViewById(R.id.recyclerView)

        // 세로 방향 배치를 위해  LinearLayoutManager을 사용한다.
        mRecyclerView?.layoutManager = LinearLayoutManager(this)


        //리사이클러뷰에 어댑터 객체 지정과 초기화.
        mRecyclerAdapter = RecyclerAdapter()
        {
            openDetailMemo(it)
        }

        readDB()

        mRecyclerAdapter?.setMemolist(mList)


        //어댑터 지정
        mRecyclerView?.adapter = mRecyclerAdapter
        //}}

        // DB읽고 출력

        //}} 20200220 hyeonjiy

        // @HACK
        //fab.setOnClickListener { view ->
        //    var sql = "update memolist set title=? where image=?"
        //    var args = arrayOf("지존현지", "R.drawable.jellybean")
        //    memodb?.execSQL(sql, args)
        //}

        //fab2.setOnClickListener { view ->

        //    val values = ContentValues().apply {
        //        put("image", R.drawable.jellybean)
        //        put("title", "젤리빈")
        //        put("description", "이것이자바다난정말자바를공부한적이없어요뇌를자극하는알고리즘컴퓨터구조와원리")
        //    }

        //    // -1 반환처리 필요.
        //    var newRowId = memodb?.insert("memolist", null, values)
        //}
    }

    //{{ 20200220 hyeonjiy : 2. 리스트뷰 클릭 시 상세 메모로 넘어감.
    // 리사이클러뷰는 아이템 뷰에서 OnClickListner를 통해 처리하게 만들어놓았다.
    // 어댑터를 통해 만들어진 각 아이템 뷰는 "뷰 홀더"객체에 저장되어 화면에 표시되고, 필요에 따라 생성 또는 재활용 된다.
    // 아이템뷰는 "뷰 홀더"가 갖고있기 때문에 뷰 홀더 객체에서 클릭 이벤트를 처리한다.
    // 여기다가 데이터도 넘겨줘야한다.
    private fun openDetailMemo(simpleMemo: RecyclerItem) {
        //{{ 20200221 hyeonjiy: 객체 전달 하기
        // 줄 메모
        var detail_memo = DetailMemoClass()

        detail_memo.idx = simpleMemo.getIdx()
        detail_memo.imagesrc = simpleMemo.getIcon()
        detail_memo.title = simpleMemo.getTitle()
        detail_memo.desc = simpleMemo.getDesc()

        var intent = Intent(this, DetailMemoActvity::class.java)
        intent.putExtra("DetailMemo", detail_memo)

        Log.d("test1", "여기서뻑남")


        startActivityForResult(intent, DETAILMEMO) // 객체 수정하면 다시 메모 업데이트되어야하니까?

        Log.d("test1", "여기서뻑남2")
        //} 20200221 hyeonjiy
    }
    //}} 20200220 hyeonjiy


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK)
        {
            // UPDATE로 메모 업데이트
            Log.d("test1", "여기들어오긴하니...?")
            updateDB(data)
        }
        else if(resultCode == Activity.MODE_APPEND)
        {
            var memo = data?.getParcelableExtra<DetailMemoClass>("newMemo")

            var contentValues = ContentValues()
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

                Log.d("test2", c?.getColumnName(idx_pos))
                Log.d("test2", c?.getColumnName(img_pos))
                Log.d("test2", c?.getColumnName(title_pos))
                Log.d("test2", c?.getColumnName(desc_pos))
                addItem(idx, imgData, titleData, descData)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                return true
            }
            R.id.add_memo -> {
                var intent = Intent(this, NewMemoActivity::class.java)
                startActivityForResult(intent, NEWMEMO)
                return true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    public fun addItem(memoIdx:Int, iconPath: ByteArray, title: String, desc: String) {

        var item = RecyclerItem()

        item.setIdx(memoIdx)
        item.setIcon(iconPath)
        item.setTitle(title)
        item.setDesc(desc)

        mList.add(item)
        mRecyclerAdapter?.setMemolist(mList)
    }

    override fun onDestroy() {
        super.onDestroy()

        // DB 닫음
        memodb?.close()
    }

    private fun readDB() {
        //{{ 20200221 hyeonjiy : 4. DB오픈

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
            addItem(idx, imgData, titleData, descData)
        }

        //}} 20200221
    }

    override fun onStart() {
        super.onStart()

        Log.d("test1", "리스타트@@@@@@@@@@@@@@@@@@@@@@@@@")
    }


    fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        var Bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        return Bitmap
    }

    // 수정완료되었기 때문에 DB업데이트
    private fun updateDB(modifiedMemo : Intent?) {
        //객체 받아오기
        var memo = modifiedMemo?.getParcelableExtra<DetailMemoClass>("modifiedMemo")

        var contentValues = ContentValues()
        contentValues.put("title", memo?.title)
        contentValues.put("description", memo?.desc)

        var nameArr = arrayOf(memo?.idx.toString())

        var change = memodb?.update("memolist", contentValues, "idx=?", nameArr)

        Log.d("test1", "${change}")
        Log.d("test1", "${memo?.title}")
        Log.d("test1", "${memo?.desc}")
    }
}

