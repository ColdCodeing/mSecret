package com.mm.secretapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.mm.secretapp.R
import com.mm.secretapp.dto.UserPassItem
import com.yanzhenjie.recyclerview.swipe.SwipeMenuAdapter
import java.text.SimpleDateFormat
import java.util.*

class UpassItemAdapter : SwipeMenuAdapter<UpassItemAdapter.ViewHolder> {
    var context: Context
    var datas: List<UserPassItem> = ArrayList<UserPassItem>()
    lateinit var onItemClickListener: (position: Int, data: UserPassItem) -> Unit

    constructor(context: Context, datas: List<UserPassItem>) : super() {
        this.context = context
        this.datas = datas
    }

    fun notifyDataChanged(datas: List<UserPassItem>) {
        this.datas = datas
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onCompatCreateViewHolder(realContentView: View, viewType: Int): ViewHolder {
        return ViewHolder(realContentView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val userPassItem = datas.get(position)
        viewHolder.renderItem(position, userPassItem)
        viewHolder.itemView.setOnClickListener {
            onItemClickListener(position, userPassItem)
        }
    }

    override fun onCreateContentView(parent: ViewGroup?, viewType: Int): View {
        return LayoutInflater.from(context).inflate(R.layout.item_upass, parent, false)
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, data: UserPassItem)
    }

    class ViewHolder : RecyclerView.ViewHolder {
        var iconIv: ImageView
        var nameTv: TextView
        var  timeTv: TextView
        var descTv: TextView
        @SuppressLint("SimpleDateFormat")
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        constructor(view: View) : super(view) {
            iconIv = view.findViewById(R.id.iv_icon)
            nameTv = view.findViewById(R.id.tv_name)
            timeTv = view.findViewById(R.id.tv_time)
            descTv = view.findViewById(R.id.tv_describe)
        }

        fun renderItem(position: Int, userPassItem: UserPassItem) {
            nameTv.setText(userPassItem.data.getString("title"))
            timeTv.setText(simpleDateFormat.format(Date(userPassItem.updatedTime)))
            descTv.setText(userPassItem.data.getString("desc"))
        }
    }

}