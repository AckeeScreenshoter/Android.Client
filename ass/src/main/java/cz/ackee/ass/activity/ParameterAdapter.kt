package cz.ackee.ass.activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cz.ackee.ass.R

/**
 * Displays a list both statically provided parameters (app name, app version, os version, etc.)
 * and user-provided parameters.
 */
internal class ParameterAdapter(
    private val data: List<Pair<String, Any>>
) : RecyclerView.Adapter<ParameterAdapter.MyViewHolder>() {

    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.ass_feedback_layout_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val (key, value) = data[position]
        holder.txtTitle.text = key
        holder.txtValue.text = value.toString()
    }

    class MyViewHolder(layout: View) : RecyclerView.ViewHolder(layout) {
        val txtTitle: TextView = layout.findViewById(R.id.ass_txt_title)
        val txtValue: TextView = layout.findViewById(R.id.ass_txt_value)
    }
}
