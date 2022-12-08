package com.wakeup.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.wakeup.domain.model.SortType
import com.wakeup.domain.model.WeatherType
import com.wakeup.domain.usecase.DeleteMomentUseCase
import com.wakeup.domain.usecase.GetAllMomentsUseCase
import com.wakeup.domain.usecase.GetMomentListUseCase
import com.wakeup.domain.usecase.weather.GetWeatherDataUseCase
import com.wakeup.presentation.mapper.toDomain
import com.wakeup.presentation.mapper.toPresentation
import com.wakeup.presentation.model.LocationModel
import com.wakeup.presentation.model.MomentModel
import com.wakeup.presentation.model.WeatherModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val STATE_COLLAPSED = 4

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMomentListUseCase: GetMomentListUseCase,
    private val getAllMomentListUseCase: GetAllMomentsUseCase,
    private val getWeatherDataUseCase: GetWeatherDataUseCase,
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    lateinit var allMoments: StateFlow<List<MomentModel>>
    lateinit var moments: Flow<PagingData<MomentModel>>

    private val _scrollToTop = MutableStateFlow(false)
    val scrollToTop = _scrollToTop.asStateFlow()

    val sortType = MutableStateFlow(SortType.MOST_RECENT)
    val bottomSheetState = MutableStateFlow(STATE_COLLAPSED)

    val fetchLocationState = MutableStateFlow(false)

    private val location = MutableStateFlow<LocationModel?>(null)

    private val _weather = MutableStateFlow(WeatherModel(0, WeatherType.NONE, "", 0.0))
    val weather = _weather.asStateFlow()

    init {
        fetchMoments()
        fetchAllMoments()
    }

    fun fetchMoments() {
        moments = getMomentListUseCase(
            sortType = sortType.value,
            query = searchQuery.value,
            myLocation = location.value?.toDomain()
        ).map { pagingMoments ->
            pagingMoments.map { moment ->
                moment.toPresentation()
            }
        }.cachedIn(viewModelScope)

        fetchLocationState.value = false
        location.value = null
    }

    fun fetchAllMoments() {
        allMoments = getAllMomentListUseCase(searchQuery.value).map { moments ->
            moments.map { moment ->
                moment.toPresentation()
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun fetchWeather(location: LocationModel) {
        viewModelScope.launch {
            getWeatherDataUseCase(location.toDomain())
                .onSuccess {
                    _weather.value = it.toPresentation()
                }.onFailure {
                    // TODO UI State 실패 처리하면 좋겠다.
                    _weather.value = WeatherModel(0, WeatherType.NONE, "", 0.0)
                }
        }
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setScrollToTop(state: Boolean) {
        _scrollToTop.value = state
    }

    fun setLocation(location: LocationModel) {
        this.location.value = location
    }
}