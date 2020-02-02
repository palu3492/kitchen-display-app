package com.example.grocerylist

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.video_row.view.*
import okhttp3.*
import java.io.IOException


//class MainAdapter(val groceryListFeed: GroceryListFeed): RecyclerView.Adapter<CustomViewHolder>() {
class MainAdapter(var groceryListFeed: GroceryListFeed): RecyclerView.Adapter<CustomViewHolder>() {
// val groceryListFeed: GroceryListFeed
//    val videoTitles = listOf("first", "second", "third")
    val buttonIds = HashMap<Button, Int>()

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
        if (item.title != "") {
            holder.view.textView_item_title.setText(item.title)
        }
        if (item.color != "") {
            holder.view.imageView_item_color.setBackgroundColor(Color.parseColor(item.color))
        }
        val button = holder.view.button_delete_item

        buttonIds[button] = item.id

        button.setOnClickListener {v ->
            println("Deleting item")
            val url = "https://list-display-app.herokuapp.com/items"
            val formEncoded = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8")
            val string = "id=" + buttonIds[v]
            val body = RequestBody.create(formEncoded, string)
            val request = Request.Builder().url(url).delete(body).build()
            val client = OkHttpClient()
            client.newCall(request).enqueue(object: Callback {
                override fun onResponse(call: Call, response: Response) {
                    println("response")
                }
                override fun onFailure(call: Call, e: IOException) {
                    println("Failed to execute request")
                }
            })
        }

    }

}

class CustomViewHolder(val view: View): RecyclerView.ViewHolder(view)
