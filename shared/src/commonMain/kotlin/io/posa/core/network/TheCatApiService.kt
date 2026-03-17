package io.pusa.network

import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import io.posa.core.common.Config.DEFAULT_PAGE_SIZE
import io.pusa.network.dto.CatFavouriteDto
import io.pusa.network.dto.CatBreedDto
import io.pusa.network.dto.CatImageDto
import io.pusa.network.dto.CommonResponseDto

interface TheCatApiService {
    companion object {
        private const val DEFAULT_SIZE = "med"
        private const val DEFAULT_FORMAT = "json"
        private const val DEFAULT_HAS_BREEDS = true
    }

    @GET("v1/images/search")
    suspend fun getCatImages(
        @Query page: Int,
        @Query("has_breeds") hasBreeds: Boolean = DEFAULT_HAS_BREEDS,
        @Query size: String = DEFAULT_SIZE,
        @Query format: String = DEFAULT_FORMAT,
        @Query order: String = "RANDOM",
        @Query limit: Int = DEFAULT_PAGE_SIZE
    ): List<CatImageDto>

    @GET("v1/images/{image_id}")
    suspend fun getCatImage(
        @Path("image_id") id: String
    ): CatImageDto

    @GET("v1/breeds")
    suspend fun getBreeds(
        @Query page: Int,
        @Query order: String = "RANDOM",
        @Query limit: Int = DEFAULT_PAGE_SIZE
    ): List<CatBreedDto>

    @GET("v1/breeds/search")
    suspend fun searchBreeds(
        @Query("q") query: String,
        @Query("attach_image") attachImage: Boolean = true,
    ): List<CatBreedDto>

    @GET("v1/breeds/{breed_id}")
    suspend fun getBreed(
        @Path("breed_id") id: String
    ): CatBreedDto

    @GET("v1/favourites")
    suspend fun getFavourites(
        @Query("sub_id") userId: String,
        @Query limit: Int = DEFAULT_PAGE_SIZE,
        @Query order: String = "DESC"
    ): List<CatFavouriteDto>

    @POST("v1/favourites")
    suspend fun addFavourite(
        @Query("image_id") imageId: String,
        @Query("sub_id") userId: String
    ): CommonResponseDto

    @DELETE("v1/favourites/{favourite_id}")
    suspend fun removeFavourite(
        @Path("favourite_id") id: Long
    ): CommonResponseDto
}