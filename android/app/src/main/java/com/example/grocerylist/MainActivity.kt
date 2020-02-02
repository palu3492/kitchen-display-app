package com.example.grocerylist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        recyclerView_main.setBackgroundColor(Color.BLUE)

        recyclerView_main.layoutManager = LinearLayoutManager(this)
//        recyclerView_main.adapter = MainAdapter()

        fetchJson()

        addNewItemButton()

        recyclerView_main.setOnClickListener { v ->
            println(v)
        }
    }

    fun fetchJson(){
        println("Fetching JSON")

        val url = "wss://list-display-app.herokuapp.com/items"

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        // feed
        val emptyList = listOf<Item>()
        val feed = GroceryListFeed(emptyList)

        val adapter = MainAdapter(feed)
        recyclerView_main.adapter = adapter

        val wsListener = MyWebSocketListener(adapter, this)
        client.newWebSocket(request, wsListener)

//        client.newCall(request).enqueue(object: Callback {
//            override fun onResponse(call: Call, response: Response) {
//                val body = response.body()?.string()
//                val gson = GsonBuilder().create()
//                val homeFeed = gson.fromJson(body, GroceryListFeed::class.java)
//                runOnUiThread {
//                    recyclerView_main.adapter = MainAdapter(homeFeed)
//                }
//            }
//            override fun onFailure(call: Call, e: IOException) {
//                println("Failed to execute request")
//            }
//        })

    }

    fun addNewItemButton(){
        button_add_item.setOnClickListener{
            val newItemTitle = editText_new_item.text.toString()
            if(newItemTitle.count() > 1) {
                println("Adding item: "+newItemTitle)
                editText_new_item.setText("")
                val url = "https://list-display-app.herokuapp.com/items"
                val formEncoded = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8")
                val string = "title=" + newItemTitle + "&color="
                println(string)
                val body = RequestBody.create(formEncoded, string)
                val request = Request.Builder().url(url).put(body).build()
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

}

class MyWebSocketListener(val adapter: MainAdapter, val mainActivity: MainActivity): WebSocketListener(){
//    val mainAdapter = adapter
    override fun onOpen(webSocket: WebSocket, response: Response) {
        println("connected")
    }

    val gson = GsonBuilder().create()

    override fun onMessage(webSocket: WebSocket, text: String) {

//        println(text)
        val items = gson.fromJson(text, Array<Item>::class.java).toList()
        val groceryListFeed = GroceryListFeed(items)
//        println(groceryListFeed)
//        println("--")
//        val groceryListFeed = Gson().fromJson(text, GroceryListFeed::class.java)

//        val i0 = Item(1, "orange", 1, "red")
//        val i1 = Item(1, "apple", 1, "red")
//        val a = listOf<Item>(i0, i1)
//        val g = GroceryListFeed(a)
        mainActivity.runOnUiThread {
            adapter.setFeed(groceryListFeed)
            adapter.notifyDataSetChanged()
        }

    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        println("WebSocket connection closed")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        println("WebSocket connection closing")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        println("WebSocket fail")
        println("Error : " + t.message)
    }
}

data class GroceryListFeed(val items: List<Item>)

data class Item(val id: Int = 0, val title: String = "", val active: Int = 0, val color: String = "")
