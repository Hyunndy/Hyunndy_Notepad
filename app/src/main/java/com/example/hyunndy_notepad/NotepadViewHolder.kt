package com.example.hyunndy_notepad

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import kotlinx.android.synthetic.main.notepad_item.view.*
import org.w3c.dom.Text
import com.example.hyunndy_notepad.MemoItem as RecyclerItem

 //1. 어댑터 구현.
 //2. 리사이클러뷰에서는 반드시 개발자가 어댑터를 직접 구현해야 한다.
public class NotepadAdapter() : RecyclerView.Adapter<NotepadAdapter.NotePadViewHolder>()
{
    //아이템리스트.
    private var mData = arrayListOf<RecyclerItem>()

    // 생성자
    constructor(items : ArrayList<RecyclerItem>) : this()
    {
        mData = items
    }

    // 뷰홀더
    public class NotePadViewHolder(val memoItem : View) : RecyclerView.ViewHolder(memoItem) {
    }


    // @onCreateViewHolder()
    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    // 아이템 뷰를 위한 뷰홀더 객체를 생성하여 리턴.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotePadViewHolder {

        //  var context = parent.context
        // Inflater : xml코드로 작성된 레이아웃을 view로 실체화시켜주는것.
        //  var inflater: LayoutInflater =
        //  context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // Inflater로 view 생성
        var memoview = LayoutInflater.from(parent.context).inflate(R.layout.notepad_item, parent, false)

        // view를 리턴.
        return NotePadViewHolder(memoview)
    }


    // @onBindViewHolder()
    // position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시
    // 데이터를 뷰홀더에 바인딩.
    override fun onBindViewHolder(holder: NotePadViewHolder, position: Int) {

        // 이미지뷰세팅
        holder.memoItem.imageView2.setImageResource(mData[position].getIcon())

        // 제목세팅
        holder.memoItem.textView.text = mData[position].getTitle()

        // 본문세팅
        holder.memoItem.textView3.text = mData[position].getDesc()
    }

    // 전체 아이템 갯수 리턴
    override fun getItemCount(): Int {
        return mData.size
    }

}