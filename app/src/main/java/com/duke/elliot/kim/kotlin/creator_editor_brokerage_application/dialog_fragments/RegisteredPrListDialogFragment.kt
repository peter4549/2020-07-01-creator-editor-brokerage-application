package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.dialog_fragments

import android.app.Dialog
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.PrListFragment

class RegisteredPrListDialogFragment: RecyclerViewDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        val adapter =
            PrListFragment().PrListRecyclerViewAdapter((requireActivity() as MainActivity),
                true, dialog)

        @Suppress("UNCHECKED_CAST")
        super.setRecyclerView(adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>)

        return dialog
    }
}