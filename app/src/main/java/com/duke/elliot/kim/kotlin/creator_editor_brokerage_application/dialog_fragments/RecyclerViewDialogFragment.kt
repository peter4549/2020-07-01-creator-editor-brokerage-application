package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.dialog_fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters.LayoutManagerWrapper
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.PrListFragment
import kotlinx.android.synthetic.main.fragment_recycler_view_dialog.view.*

interface OnSetRecyclerView {
    fun setRecyclerView(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>)
}

interface OnSetTitle {
    fun setTitle(text: String)
}

open class RecyclerViewDialogFragment: DialogFragment(), OnSetTitle, OnSetRecyclerView {

    private lateinit var recyclerView: RecyclerView
    private lateinit var textViewTitle: TextView

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_recycler_view_dialog, null)
        builder.setView(view)

        recyclerView = view.findViewById(R.id.recycler_view)
        textViewTitle = view.findViewById(R.id.text_view_title)

        return builder.create()
    }

    override fun setRecyclerView(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
        recyclerView.apply {
            this.adapter = adapter
            layoutManager = LayoutManagerWrapper(requireContext(), 1)
            scheduleLayoutAnimation()
        }
    }

    override fun setTitle(text: String) {
        textViewTitle.visibility = View.VISIBLE
        textViewTitle.text = text
    }

}