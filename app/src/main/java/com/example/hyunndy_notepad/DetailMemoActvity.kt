package com.example.hyunndy_notepad

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_detail_memo_actvity.*
import kotlinx.android.synthetic.main.content_detail_memo_actvity.*

class DetailMemoActvity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_memo_actvity)
        setSupportActionBar(toolbar)

        //{{ 4. 20200221 hyeonjiy : 전달받은 텍스트 내용 출력하기
        var detail_memo = intent.getParcelableExtra<DetailMemoClass>("DetailMemo")
        imageView.setImageResource(detail_memo.imagesrc!!)
        textView2.text = detail_memo.title
        textView4.text = detail_memo.desc
        //}}
    }

}
