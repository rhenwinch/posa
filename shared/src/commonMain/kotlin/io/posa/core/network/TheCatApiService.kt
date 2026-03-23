package io.posa.core.network

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import io.posa.core.common.Config.DEFAULT_PAGE_SIZE
import io.posa.core.network.dto.CatFavouriteDto
import io.posa.core.network.dto.CatBreedDto
import io.posa.core.network.dto.CatImageDto
import io.posa.core.network.dto.CommonResponseDto
import io.posa.core.network.dto.FavouriteRequestDto
import kotlinx.coroutines.flow.Flow

interface TheCatApiService {
    companion object {
        private const val DEFAULT_SIZE = "med"
        private const val DEFAULT_FORMAT = "json"
        private const val DEFAULT_HAS_BREEDS = true
    }

    @GET("images/search")
    suspend fun getCatImages(
        @Query page: Int,
        @Query("has_breeds") hasBreeds: Boolean = DEFAULT_HAS_BREEDS,
        @Query size: String = DEFAULT_SIZE,
        @Query format: String = DEFAULT_FORMAT,
        @Query order: String = "RANDOM",
        @Query limit: Int = DEFAULT_PAGE_SIZE
    ): List<CatImageDto>

    @GET("images/{image_id}")
    suspend fun getCatImage(
        @Path("image_id") id: String
    ): CatImageDto

    @GET("breeds")
    suspend fun getBreeds(
        @Query page: Int,
        @Query order: String = "RANDOM",
        @Query limit: Int = DEFAULT_PAGE_SIZE
    ): List<CatBreedDto>

    @GET("breeds/search")
    suspend fun searchBreeds(
        @Query("q") query: String,
        @Query("attach_image") attachImage: Boolean = true,
    ): List<CatBreedDto>

    @GET("breeds/{breed_id}")
    suspend fun getBreed(
        @Path("breed_id") id: String
    ): CatBreedDto

    @GET("favourites")
    fun getFavourites(
        @Query("sub_id") userId: String,
        @Query page: Int,
        @Query limit: Int = DEFAULT_PAGE_SIZE,
        @Query order: String = "DESC"
    ): Flow<List<CatFavouriteDto>>

    @Headers("Content-type: application/json")
    @POST("favourites")
    suspend fun addFavourite(
        @Body data: FavouriteRequestDto
    ): CommonResponseDto

    @DELETE("favourites/{favourite_id}")
    suspend fun removeFavourite(
        @Path("favourite_id") id: Long
    ): CommonResponseDto
}