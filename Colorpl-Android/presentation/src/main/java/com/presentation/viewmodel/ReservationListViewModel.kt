package com.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.domain.model.ReservationInfo
import com.domain.model.ShowParam
import com.domain.usecase.ReservationListUseCase
import com.domain.util.DomainResult
import com.presentation.util.ShowType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ReservationListViewModel @Inject constructor(
    private val getReservationListUseCase: ReservationListUseCase,
) : ViewModel() {
    private val _reservationList = MutableStateFlow<List<ReservationInfo>>(listOf(ReservationInfo()))
    val reservationList: MutableStateFlow<List<ReservationInfo>> = _reservationList


    private val _showParam = MutableStateFlow<ShowParam>(ShowParam())
    val showParam: StateFlow<ShowParam> get() = _showParam

    fun setShowParam(value: ShowParam) {
        _showParam.value = value
    }

    private val _date = MutableStateFlow<LocalDate?>(null)
    val date : StateFlow<LocalDate?> get() = _date

    fun setDate(value : LocalDate?){
        _date.value = value
    }


    init {
        getReservationList(setFilter())
    }

    fun setParam(type: ShowType, param: String) {
        when (type) {
            ShowType.KEYWORD -> {
                setShowParam(
                    _showParam.value.copy(
                        keyword = param
                    )
                )
            }

            ShowType.DATE -> {
                setShowParam(
                    _showParam.value.copy(
                        date = param
                    )
                )
            }

            ShowType.CATEGORY -> {
                setShowParam(
                    _showParam.value.copy(
                        category = param
                    )
                )
            }

            ShowType.LOCATION -> {
                setShowParam(
                    _showParam.value.copy(
                        area = param
                    )
                )
            }
        }
        getReservationList(filter = setFilter())
    }

    fun setFilter(): Map<String, String?> {
        val data = showParam.value
        Timber.d("파람 확인 $data")
        return hashMapOf(
            "date" to data.date,
            "area" to data.area,
            "keyword" to data.keyword,
            "category" to data.category
        ).filter { it.value?.isNotEmpty() == true }
    }

    fun dataClear(){
        setShowParam(ShowParam())
        getReservationList(setFilter())
        setDate(null)
    }



    /** 예매 아이템 전체 조회. */
    fun getReservationList(filter: Map<String, String?>) {
        viewModelScope.launch {
            getReservationListUseCase(filters = filter).collectLatest { response ->
                when (response) {
                    is DomainResult.Success -> {
                        Timber.d("예매 정보 조회 성공 ${response.data}")
                        _reservationList.value = response.data
                    }

                    is DomainResult.Error -> {
                        Timber.d("예매 정보 조회 실패 ${response.exception}")
                    }
                }
            }
        }
    }

}