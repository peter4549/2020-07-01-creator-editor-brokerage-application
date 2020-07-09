package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters

import android.view.GestureDetector.SimpleOnGestureListener
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.IMAGES
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.IMAGE_NAMES
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.TITLE
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.USER_ID
import com.facebook.drawee.view.SimpleDraweeView
import com.google.firebase.storage.FirebaseStorage
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
        val pr = prList[position]
        holder.view.text_view_title.text = pr.getValue(TITLE) as String
        //holder.view.text_view_publisher = prList[position].get
        @Suppress("UNCHECKED_CAST")
        loadImage(holder.view.simple_drawee_view_pr,
            pr.getValue(USER_ID) as String, pr.getValue(IMAGE_NAMES) as List<String?>)

    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    private fun loadImage(simpleDraweeView: SimpleDraweeView, userId: String, imageNames: List<String?>) {
        val storageReference = FirebaseStorage.getInstance().reference

        if (imageNames[0] != null)
            storageReference.child(IMAGES)
                .child(userId).child(imageNames[0]!!).downloadUrl
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        simpleDraweeView.setImageURI(task.result.toString())
                    } else {
                        println("$TAG: ${task.exception}")
                    }
                }
    }

    companion object {
        const val TAG = "RecyclerViewAdapter"
    }
}