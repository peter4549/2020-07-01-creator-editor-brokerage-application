package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.PUBLIC_NAME
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.TITLE
import kotlinx.android.synthetic.main.card_view_pr.view.*

class RecyclerViewAdapter(private val prList: List<Map<String, Any>>) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_view_pr, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return prList.size
    }

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        holder.view.text_view_title.text = prList[position].getValue(TITLE) as String
        //holder.view.text_view_publisher = prList[position].get
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}