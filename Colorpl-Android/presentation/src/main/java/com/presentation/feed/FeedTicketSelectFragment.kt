package com.presentation.feed

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.colorpl.presentation.R
import com.colorpl.presentation.databinding.FragmentFeedTicketSelectBinding
import com.presentation.base.BaseDialogFragment
import com.presentation.component.adapter.feed.FeedTicketSelectAdapter
import com.presentation.component.dialog.LoadingDialog
import com.presentation.util.addCustomItemDecoration
import com.presentation.viewmodel.TicketSelectViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber


@AndroidEntryPoint
class FeedTicketSelectFragment :
    BaseDialogFragment<FragmentFeedTicketSelectBinding>(R.layout.fragment_feed_ticket_select) {

    private val feedTicketSelectAdapter by lazy {
        FeedTicketSelectAdapter() {

        }
    }

    private val loadingDialog by lazy {
        LoadingDialog(requireContext())
    }
    private val viewModel: TicketSelectViewModel by viewModels()

    //얘도 ViewModel에서 따로 빼서 쓰면 될듯
    var ticketPosition = 0

    override fun initView(savedInstanceState: Bundle?) {
        observeViewModel()
        initAdapter()
        initClickEvent()
    }

    private fun observeViewModel() {
        loadingDialog.show()
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.tickets.collectLatest { unreviewedList ->
                    feedTicketSelectAdapter.submitList(unreviewedList)
                    loadingDialog.dismiss()
                }
            }
        }
    }


    private fun initAdapter() {
        binding.vpFeedTicketSelect.apply {
            adapter = feedTicketSelectAdapter
            offscreenPageLimit = 1
            this.addCustomItemDecoration()
            this.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    Timber.d("현재 페이지 위치 $position")
                }
            })
        }
    }

    private fun initClickEvent() {
        binding.apply {
            tvSelect.setOnClickListener {
                navigateDestination(R.id.action_fragment_feed_ticket_select_to_fragment_review)
            }
            ivBack.setOnClickListener {
                navigatePopBackStack()
            }
        }
    }
}