package com.presentation.my_page

import androidx.core.os.bundleOf
import androidx.core.view.postDelayed
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.colorpl.presentation.R
import com.colorpl.presentation.databinding.FragmentMyPageBinding
import com.domain.model.TicketResponse
import com.presentation.base.BaseFragment
import com.presentation.component.adapter.schedule.TicketAdapter
import com.presentation.my_page.model.MyPageEventState
import com.presentation.util.setDistanceX
import com.presentation.util.setImageCircleCrop
import com.presentation.util.setTransactionX
import com.presentation.viewmodel.FeedViewModel
import com.presentation.viewmodel.MyPageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class MyPageFragment : BaseFragment<FragmentMyPageBinding>(R.layout.fragment_my_page) {

    private val myPageViewModel: MyPageViewModel by viewModels()


    private val ticketAdapter: TicketAdapter by lazy {
        TicketAdapter(
            onTicketClickListener = { ticket ->
                onTicketClickListener(ticket = ticket)
            }
        )
    }

    override fun initView() {
        initTicket()
        initClickEvent()
        observeUiState()
        observeTicket()
        observeEvent()
    }

    override fun onResume() {
        super.onResume()
        myPageViewModel.getMemberInfo()
    }


    private fun initTicket() {
        binding.rcTicket.apply {
            adapter = ticketAdapter
            itemAnimator = null
        }


    }


    private fun observeTicket() {
        myPageViewModel.useTickets.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach {
                ticketAdapter.submitList(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)

        myPageViewModel.unUseTickets.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach {
                ticketAdapter.submitList(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)
    }


    private fun observeUiState() {
        myPageViewModel.memberUiState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach {
                binding.member = it.memberInfo
                binding.ivProfileImg.setImageCircleCrop(it.memberInfo?.profileImage, true)

            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun observeEvent() {
        myPageViewModel.myPageEventState.flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach {
                when (it) {
                    is MyPageEventState.UseTicket -> {
                        binding.apply {
                            ticketTitle = true
                            ticketCount = it.data.size.toString()
                            ticketAdapter.submitList(it.data)
                            ivTicketExpire.isSelected = true
                            ivTicketStar.isSelected = false
                            val distance =
                                setDistanceX(binding.ivTicketStar, binding.ivTicketExpire)
                            indicator.setTransactionX(distance)
                        }

                    }

                    is MyPageEventState.UnUseTicket -> {
                        binding.apply {
                            ticketTitle = false
                            ticketCount = it.data.size.toString()
                            ticketAdapter.submitList(it.data)
                            ivTicketStar.isSelected = true
                            ivTicketExpire.isSelected = false
                            indicator.setTransactionX(0f)
                        }
                    }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

    }

    private fun initClickEvent() {

        binding.apply {
            ivTicketStar.postDelayed(300L) {
                binding.ivTicketStar.performClick()
            }
            ivTicketStar.setOnClickListener {
                myPageViewModel.ticketEvent(UN_USE)

            }
            ivTicketExpire.setOnClickListener {
                myPageViewModel.ticketEvent(USE)
            }
            tvProfileImg.setOnClickListener { //프로필 수정 이동
                navigateDestinationBundle(
                    R.id.action_fragment_my_page_to_fragment_profile_update, bundleOf(
                        "member" to myPageViewModel.memberUiState.value.memberInfo
                    )
                )
            }
            includeSearchUser.clMenu.setOnClickListener { // 유저 찾기 이동
                navigateDestination(R.id.action_fragment_my_page_to_fragment_user_search)
            }
            includeMyReview.clMenu.setOnClickListener { // 나의 리뷰 이동
                navigateDestination(R.id.action_fragment_my_page_to_fragment_my_review)
            }
            includeNotice.clMenu.setOnClickListener { //공지사항 이동
                navigateDestination(R.id.action_fragment_my_page_to_fragment_notice)
            }
            includePayment.clMenu.setOnClickListener { //결제 내역 이동
                navigateDestination(R.id.action_fragment_my_page_to_fragment_payment_history)
            }
            ivSetting.setOnClickListener { //설정 이동
                navigateDestination(R.id.action_fragment_my_page_to_fragment_setting)
            }
            imgFeedRegister.setOnClickListener {
                navigateDestination(R.id.action_fragment_my_page_to_fragment_feed_ticket_select)
            }
        }
    }

    private fun onTicketClickListener(ticket: TicketResponse) {
        navigateDestinationBundle(
            R.id.action_fragment_my_page_to_fragment_ticket,
            bundleOf("ticket" to ticket)
        )
    }

    companion object {
        const val USE = true
        const val UN_USE = false
    }

}