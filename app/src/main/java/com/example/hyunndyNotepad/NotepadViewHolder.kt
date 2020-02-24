package com.example.hyunndyNotepad

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hyunndyNotepad.MemoItem as RecyclerItem

/* --------------------------------------------------------------------------------------------------
작성자: HYEONJIYOO
작성일: 2020.02.24
클래스명: NotepadAdapter
클래스기능:
1. 어댑터 클래스.
2. 리스트뷰 항목에 리스너 세팅. 선택된 항목의 Position을 리턴해서 후에 이용 가능.
-------------------------------------------------------------------------------------------------- */

class NotepadAdapter(val itemClick: (RecyclerItem, Int) -> Unit)  : RecyclerView.Adapter<NotepadAdapter.NotePadViewHolder>()
{
    private var updateMemolist = arrayListOf<RecyclerItem>()

    //{{ @HYEONJIY: 메모 수정 시 리스트뷰 항목을 업데이트 하는 함수.
    fun setMemolist(set:ArrayList<RecyclerItem>)
    {
        if(this.updateMemolist != null)
        {
            this.updateMemolist.clear()
            this.updateMemolist.addAll(set)
        }

        notifyDataSetChanged()
    }
    //}} @HYEONJIY

     inner class NotePadViewHolder(var memoItem:View, var itemClick: (RecyclerItem, Int) -> Unit) : RecyclerView.ViewHolder(memoItem) {
        val image = memoItem?.findViewById<ImageView>(R.id.imageView2)
        val title = memoItem?.findViewById<TextView>(R.id.textView)
        val desc = memoItem?.findViewById<TextView>(R.id.textView3)

        fun bind (Items : RecyclerItem) {
            if(Items.getThumbnail() != null) {
                var array = Items.getThumbnail()
                var bitmap = BitmapFactory.decodeByteArray(array, 0, array?.size!!)

                image.setImageBitmap(bitmap)
            }
            else {
                image.setImageBitmap(null)
            }

            title.text = Items.getTitle()
            desc.text = Items.getDesc()

            // 리스트뷰 클릭 시 리스너 추가.
            memoItem.setOnClickListener{ it ->
                itemClick(Items, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotePadViewHolder {
        var memoview = LayoutInflater.from(parent.context).inflate(R.layout.notepad_item, parent, false)
        return NotePadViewHolder(memoview, itemClick)
    }

    override fun onBindViewHolder(holder: NotePadViewHolder, position: Int) {
        holder.bind(updateMemolist[position])
    }

    override fun getItemCount(): Int {
        return updateMemolist.size
    }
}