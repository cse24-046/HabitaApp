package com.example.habita.database

object BookingManager {

    private val bookingList = mutableListOf<Booking>()

    fun addBooking(booking: Booking) {
        bookingList.add(booking)
    }

    fun getBookings(): List<Booking> {
        return bookingList
    }
}