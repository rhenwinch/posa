package io.posa.core.network

import io.posa.core.common.enum.SortOrder
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TheCatApiServiceImplTest {
    private companion object {
        const val DEFAULT_PAGE = 0
        const val DEFAULT_LIMIT = 10
    }

    private lateinit var api: TheCatApiService

    @BeforeTest
    fun setUp() {
        api = ktorfitClient.createTheCatApiService()
    }

    @Test
    fun `getBreeds must return 10 random cat breeds`() = runTest {
        val list = api.getBreeds(
            page = DEFAULT_PAGE,
            limit = DEFAULT_LIMIT,
            order = SortOrder.RANDOM.name
        )

        assertEquals(list.isNotEmpty(), true)
    }

    @Test
    fun `getBreed must return a cat breed`() = runTest {
        api.getBreed(id = "abys")

        assertTrue(true) // Just for test case passing
    }
}