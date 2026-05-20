package com.example.habita.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.habita.R
import com.example.habita.database.Booking

class BookingAdapter(
    private val bookings: List<Booking>
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtBookingTitle: TextView = itemView.findViewById(R.id.txtBookingTitle)
        val txtBookingRef: TextView = itemView.findViewById(R.id.txtBookingRef)
        val txtBookingStatus: TextView = itemView.findViewById(R.id.txtBookingStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        holder.txtBookingTitle.text = booking.title
        holder.txtBookingRef.text = "Reference: ${booking.reference}"
        holder.txtBookingStatus.text = "Status: ${booking.status}"
    }

    override fun getItemCount(): Int = bookings.size
}