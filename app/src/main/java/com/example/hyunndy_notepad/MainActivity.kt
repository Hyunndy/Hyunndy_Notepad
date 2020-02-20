package com.example.hyunndy_notepad

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hyunndy_notepad.MemoItem as RecyclerItem
import com.example.hyunndy_notepad.NotepadAdapter as RecyclerAdapter

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    // 어뎁터, 리사이클러뷰에 지정.
    var mRecyclerView:RecyclerView? = null
    var mRecyclerAdapter: RecyclerAdapter? = null
    var mList = arrayListOf<RecyclerItem>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        fab.setOnClickListener { view ->

            //액티비티 실행
            var intent = Intent(this, NewMemoActivity::class.java)
            startActivity(intent)
        }

        //{{ 20200220 hyeonjiy : 1. 기본 View 세팅
        // 리사이클러뷰 가져오기
        mRecyclerView = findViewById(R.id.recyclerView)

        //리사이클러뷰에 어댑터 객체 지정과 초기화.
        mRecyclerAdapter = RecyclerAdapter(mList)
        {
            openDetailMemo(it)
        }

        //어댑터 지정
        mRecyclerView?.adapter = mRecyclerAdapter

        // 세로 방향 배치를 위해  LinearLayoutManager을 사용한다.
        mRecyclerView?.layoutManager = LinearLayoutManager(this)

        addItem(R.drawable.jellybean, "젤리빈", "안녕하세요제이름은유현지입니다저는지금이어플을개발하고있는데제발자동줄바꿈좀되었으면좋겠네요너무화가나니까요.")

        //}} 20200220 hyeonjiy
    }

    //{{ 20200220 hyeonjiy : 2. 리스트뷰 클릭 시 상세 메모로 넘어감.

    // 리사이클러뷰는 아이템 뷰에서 OnClickListner를 통해 처리하게 만들어놓았다.
    // 어댑터를 통해 만들어진 각 아이템 뷰는 "뷰 홀더"객체에 저장되어 화면에 표시되고, 필요에 따라 생성 또는 재활용 된다.
    // 아이템뷰는 "뷰 홀더"가 갖고있기 때문에 뷰 홀더 객체에서 클릭 이벤트를 처리한다.
    // 여기다가 데이터도 넘겨줘야한다.
    private fun openDetailMemo(simpleMemo : RecyclerItem)
    {
        //{{ 20200221 hyeonjiy: 객체 전달 하기
        // 줄 메모
        var detail_memo = DetailMemoClass()
        detail_memo.imagesrc = simpleMemo.getIcon()
        detail_memo.title = simpleMemo.getTitle()
        detail_memo.desc = simpleMemo.getDesc()

        var intent = Intent(this, DetailMemoActvity::class.java)
        intent.putExtra("DetailMemo", detail_memo)
        startActivityForResult(intent, 0) // 객체 수정하면 다시 메모 업데이트되어야하니까?
        //} 20200221 hyeonjiy:
    }

    //}} 20200220 hyeonjiy

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
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    public fun addItem(iconPath:Int, title:String, desc:String)
    {
        var item = RecyclerItem()

        item.setIcon(iconPath)
        item.setTitle(title)
        item.setDesc(desc)

        mList.add(item)
    }
}
