package com.example.habita.database

import kotlinx.coroutines.flow.Flow

class ListingRepository(private val listingDao: ListingDao) {
    val allListings: Flow<List<Listing>> = listingDao.getAllListings()
    val savedListings: Flow<List<Listing>> = listingDao.getSavedListings()

    suspend fun insertAll(listings: List<Listing>) {
        listingDao.insertAll(listings)
    }

    suspend fun updateListing(listing: Listing) {
        listingDao.updateListing(listing)
    }

    fun filterListings(location: String, type: String, minPrice: Int, maxPrice: Int): Flow<List<Listing>> {
        return listingDao.filterListings(location, type, minPrice, maxPrice)
    }
}