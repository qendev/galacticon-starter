/*
 * Copyright (c) 2017 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.galacticon

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity(), ImageRequester.ImageRequesterResponse {

  private var photosList: ArrayList<Photo> = ArrayList()
  private lateinit var imageRequester: ImageRequester

  private lateinit var linearLayoutManager: LinearLayoutManager
  private lateinit var adapter: RecyclerAdapter

  private lateinit var gridLayoutManager: GridLayoutManager

  private val lastVisibleItemPosition: Int
    get() = if (recyclerView.layoutManager == linearLayoutManager) {
      linearLayoutManager.findLastVisibleItemPosition()
    } else {
      gridLayoutManager.findLastVisibleItemPosition()
    }









  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }



  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)



    linearLayoutManager = LinearLayoutManager(this)
    recyclerView.layoutManager = linearLayoutManager

    gridLayoutManager = GridLayoutManager(this, 2)
    recyclerView.layoutManager=gridLayoutManager



    adapter = RecyclerAdapter(photosList)
    recyclerView.adapter = adapter

    setRecyclerViewScrollListener()
    setRecyclerViewItemTouchListener()




    imageRequester = ImageRequester(this)
  }

  override fun onStart() {
    super.onStart()
    //to make sure you don’t have an empty screen.
    if (photosList.size == 0) {
      requestPhoto()
    }

  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == R.id.action_change_recycler_manager) {
      changeLayoutManager()
      return true
    }
    return super.onOptionsItemSelected(item)
  }


  private fun requestPhoto() {
    try {
      imageRequester.getPhoto()
    } catch (e: IOException) {
      e.printStackTrace()
    }

  }

  override fun receivedNewPhoto(newPhoto: Photo) {
    runOnUiThread {
      photosList.add(newPhoto)
      // if your list is empty, and if yes, it requests a photo.
      //Here, you inform the recycler adapter that you added an item after updating the list of photos.
      adapter.notifyItemInserted(photosList.size-1)

    }
  }

  private fun setRecyclerViewScrollListener() {
    recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        val totalItemCount = recyclerView.layoutManager!!.itemCount
        if (!imageRequester.isLoadingData && totalItemCount == lastVisibleItemPosition + 1) {
          requestPhoto()
        }
      }
    })
  }

  private fun changeLayoutManager() {
    if (recyclerView.layoutManager == linearLayoutManager) {
      //1 If it’s using the LinearLayoutManager, it swaps in the GridLayoutManager.
      recyclerView.layoutManager = gridLayoutManager
      //2 It requests a new photo if your grid layout only has one photo to show.
      if (photosList.size == 1) {
        requestPhoto()
      }
    } else {
      //3 If it’s using the GridLayoutManager, it swaps in the LinearLayoutManager.
      recyclerView.layoutManager = linearLayoutManager
    }
  }

  private fun setRecyclerViewItemTouchListener() {

    //1 Create the callback and tell it what events to listen for.
    // It takes two parameters: One for drag directions and one for swipe directions.
    // You’re only interested in swipe. Pass 0 to inform the callback not to respond to drag events.
    val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
      override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, viewHolder1: RecyclerView.ViewHolder): Boolean {
        //2 Return false in onMove. You don’t want to perform any special behavior here.
        return false
      }
      //3 Call onSwiped when you swipe an item in the direction specified in the ItemTouchHelper.
      // Here, you request the viewHolder parameter passed for the position of the item view, and then you remove that item from your list of photos.
      // Finally, you inform the RecyclerView adapter that an item has been removed at a specific position.
      override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
        val position = viewHolder.adapterPosition
        photosList.removeAt(position)
        recyclerView.adapter!!.notifyItemRemoved(position)
      }
    }

    //4Initialize ItemTouchHelper with the callback behavior you defined, and then attach it to the RecyclerView.
    val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
    itemTouchHelper.attachToRecyclerView(recyclerView)
  }





}
