package com.example.grocerylist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException

// feed
val emptyList = mutableListOf<Item>()
var groceryListFeed = GroceryListFeed(emptyList)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        recyclerView_main.setBackgroundColor(Color.BLUE)

        recyclerView_main.layoutManager = LinearLayoutManager(this)
//        recyclerView_main.adapter = MainAdapter()

        val adapter = MainAdapter(groceryListFeed)
        recyclerView_main.adapter = adapter

        setUpWebSocket(adapter)

        SetupNewItemButton(adapter)
    }

    fun setUpWebSocket(adapter: MainAdapter){
        println("Fetching JSON")

        val url = "wss://list-display-app.herokuapp.com/items"

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        val wsListener = MyWebSocketListener(adapter, this)
        val ws = client.newWebSocket(request, wsListener)

        // ping server every 20 seconds
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                ws.send("ping")
                println("ping")
                mainHandler.postDelayed(this, 20000)
            }
        })
    }

    fun SetupNewItemButton(adapter: MainAdapter){
        button_add_item.setOnClickListener{
            val newItemTitle = editText_new_item.text.toString()
            if(newItemTitle.count() > 1) {
                println("Adding item: "+newItemTitle)
                editText_new_item.setText("")
                // update locally
                var items = groceryListFeed.items
                items.add(Item(1, newItemTitle, 1, ""))
                groceryListFeed = GroceryListFeed(items)
                adapter.setFeed(groceryListFeed)
                adapter.notifyDataSetChanged()

                // update on server
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
        val items = gson.fromJson(text, Array<Item>::class.java).toMutableList()
        groceryListFeed = GroceryListFeed(items)
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

data class GroceryListFeed(val items: MutableList<Item>)

data class Item(val id: Int = 0, val title: String = "", val active: Int = 0, val color: String = "")
