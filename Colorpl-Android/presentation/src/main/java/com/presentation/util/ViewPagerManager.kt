package com.presentation.util

import androidx.viewpager2.widget.ViewPager2

object ViewPagerManager {
    private var viewPager: ViewPager2? = null

    const val PREV = -1;
    const val NEXT = 1;

    fun setViewPager(viewPager: ViewPager2) {
        this.viewPager = viewPager
    }

    fun getViewPager(): ViewPager2? {
        return viewPager
    }

    fun moveNext() : Int {
        viewPager?.let {
            val currentItem = it.currentItem
            if (currentItem < (it.adapter?.itemCount?.minus(1) ?: 0)) {
                it.setCurrentItem(currentItem + 1, true)
            }
        }
        return NEXT
    }

    fun movePrevious() : Int {
        viewPager?.let {
            val currentItem = it.currentItem
            if (currentItem > 0) {
                it.setCurrentItem(currentItem - 1, true)
            }
        }
        return PREV
    }
}
