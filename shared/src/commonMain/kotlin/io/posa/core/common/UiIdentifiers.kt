package io.posa.core.common

object UiIdentifiers {
    const val BREEDS_SCREEN = "breeds:screen"
    const val BREEDS_CONTENT = "breeds:content"
    const val BREEDS_DECK = "breeds:deck"
    const val BREEDS_LOADING = "breeds:loading"
    const val BREEDS_ERROR = "breeds:error"
    const val BREEDS_EMPTY_END = "breeds:empty:end"
    const val BREEDS_EMPTY_LOADING_MORE = "breeds:empty:loadingMore"
    const val BREEDS_TOP_BAR_FAVOURITES_BUTTON = "breeds:topBar:favouritesButton"
    const val BREEDS_OVERLAY_LIKE = "breeds:overlay:like"
    const val BREEDS_OVERLAY_NOPE = "breeds:overlay:nope"

    const val FAVOURITES_SCREEN = "favourites:screen"
    const val FAVOURITES_DETAIL_SHEET = "favourites:detailSheet"
    const val FAVOURITES_TOP_BAR_BACK_BUTTON = "favourites:topBar:backButton"
    const val FAVOURITES_GRID = "favourites:grid"
    const val FAVOURITES_SORT_ORDER = "favourites:sortOrder"
    const val FAVOURITES_SORT_ORDER_ASC = "favourites:sortOrder:asc"
    const val FAVOURITES_SORT_ORDER_DESC = "favourites:sortOrder:desc"
    const val FAVOURITES_EMPTY = "favourites:empty"
    const val FAVOURITES_LOADING = "favourites:loading"
    const val FAVOURITES_ERROR = "favourites:error"
    const val FAVOURITES_END_OF_LIST = "favourites:endOfList"

    const val BREED_DETAIL_ROOT = "breedDetail:root"
    const val BREED_DETAIL_CONTENT = "breedDetail:content"
    const val BREED_DETAIL_TEMPERAMENTS = "breedDetail:temperaments"
    const val BREED_DETAIL_TRAITS = "breedDetail:traits"
    const val BREED_DETAIL_BADGES = "breedDetail:badges"

    fun breedsCardTop(breedId: String): String = "breeds:card:top:$breedId"

    fun breedsCardBack(breedId: String): String = "breeds:card:back:$breedId"

    fun favouritesItem(imageId: String): String = "favourites:item:$imageId"

    fun favouritesItemRemove(imageId: String): String = "favourites:item:$imageId:remove"

    fun breedDetailTemperament(value: String): String = "breedDetail:temperament:${normalize(value)}"

    fun breedDetailTrait(value: String): String = "breedDetail:trait:${normalize(value)}"

    fun breedDetailBadge(value: String): String = "breedDetail:badge:${normalize(value)}"

    private fun normalize(value: String): String = value.lowercase().replace(' ', '_')
}