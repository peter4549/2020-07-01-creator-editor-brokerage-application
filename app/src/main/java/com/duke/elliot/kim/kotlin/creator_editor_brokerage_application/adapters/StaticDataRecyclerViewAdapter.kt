package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

open class StaticDataRecyclerViewAdapter<T: Any?>(private val items: ArrayList<T>,
                                         private val layoutId: Int):
    RecyclerView.Adapter<StaticDataRecyclerViewAdapter<T>.ViewHolder>() {

    private lateinit var recyclerView: RecyclerView

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.count()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    }

    @Suppress("unused")
    fun insert(item: T, position: Int = 0) {
        items.add(position, item)
        notifyItemInserted(position)
    }

    @Suppress("unused")
    fun update(item: T) {
        notifyItemChanged(items.indexOf(item))
    }

    @Suppress("unused")
    fun update(position: Int) {
        notifyItemChanged(position)
    }

    @Suppress("unused")
    fun remove(item: T) {
        val position = items.indexOf(item)
        items.remove(item)
        notifyItemRemoved(position)
    }

    @Suppress("unused")
    fun delete(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }
}