package com.example.habita.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {
    @Query("SELECT * FROM listings")
    fun getAllListings(): Flow<List<Listing>>

    @Query("SELECT * FROM listings WHERE isSample = 1")
    fun getSampleListings(): Flow<List<Listing>>

    @Query("SELECT * FROM listings WHERE isSample = 0 AND providerId = :providerId")
    fun getProviderListings(providerId: String): Flow<List<Listing>>

    @Query("SELECT * FROM listings WHERE isSaved = 1 AND isSample = 1")
    fun getSavedListings(): Flow<List<Listing>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(listings: List<Listing>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: Listing)

    @Update
    suspend fun updateListing(listing: Listing)

    @Query("SELECT * FROM listings WHERE id = :id LIMIT 1")
    suspend fun getListingById(id: Int): Listing?

    @Query("SELECT * FROM listings WHERE title = :title LIMIT 1")
    suspend fun getListingByTitle(title: String): Listing?

    @Query("SELECT * FROM listings WHERE location = :location AND houseType = :type AND price BETWEEN :minPrice AND :maxPrice")
    fun filterListings(location: String, type: String, minPrice: Int, maxPrice: Int): Flow<List<Listing>>
}