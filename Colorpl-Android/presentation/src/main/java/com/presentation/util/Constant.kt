package com.presentation.util

import android.content.Context
import com.colorpl.presentation.R
import com.presentation.component.custom.DropDownData

enum class Mode(val mode: String) {
    WALK("WALK"),
    BUS("BUS"),
    SUBWAY("SUBWAY"),
    EXPRESS_BUS("EXPRESS BUS"),
    TRAIN("TRAIN"),
    AIRPLANE("AIRPLANE"),
    FERRY("FERRY"),
}

enum class TicketState(val state: Int) {
    ISSUED(0),
    UNISSUED(1)
}

enum class Calendar {
    CURRENT,
    NEXT,
    PREVIOUS,
    CHANGE,
    RESTORE
}

enum class FilterType(private val resourceId: Int) {
    ALL(R.string.feed_filter_all),
    MOVIE(R.string.feed_filter_movie),
    PERFORMANCE(R.string.feed_filter_performance),
    CONCERT(R.string.feed_filter_concert),
    PLAY(R.string.feed_filter_play),
    MUSICAL(R.string.feed_filter_musical),
    EXHIBITION(R.string.feed_filter_exhibition);

    fun getText(context: Context): String {
        return context.getString(resourceId)
    }
}

enum class Sign {
    ID,
    PASSWORD,
    NICKNAME,
    PROFILE
}

enum class Category {
    MOVIE,
    THEATRE,
    MUSICAL,
    CIRCUS,
    EXHIBITION
}

enum class PaymentResult(val value: Int) {
    COMPLETE(0),
    REFUND(1),
    USE(2);


    companion object {

        fun getType(value: Int): PaymentResult =
            entries.find { it.value == value } ?: COMPLETE

        fun getMenu(mode : PaymentResult) : List<Int>{
            return when(mode){
                COMPLETE -> listOf(0,1)
                REFUND -> listOf(1)
                USE -> listOf(2)
            }
        }
    }
}

enum class DropDownMenu(private val value: Int, private val resourceId: Int) {
    REFUND(0, R.string.drop_down_menu_refund),
    DELETE(1, R.string.drop_down_menu_delete);

    fun getText(context: Context): String {
        return context.getString(resourceId)
    }

    companion object{
        fun getMenu(value : Int) : DropDownMenu = entries.find { it.value == value } ?: REFUND

        fun getDropDown(value : List<Int>, context : Context) : List<DropDownData>{
            val result = mutableListOf<DropDownData>()
            value.forEach { id ->
                val menu = getMenu(id)
                result.add(DropDownData(menu.getText(context),menu))
            }
            return result
        }

    }

}