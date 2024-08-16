package com.presentation.reservation

import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.colorpl.presentation.R
import com.colorpl.presentation.databinding.FragmentReservationTimeTableBinding
import com.domain.model.DateTableItem
import com.domain.model.ReservationPairInfo
import com.domain.model.TimeTable
import com.presentation.base.BaseFragment
import com.presentation.component.adapter.reservation.process.OnTimeTableClickListener
import com.presentation.component.adapter.reservation.process.ReservationDateTableAdapter
import com.presentation.component.adapter.reservation.process.ReservationPlaceAdapter
import com.presentation.util.ViewPagerManager
import com.presentation.viewmodel.MainViewModel
import com.presentation.viewmodel.ReservationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@AndroidEntryPoint
class ReservationTimeTableFragment :
    BaseFragment<FragmentReservationTimeTableBinding>(R.layout.fragment_reservation_time_table),
    OnTimeTableClickListener {
    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: ReservationViewModel by viewModels({ requireParentFragment() })
    private val reservationPlaceAdapter by lazy {
        ReservationPlaceAdapter(this)
    }
    private val reservationDateTableAdapter by lazy {
        ReservationDateTableAdapter { dateItem ->
            viewModel.setReservationDate(dateItem.date)
        }
    }

    override fun initView() {
        initDate()
        initTimeTableAdapter()
        initDateAdapter()
        initViewModel()
        observedSelectedDate()
        observedReservationSchedule()
        observeReservationSchedule()
        observedSelectedDate()
    }


    private fun initTimeTableAdapter() {
        binding.rcReservationTimeTable.adapter = reservationPlaceAdapter
    }

    private fun initDateAdapter() {
        binding.rcReservationDateTable.apply {
            this@apply.itemAnimator = null
            this@apply.adapter = reservationDateTableAdapter
        }
    }

    private fun initDate() {
        val mainDate = mainViewModel.reservationDate.value
        val scheduleData = viewModel.reservationSchedule.value
        val initData = if (mainDate == LocalDate.now()) {
            var tmp: LocalDate? = null
            for (i in 0 until 14) {
                val date = LocalDate.now().plusDays(i.toLong())
                val formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                if (scheduleData[formattedDate] == true) {
                    tmp = date
                    break;
                }
            }
            tmp
        } else {
            mainDate
        }
        initData?.let {
            viewModel.setReservationDate(it)
        }
    }


    private fun initViewModel() {
        binding.apply {
            this@apply.viewModel = this@ReservationTimeTableFragment.viewModel
        }
    }

    private fun observedSelectedDate() {
        var position = 0
        var check = false
        viewModel.reservationDate.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach {
                val dateList = (0 until 14).map { i ->
                    val date = LocalDate.now().plusDays(i.toLong())
                    val formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    if (position == 0) {
                        position = if (it == date) i else 0

                    }
                    Timber.tag("viewModel_Schedule")
                        .d(viewModel.reservationSchedule.value.toString())
                    DateTableItem(
                        date = date,
                        isSelected = viewModel.reservationDate.value == date,
                        isEvent = viewModel.reservationSchedule.value[formattedDate] ?: false
                    )
                }
                reservationDateTableAdapter.submitList(dateList)

                if(!check){
                    viewLifecycleOwner.lifecycleScope.launch {
                        delay(300L)
                        Timber.d("포지션 확인 $position")
                        binding.rcReservationDateTable.scrollToPosition(position)
                        check = true
                    }
                }

                val formattedDate =
                    viewModel.reservationDate.value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                viewModel.getReservationSchedule(viewModel.reservationDetailId.value, formattedDate)
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun observedReservationSchedule() {
        viewModel.getReservationSchedule.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach {
                reservationPlaceAdapter.submitList(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun observeReservationSchedule() {
        viewModel.reservationSchedule.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach {
//                observedSelectedDate()
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onTimeTableClick(data: ReservationPairInfo, timeTable: TimeTable) {
        // 예약 날짜와 현재 날짜가 같다면 시간 비교
        viewModel.setReservationSeat(listOf())
        if (viewModel.reservationDate.value == LocalDate.now()) {
            runCatching {
                val parsedTime = LocalTime.parse(timeTable.startTime)
                parsedTime
            }.onSuccess { startTime ->
                val currentTime = LocalDateTime.now().toLocalTime()
                if (currentTime.isAfter(startTime)) {
                    Toast.makeText(context, getString(R.string.reservation_over_time), Toast.LENGTH_SHORT).show()
                } else {
                    proceedWithReservation(data, timeTable)
                }
            }.onFailure { exception ->
                if (exception is DateTimeParseException) {
                    Timber.e("시간 파싱 오류: ${exception.message}")
                } else {
                    Timber.e("예상치 못한 오류 발생: ${exception.message}")
                }
            }
        } else {
            // 날짜가 오늘과 같지 않다면 바로 예약 진행
            proceedWithReservation(data, timeTable)
        }
    }

    private fun proceedWithReservation(data: ReservationPairInfo, timeTable: TimeTable) {
        with(viewModel) {
            setReservationPlace(data.placeName)
            setReservationTheater(data.hallName)
            setReservationTimeTable(timeTable)
            getReservationSeat(reservationDetailId.value, timeTable.scheduleId)
            Timber.tag("Seat API 요청").d("${reservationDetailId.value}, ${timeTable.scheduleId}")
        }
        viewModel.setViewPagerStatus(ViewPagerManager.moveNext())
    }


}
