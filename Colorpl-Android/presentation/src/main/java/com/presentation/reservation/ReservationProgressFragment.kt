package com.presentation.reservation


import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.colorpl.presentation.R
import com.colorpl.presentation.databinding.FragmentReservationProgressBinding
import com.presentation.base.BaseFragment
import com.presentation.util.TopButtonsStatus
import com.presentation.util.ViewPagerManager
import com.presentation.util.onBackButtonPressed
import com.presentation.viewmodel.ReservationViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ReservationProgressFragment :
    BaseFragment<FragmentReservationProgressBinding>(R.layout.fragment_reservation_progress) {
    private val args: ReservationProgressFragmentArgs by navArgs()
    private val reservationViewModel: ReservationViewModel by viewModels()


    override fun initView() {
        initUi()
//        backEvent()
    }

    private fun initUi() {
        initViewPager()
        initWormDotsIndicator()
        initTopButtonsClickEvent()
        initViewModelData()
        Timber.d(args.reservationDetail.title)
    }

    private fun initViewModelData() {
        with(args.reservationDetail) {
            reservationViewModel.setReservationDetailId(reservationInfoId)
            contentImg?.let { reservationViewModel.setReservationImg(it) }
            reservationViewModel.setReservationTitle(title)
            reservationViewModel.setReservationPriceBySeatClass(priceBySeatClass)
            if (schedule == null) {
                Timber.tag("args").e("null")
            } else {
                Timber.tag("args").e(schedule.toString())
            }
            schedule?.let { reservationViewModel.setReservationSchedule(it) }
        }
    }

    /** ViewPager 설정 */
    private fun initViewPager() {
        binding.vpScreen.apply {
            this@apply.adapter = ViewPagerAdapter(this@ReservationProgressFragment)
            this@apply.isUserInputEnabled = false
            // ViewPagerManager에 ViewPager 인스턴스 설정
            ViewPagerManager.setViewPager(this@apply)

            // OnPageChangeCallback 추가
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    handlePageSelected(position)
                    handleBackEvent(position)
                }
            })
        }
    }

    /** 상단 버튼 visibility 유무.
     * @param position 페이지 */
    private fun handlePageSelected(position: Int) {
        val status = when (position) {
            0 -> TopButtonsStatus.EXIT
            1, 2 -> TopButtonsStatus.BOTH
            3 -> TopButtonsStatus.NONE
            else -> throw IllegalStateException("Unexpected position $position")
        }
        binding.type = status
    }

    /** 시스템 뒤로가기 이벤트 처리.
     * @param position 페이지 */
    private fun handleBackEvent(position: Int) {
        requireActivity().onBackButtonPressed(viewLifecycleOwner) {
            when (position) {
                0, 3 -> navigatePopBackStack()
                1, 2 -> ViewPagerManager.movePrevious()
                else -> throw IllegalStateException("Unexpected position $position")
            }
        }
    }

    /** WormDotsIndicator 설정 */
    private fun initWormDotsIndicator() {
        binding.wdiProgress.apply {
            attachTo(binding.vpScreen)
            this@apply.dotsClickable = false // 추후 false 처리.
        }
    }

    /** 상단 버튼 클릭 이벤트 설정 */
    private fun initTopButtonsClickEvent() {
        binding.includeTopCenter.apply {
            ivBack.setOnClickListener {
                reservationViewModel.setViewPagerStatus(ViewPagerManager.movePrevious())
            }
            tvExit.setOnClickListener {
                navigatePopBackStack()
            }
        }
    }


    private inner class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int {
            return 4
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                TIME_TABLE -> ReservationTimeTableFragment()
                SEAT -> ReservationSeatFragment()
                PAYMENT -> ReservationPaymentFragment()
                COMPLETE -> ReservationCompleteFragment()
                else -> throw IllegalStateException("Unexpected position $position")
            }
        }
    }

    companion object {
        const val TIME_TABLE = 0
        const val SEAT = 1
        const val PAYMENT = 2
        const val COMPLETE = 3

    }

}