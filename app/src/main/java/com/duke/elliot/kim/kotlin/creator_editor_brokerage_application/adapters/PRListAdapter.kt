package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.R
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.activities.MainActivity
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.fragments.*
import com.duke.elliot.kim.kotlin.creator_editor_brokerage_application.model.PRModel
import com.facebook.drawee.view.SimpleDraweeView
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.item_view_pr.view.*


class PRListAdapter(private val activity: MainActivity,
                    private val containerViewId: Int,
                    private val prList: MutableList<PRModel>) : RecyclerView.Adapter<PRListAdapter.ViewHolder>() {

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PRListAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view_pr, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return prList.size
    }

    override fun onBindViewHolder(holder: PRListAdapter.ViewHolder, position: Int) {
        val pr = prList[position]
        holder.view.text_view_title.text = pr.title
        // holder.view.text_view_publisher = prList[position].get
        @Suppress("UNCHECKED_CAST")
        loadImage(holder.view.simple_drawee_view_pr,
            pr.userId, pr.imageNames as List<String?>)

        holder.view.setOnClickListener {
            activity.startFragment(PRFragment(pr), containerViewId, MainActivity.PR_FRAGMENT_TAG)
        }
    }

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

    fun insert(pr: PRModel) {
        prList.add(0, pr)
        notifyItemInserted(0)
    }

    fun update(pr: PRModel) {
        notifyItemChanged(getPosition(pr))
    }

    fun delete(pr: PRModel) {
        prList.remove(pr)
        notifyItemRemoved(getPosition(pr))
    }

    private fun getPosition(pr: PRModel) = prList.indexOf(pr)

    companion object {
        const val TAG = "RecyclerViewAdapter"
    }
}