package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.dialog_fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R

interface OnButtonClickListeners {
    fun setTitle(text: String)
    fun setFirstButton(text: String, drawableResourceId: Int?, onClickListener: View.OnClickListener?)
    fun setSecondButton(text: String, drawableResourceId: Int?, onClickListener: View.OnClickListener?)
    fun setThirdButton(text: String, drawableResourceId: Int?, onClickListener: View.OnClickListener?)
}

open class ThreeButtonsDialogFragment: DialogFragment(), OnButtonClickListeners {

    private lateinit var textViewTitle: TextView
    private lateinit var buttonFirst: Button
    private lateinit var buttonSecond: Button
    private lateinit var buttonThird: Button

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())

        dialog.setContentView(R.layout.dialog_fragment_three_vertical_buttons)

        buttonFirst = dialog.findViewById(R.id.button_first)
        buttonSecond = dialog.findViewById(R.id.button_second)
        buttonThird = dialog.findViewById(R.id.button_third)
        textViewTitle = dialog.findViewById(R.id.text_view_title)

        return dialog
    }

    override fun setTitle(text: String) {
        textViewTitle.text = text
    }

    override fun setFirstButton(text: String, drawableResourceId: Int?, onClickListener: View.OnClickListener?) {
        buttonFirst.text = text
        buttonFirst.setOnClickListener(onClickListener)

        if (drawableResourceId != null)
            buttonFirst.setCompoundDrawablesWithIntrinsicBounds(drawableResourceId, 0, 0, 0)
    }

    override fun setSecondButton(text: String, drawableResourceId: Int?, onClickListener: View.OnClickListener?) {
        buttonSecond.text = text
        buttonSecond.setOnClickListener(onClickListener)

        if (drawableResourceId != null)
            buttonSecond.setCompoundDrawablesWithIntrinsicBounds(drawableResourceId, 0, 0, 0)
    }

    override fun setThirdButton(text: String, drawableResourceId: Int?, onClickListener: View.OnClickListener?) {
        buttonThird.text = text
        buttonThird.setOnClickListener(onClickListener)

        if (drawableResourceId != null)
            buttonThird.setCompoundDrawablesWithIntrinsicBounds(drawableResourceId, 0, 0, 0)
    }
}