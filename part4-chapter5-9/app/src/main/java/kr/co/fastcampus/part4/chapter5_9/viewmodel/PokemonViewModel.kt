package kr.co.fastcampus.part4.chapter5_9.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kr.co.fastcampus.part4.chapter5_9.PokeAPI
import kr.co.fastcampus.part4.chapter5_9.PokemonResponse
import kr.co.fastcampus.part4.chapter5_9.Response
import javax.inject.Inject

@HiltViewModel
class PokemonViewModel @Inject constructor(
    private val pokeAPI: PokeAPI
) : ViewModel() {

    val pokemonList: Flow<PagingData<Response.Result>> = getPokemons().cachedIn(viewModelScope)
    var pokemonResult by mutableStateOf(
        PokemonResponse(
            PokemonResponse.Species(""),
            PokemonResponse.Sprites("")
        )
    )

    private fun getPokemons(): Flow<PagingData<Response.Result>> = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = true,
            prefetchDistance = 5
        ),
        pagingSourceFactory = {
            object : PagingSource<Int, Response.Result>() {
                override fun getRefreshKey(state: PagingState<Int, Response.Result>): Int? {
                    return state.anchorPosition
                }

                override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Response.Result> {
                    try {
                        val pokemons = if (params.key != null) {
                            pokeAPI.getPokemons(offset = params.key as Int, limit = params.loadSize)
                        } else {
                            pokeAPI.getPokemons()
                        }
                        // 단계 2: `offset=20&limit=20` 형태의 주소에서
                        // `prevKey`와 `nextKey`를 만들어 전달하자.
                        val prevKey = pokemons.previous
                            ?.substringAfter("offset=")
                            ?.substringBefore("&")
                            ?.toInt()

                        val nextKey = pokemons.next
                            ?.substringAfter("offset=")
                            ?.substringBefore("&")
                            ?.toInt()

                        return LoadResult.Page(
                            data = pokemons.results,
                            prevKey = prevKey,
                            nextKey = nextKey
                        )
                    } catch (e: Exception) {
                        Log.e("EEE", "error: $e")
                        e.printStackTrace()
                        return LoadResult.Error(e)
                    }
                }
            }
        }
    ).flow

    fun getPokemon(pokemonId: Int) {
        viewModelScope.launch {
            pokemonResult = pokeAPI.getPokemon(pokemonId)
        }
    }
}