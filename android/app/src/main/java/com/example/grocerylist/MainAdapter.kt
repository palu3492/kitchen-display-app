package com.example.grocerylist

import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.view.MotionEventCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.video_row.view.*
import okhttp3.*
import java.io.IOException


//class MainAdapter(val groceryListFeed: GroceryListFeed): RecyclerView.Adapter<CustomViewHolder>() {
class MainAdapter(var groceryListFeed: GroceryListFeed): RecyclerView.Adapter<CustomViewHolder>() {

//    val buttonIds = HashMap<Button, Int>()

    fun setFeed(feed: GroceryListFeed){
        groceryListFeed = feed
        println("updated feed")
    }

    override fun getItemCount(): Int {
        return groceryListFeed.items.count()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val cellForRow = layoutInflater.inflate(R.layout.video_row, parent, false)
        return CustomViewHolder(cellForRow)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {

        val item = groceryListFeed.items[position]
        if (item.title != "") {
            holder.view.textView_item_title.setText(item.title)
        }
        if (item.color != "") {
            holder.view.imageView_item_color.setBackgroundColor(Color.parseColor(item.color))
        }

        holder.view.textView_item_id.text = item.id.toString()

//        val button = holder.view.button_delete_item
//
//        buttonIds[button] = item.id
//
//        button.setOnClickListener {v ->
//
//            val id = buttonIds[v]
//
//            // update locally
//            var items = groceryListFeed.items
//            for(i in 0..items.size-1){
//                val _item = items.get(i);
//                println(_item.id)
//                if(_item.id == id){
//                    items.removeAt(i)
//                    groceryListFeed = GroceryListFeed(items)
//                    this.setFeed(groceryListFeed)
//                    this.notifyDataSetChanged()
//                    break
//                }
//            }
//
//            // update server
//            println("Deleting item")
//            val url = "https://list-display-app.herokuapp.com/items"
//            val formEncoded = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8")
//            val string = "id=" + id.toString()
//            val body = RequestBody.create(formEncoded, string)
//            val request = Request.Builder().url(url).delete(body).build()
//            val client = OkHttpClient()
//            client.newCall(request).enqueue(object: Callback {
//                override fun onResponse(call: Call, response: Response) {
//                    println("delete response")
//                }
//                override fun onFailure(call: Call, e: IOException) {
//                    println("Failed to execute delete request")
//                }
//            })
//        }

    }

}

class CustomViewHolder(val view: View): RecyclerView.ViewHolder(view)
