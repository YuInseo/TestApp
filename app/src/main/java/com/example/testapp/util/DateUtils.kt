package com.example.testapp.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dayKeyFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val monthFormat = SimpleDateFormat("M월", Locale.KOREAN)
    private val timeFormat = SimpleDateFormat("a h:mm", Locale.KOREAN)
    private val fullDateFormat = SimpleDateFormat("yyyy.MM.dd (E)", Locale.KOREAN)

    fun startOfDay(time: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun endOfDay(time: Long = System.currentTimeMillis()): Long =
        startOfDay(time) + 86_400_000L - 1L

    fun addDays(time: Long, days: Int): Long = time + days * 86_400_000L

    fun isToday(time: Long): Boolean = startOfDay(time) == startOfDay()

    fun isTomorrow(time: Long): Boolean =
        startOfDay(time) == addDays(startOfDay(), 1)

    fun isYesterday(time: Long): Boolean =
        startOfDay(time) == addDays(startOfDay(), -1)

    fun formatDate(time: Long): String = fullDateFormat.format(Date(time))
    fun formatTime(time: Long): String = timeFormat.format(Date(time))

    fun shortRelativeDate(time: Long): String {
        val today0 = startOfDay()
        val target0 = startOfDay(time)
        val diffDays = ((target0 - today0) / 86_400_000L).toInt()
        return when (diffDays) {
            -1 -> "어제"
            0 -> "오늘"
            1 -> "내일"
            in 2..6 -> {
                val cal = Calendar.getInstance().apply { timeInMillis = time }
                val dow = arrayOf("일요일", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일")[cal.get(Calendar.DAY_OF_WEEK) - 1]
                dow
            }
            else -> {
                val cal = Calendar.getInstance().apply { timeInMillis = time }
                "${cal.get(Calendar.MONTH) + 1}월 ${cal.get(Calendar.DAY_OF_MONTH)}일"
            }
        }
    }

    fun shortRelativeWithTime(time: Long): String {
        val date = shortRelativeDate(time)
        val cal = Calendar.getInstance().apply { timeInMillis = time }
        val isMidnight = cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0
        return if (isMidnight) date else "$date ${formatTime(time)}"
    }

    fun dayOfWeekKorean(time: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = time }
        return arrayOf("일", "월", "화", "수", "목", "금", "토")[cal.get(Calendar.DAY_OF_WEEK) - 1]
    }

    fun dayKey(time: Long): String =
        Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDate().format(dayKeyFormat)

    fun dayKey(date: LocalDate): String = date.format(dayKeyFormat)

    fun toLocalDate(time: Long): LocalDate =
        Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDate()

    fun localDateToMillis(date: LocalDate, time: LocalTime = LocalTime.NOON): Long =
        ZonedDateTime.of(LocalDateTime.of(date, time), ZoneId.systemDefault())
            .toInstant().toEpochMilli()

    fun monthHeader(year: Int, month: Int): String = "${month}월"

    fun longFullDate(time: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = time }
        val dow = dayOfWeekKorean(time)
        return "$dow, ${cal.get(Calendar.MONTH) + 1}월 ${cal.get(Calendar.DAY_OF_MONTH)}일"
    }
}
