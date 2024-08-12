package com.presentation.reservation

import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.colorpl.presentation.R
import com.colorpl.presentation.databinding.FragmentReservationTimeTableBinding
import com.domain.model.DateTableItem
import com.domain.model.ReservationPairInfo
import com.domain.model.ReservationPlace
import com.domain.model.Theater
import com.domain.model.TimeTable
import com.domain.model.toModel
import com.presentation.base.BaseFragment
import com.presentation.component.adapter.reservation.process.OnTimeTableClickListener
import com.presentation.component.adapter.reservation.process.ReservationDateTableAdapter
import com.presentation.component.adapter.reservation.process.ReservationPlaceAdapter
import com.presentation.util.ViewPagerManager
import com.presentation.viewmodel.ReservationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class ReservationTimeTableFragment :
    BaseFragment<FragmentReservationTimeTableBinding>(R.layout.fragment_reservation_time_table),
    OnTimeTableClickListener {
    private val viewModel: ReservationViewModel by viewModels({ requireParentFragment() })
    private val reservationPlaceAdapter by lazy {
        ReservationPlaceAdapter(this)
    }
    private val reservationDateTableAdapter by lazy {
        ReservationDateTableAdapter { dateItem ->
            onDateTableClick(dateItem)
            viewModel.setReservationDate(dateItem.date)
        }
    }

    private fun onDateTableClick(dateTable: DateTableItem) {
        Toast.makeText(requireContext(), "${dateTable.date}", Toast.LENGTH_SHORT).show()
    }

    override fun initView() {
        initAdapter()
        initDateAdapter()
        initViewModel()
        observedSelectedDate()
        observedReservationSchedule()
        observeReservationSchedule()
        viewModel.getReservationSchedule(2, "2024-08-09")
    }


    private fun initAdapter() {
        binding.rcReservationTimeTable.adapter = reservationPlaceAdapter
        val data = listOf(
            ReservationPlace.DEFAULT,
            ReservationPlace.DEFAULT.copy(
                placeName = "병점CGV",
                theaterList = listOf(
                    Theater(
                        theaterName = "1관",
                        theaterTotalSeatCount = 100,
                        timeTableList = listOf(
                            TimeTable(
                                scheduleId = 1,
                                startTime = "10:00",
                                endTime = "12:00",
                                remainingSeatCount = 99
                            ),
                            TimeTable(
                                scheduleId = 1,
                                startTime = "12:30",
                                endTime = "14:30",
                                remainingSeatCount = 88
                            ),
                            TimeTable(
                                scheduleId = 1,
                                startTime = "15:00",
                                endTime = "17:00",
                                remainingSeatCount = 77
                            ),
                            TimeTable(
                                scheduleId = 1,
                                startTime = "17:30",
                                endTime = "19:30",
                                remainingSeatCount = 15
                            ),
                            TimeTable(
                                scheduleId = 1,
                                startTime = "20:00",
                                endTime = "22:00",
                                remainingSeatCount = 23
                            )
                        )
                    ),
                )

            ),
            ReservationPlace.DEFAULT.copy(placeName = "오산CGV"),
        )


//        reservationPlaceAdapter.submitList(
//            data.toModel()
//        )
    }

    private fun initDateAdapter() {
        binding.rcReservationDateTable.apply {
            this@apply.itemAnimator = null
            this@apply.adapter = reservationDateTableAdapter
        }
        val today = LocalDate.now()
        val dateList = (0 until 14).map { i ->
            val date = today.plusDays(i.toLong())
            DateTableItem(
                date = date,
                isSelected = i == 0,
                isEvent = !date.dayOfMonth.toString().contains('6')
            )
        }

        reservationDateTableAdapter.submitList(dateList)
    }

    private fun initViewModel() {
        viewModel.setReservationDate(LocalDate.now())
        binding.apply {
            this@apply.viewModel = this@ReservationTimeTableFragment.viewModel
        }
    }

    private fun observedSelectedDate() {
        viewModel.reservationDate.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach {
                val dateList = (0 until 14).map { i ->
                    val date = LocalDate.now().plusDays(i.toLong())
                    val formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                    Timber.tag("viewModel_Schedule").d(viewModel.reservationSchedule.value.toString())
                    DateTableItem(
                        date = date,
                        isSelected = viewModel.reservationDate.value == date,
                        isEvent = viewModel.reservationSchedule.value[formattedDate]?: false
                    )
                }
                reservationDateTableAdapter.submitList(dateList)

                val formattedDate = viewModel.reservationDate.value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
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
                observedSelectedDate()
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onTimeTableClick(data: ReservationPairInfo, timeTable: TimeTable) {
        with(viewModel) {
            setReservationPlace(data.placeName)
            setReservationTheater(data.hallName)
            setReservationTimeTable(timeTable)
            getReservationSeat(reservationDetailId.value, timeTable.scheduleId)
            Timber.tag("Seat API 요청").d("${reservationDetailId.value}, ${timeTable.scheduleId}")
        }
        ViewPagerManager.moveNext()
        Timber.d("선택된 장소 : ${data.placeName}")
        Timber.d("선택된 상영관 : ${data.hallName}")
        Timber.d("선택된 시간표 : ${timeTable.startTime}")
    }


}
