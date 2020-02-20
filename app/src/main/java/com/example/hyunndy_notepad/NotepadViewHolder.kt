package com.example.hyunndy_notepad

import android.content.ClipData
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import kotlinx.android.synthetic.main.notepad_item.view.*
import org.w3c.dom.Text
import com.example.hyunndy_notepad.MemoItem as RecyclerItem


 //1. 어댑터 구현.
 //2. 리사이클러뷰에서는 반드시 개발자가 어댑터를 직접 구현해야 한다.
// Unit은 아무것도 반환하지 않는다는것이다.
public class NotepadAdapter(val memoList:ArrayList<RecyclerItem>, val itemClick : (RecyclerItem) -> Unit) : RecyclerView.Adapter<NotepadAdapter.NotePadViewHolder>()
{

    // 뷰홀더
     inner class NotePadViewHolder(val memoItem : View, itemClick: (RecyclerItem) -> Unit) : RecyclerView.ViewHolder(memoItem) {
        val image = memoItem?.findViewById<ImageView>(R.id.imageView2)
        val title = memoItem?.findViewById<TextView>(R.id.textView)
        val desc = memoItem?.findViewById<TextView>(R.id.textView3)

        fun bind (Items : RecyclerItem)
        {
            if(Items.getIcon() != -1)
            {
                image.setImageResource(Items.getIcon())
            }
            else
            {
                image.setImageResource(R.mipmap.ic_launcher)
            }

            title.text = Items.getTitle()
            desc.text = Items.getDesc()

            // 메모하나가 클릭됐을 때 처리할 일을 itemClick으로 설정한다.
            // (RecyclerItem) -> Unit에 대한 함수는 나중에 mainactivity.kt에서 작성한다.
            memoItem.setOnClickListener{ itemClick(Items) }
        }
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
        return NotePadViewHolder(memoview, itemClick)
    }

    // @onBindViewHolder()
    // position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시
    // 데이터를 뷰홀더에 바인딩.
    override fun onBindViewHolder(holder: NotePadViewHolder, position: Int) {

        holder.bind(memoList[position])
    }


    // 전체 아이템 갯수 리턴
    override fun getItemCount(): Int {
        return memoList.size
    }

}