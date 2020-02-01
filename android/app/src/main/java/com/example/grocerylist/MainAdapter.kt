package com.example.grocerylist

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.video_row.view.*

//class MainAdapter(val groceryListFeed: GroceryListFeed): RecyclerView.Adapter<CustomViewHolder>() {
class MainAdapter(var groceryListFeed: GroceryListFeed): RecyclerView.Adapter<CustomViewHolder>() {
// val groceryListFeed: GroceryListFeed
//    val videoTitles = listOf("first", "second", "third")


    fun setFeed(feed: GroceryListFeed){
        groceryListFeed = feed
        println("updated feed")
    }

    override fun getItemCount(): Int {
        return groceryListFeed.items.count()
//        return 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.video_row, parent, false)
        return CustomViewHolder(cellForRow)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
//        println("here2")
//        println(groceryListFeed)
//        holder.view.textView_item_title.setText(groceryListJson)
        val item = groceryListFeed.items[position]
        holder.view.textView_item_title.setText(item.title)
        println(item)
        if(item.color != "") {
            holder.view.imageView_item_color.setBackgroundColor(Color.parseColor(item.color))
        }
    }


}

class CustomViewHolder(val view: View): RecyclerView.ViewHolder(view)
