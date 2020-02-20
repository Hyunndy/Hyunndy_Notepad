package com.example.hyunndy_notepad

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
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        // 리사이클러뷰 가져오기
        mRecyclerView = findViewById(R.id.recyclerView)

        //리사이클러뷰에 어댑터 객체 지정과 초기화.
        mRecyclerAdapter = RecyclerAdapter(mList)

        //어댑터 지정
        mRecyclerView?.adapter = mRecyclerAdapter

        // 세로 방향 배치를 위해  LinearLayoutManager을 사용한다.
        mRecyclerView?.layoutManager = LinearLayoutManager(this)

        addItem(R.drawable.jellybean, "젤리빈", "이것은 젤리빈입니다.")


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
