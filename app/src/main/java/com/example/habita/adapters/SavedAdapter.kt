package com.example.habita.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.habita.R
import com.example.habita.database.Listing

class SavedAdapter(
    private val items: List<Listing>,
    private val onItemClick: (Listing) -> Unit
) : RecyclerView.Adapter<SavedAdapter.SavedViewHolder>() {

    class SavedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTitle: TextView = itemView.findViewById(R.id.txtBookingTitle)
        val txtRef: TextView = itemView.findViewById(R.id.txtBookingRef)
        val txtStatus: TextView = itemView.findViewById(R.id.txtBookingStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
        return SavedViewHolder(view)
    }

    override fun onBindViewHolder(holder: SavedViewHolder, position: Int) {
        val listing = items[position]
        holder.txtTitle.text = listing.title
        holder.txtRef.text = "Location: ${listing.location}"
        holder.txtStatus.text = "Saved"
        
        holder.itemView.setOnClickListener { onItemClick(listing) }
    }

    override fun getItemCount(): Int = items.size
}