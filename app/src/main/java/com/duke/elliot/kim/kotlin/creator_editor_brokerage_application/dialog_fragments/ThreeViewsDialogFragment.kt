package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.dialog_fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import kotlinx.android.synthetic.main.fragment_recycler_view_dialog.view.*

interface OnSetViews {
    fun setTitle(text: String)
    fun setFirstButton(text: String, drawableResourceId: Int?, onClickListener: View.OnClickListener?)
    fun setSecondButton(text: String, drawableResourceId: Int?, onClickListener: View.OnClickListener?)
    fun setThirdButton(text: String, drawableResourceId: Int?, onClickListener: View.OnClickListener?)
}

open class ThreeViewsDialogFragment: DialogFragment(), OnSetViews {

    private lateinit var textViewTitle: TextView
    private lateinit var buttonFirst: Button
    private lateinit var buttonSecond: Button
    private lateinit var buttonThird: Button

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_fragment_three_vertical_buttons, null)
        builder.setView(view)

        buttonFirst = view.findViewById(R.id.button_first)
        buttonSecond = view.findViewById(R.id.button_second)
        buttonThird = view.findViewById(R.id.button_third)
        textViewTitle = view.findViewById(R.id.text_view_title)

        return builder.create()
    }

    override fun setTitle(text: String) {
        textViewTitle.visibility = View.VISIBLE
        textViewTitle.text = text
    }

    override fun setFirstButton(text: String, drawableResourceId: Int?, onClickListener: View.OnClickListener?) {
        buttonFirst.visibility = View.VISIBLE
        buttonFirst.text = text
        buttonFirst.setOnClickListener(onClickListener)

        if (drawableResourceId != null)
            buttonFirst.setCompoundDrawablesWithIntrinsicBounds(drawableResourceId, 0, 0, 0)
    }

    override fun setSecondButton(text: String, drawableResourceId: Int?, onClickListener: View.OnClickListener?) {
        buttonSecond.visibility = View.VISIBLE
        buttonSecond.text = text
        buttonSecond.setOnClickListener(onClickListener)

        if (drawableResourceId != null)
            buttonSecond.setCompoundDrawablesWithIntrinsicBounds(drawableResourceId, 0, 0, 0)
    }

    override fun setThirdButton(text: String, drawableResourceId: Int?, onClickListener: View.OnClickListener?) {
        buttonThird.visibility = View.VISIBLE
        buttonThird.text = text
        buttonThird.setOnClickListener(onClickListener)

        if (drawableResourceId != null)
            buttonThird.setCompoundDrawablesWithIntrinsicBounds(drawableResourceId, 0, 0, 0)
    }
}