package com.example.grocerylist

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.video_row.view.*
import okhttp3.*
import org.w3c.dom.Text
import java.io.IOException

// feed
val emptyList = mutableListOf<Item>()
var groceryListFeed = GroceryListFeed(emptyList)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()?.hide(); //<< this
        setContentView(R.layout.activity_main)

        recyclerView_main.layoutManager = LinearLayoutManager(this)
//        recyclerView_main.adapter = MainAdapter()

        val adapter = MainAdapter(groceryListFeed)
        recyclerView_main.adapter = adapter

        setUpWebSocket(adapter)

        addSwipeDelete(adapter)

        setupNewItemButton(adapter)

        setUpSpinner()

        setUpActionButton()

        button_cancel.setOnClickListener {
            constraintLayout_add_item.visibility = View.INVISIBLE
        }

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

    var color = ""

    fun setupNewItemButton(adapter: MainAdapter){
        button_add_item.setOnClickListener{
            val newItemTitle = editText_new_item.text.toString()
            if(newItemTitle.count() > 1) {
                constraintLayout_add_item.visibility = View.INVISIBLE
                println("Adding item: "+newItemTitle)
                editText_new_item.setText("")
                // update locally
                var items = groceryListFeed.items
                items.add(Item(1, newItemTitle, 1, color))
                groceryListFeed = GroceryListFeed(items)
                adapter.setFeed(groceryListFeed)
                adapter.notifyDataSetChanged()

                // update on server
                val url = "https://list-display-app.herokuapp.com/items"
                val formEncoded = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8")
                val string = "title=" + newItemTitle + "&color="+color
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

    fun setUpSpinner(){
        val spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.names, android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_names.adapter = spinnerAdapter
        spinner_names.onItemSelectedListener = object: AdapterView.OnItemClickListener,
            AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, p3: Long) {
                val name = parent?.getItemAtPosition(position).toString()
                if(name == "Alex"){
                    color = "red"
                } else if(name == "Aaron"){
                    color = "blue"
                } else if(name == "Tony"){
                    color = "green"
                }
                color = when(name) {
                    "Alex" -> "red"
                    "Aaron" -> "blue"
                    "Tony" -> "green"
                    else -> ""
                }
                println(color)
            }

            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

            }

        }
    }

    fun setUpActionButton(){
        floatingActionButton_add_item.setOnClickListener{
            floatingActionButton_add_item.show()
            constraintLayout_add_item.visibility = View.VISIBLE
        }
    }

    fun addSwipeDelete(adapter: MainAdapter){
//        val swipeHandler = object : SwipeToDeleteCallback(context) {
//            override fun onSwiped(...) {
//                val adapter = recyclerView.adapter as SimpleAdapter
//                adapter.removeAt(viewHolder.adapterPosition)
//            }
//        }
        val swipeHandler = object: SwipeToDeleteCallback(adapter){}
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView_main)
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

abstract class SwipeToDeleteCallback(val adapter: MainAdapter) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

//    private val deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_white_24)
//    private val intrinsicWidth = deleteIcon.intrinsicWidth
//    private val intrinsicHeight = deleteIcon.intrinsicHeight
    private val background = ColorDrawable()
    private val backgroundColor = Color.parseColor("#f44336")
//    private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        println("onMove")
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val itemView = viewHolder.itemView
        val id = itemView.textView_item_id.text.toString().toInt()
        var items = groceryListFeed.items
        for(i in 0..items.size-1){
            val _item = items.get(i);
            if(_item.id == id){
                items.removeAt(i)
                groceryListFeed = GroceryListFeed(items)
                println(groceryListFeed)
                adapter.setFeed(groceryListFeed)
                adapter.notifyDataSetChanged()
                break
            }
        }

        //update server
        println("Deleting item")
        val url = "https://list-display-app.herokuapp.com/items"
        val formEncoded = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8")
        val string = "id=" + id.toString()
        val body = RequestBody.create(formEncoded, string)
        val request = Request.Builder().url(url).delete(body).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                println("delete response")
            }
            override fun onFailure(call: Call, e: IOException) {
                println("Failed to execute delete request")
            }
        })

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

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
//        val itemHeight = itemView.bottom - itemView.top
//        val isCanceled = dX == 0f && !isCurrentlyActive
//
//        if (isCanceled) {
//            clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
//            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
//            return
//        }

        // Draw the red delete background
        background.color = backgroundColor
        background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
        background.draw(c)

        // Calculate position of delete icon
//        val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
//        val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
//        val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
//        val deleteIconRight = itemView.right - deleteIconMargin
//        val deleteIconBottom = deleteIconTop + intrinsicHeight

        // Draw the delete icon
//        deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
//        deleteIcon.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

}