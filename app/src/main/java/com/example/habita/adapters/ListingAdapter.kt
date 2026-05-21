package com.example.habita.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.habita.R
import com.example.habita.database.Listing

class ListingAdapter(
    private val listings: List<Listing>,
    private val onItemClick: (Listing) -> Unit,
    private val onFavoriteClick: (Listing) -> Unit
) : RecyclerView.Adapter<ListingAdapter.ListingViewHolder>() {

    class ListingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgListing: ImageView = itemView.findViewById(R.id.imgListing)
        val txtListingTitle: TextView = itemView.findViewById(R.id.txtListingTitle)
        val txtListingPrice: TextView = itemView.findViewById(R.id.txtListingPrice)
        val txtListingLocation: TextView = itemView.findViewById(R.id.txtListingLocation)
        val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavorite)
        val txtStatusBadge: TextView = itemView.findViewById(R.id.txtStatusBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_listing, parent, false)
        return ListingViewHolder(view)
    }

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        val listing = listings[position]

        holder.txtListingTitle.text = listing.title
        holder.txtListingPrice.text = "P${listing.price} / month"
        holder.txtListingLocation.text = listing.location
        
        // Using high quality/AI generated main image
        if (listing.mainImage != 0) {
            holder.imgListing.setImageResource(listing.mainImage)
        } else {
            holder.imgListing.setImageResource(R.mipmap.ic_launcher)
        }
        
        // Show RESERVED badge if booked
        if (listing.status == "RESERVED") {
            holder.txtStatusBadge.visibility = View.VISIBLE
            holder.itemView.alpha = 0.6f
        } else {
            holder.txtStatusBadge.visibility = View.GONE
            holder.itemView.alpha = 1.0f
        }

        // Toggle Star Icon
        holder.btnFavorite.setImageResource(
            if (listing.isSaved) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
        )

        holder.itemView.setOnClickListener {
            onItemClick(listing)
        }
        
        holder.btnFavorite.setOnClickListener {
            onFavoriteClick(listing)
        }
    }

    override fun getItemCount(): Int = listings.size
}