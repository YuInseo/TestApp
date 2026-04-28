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
    private val dateFormat = SimpleDateFormat("yyyy.MM.dd (E)", Locale.KOREAN)
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.KOREAN)
    private val dayKeyFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

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

    fun formatDate(time: Long): String = dateFormat.format(Date(time))
    fun formatTime(time: Long): String = timeFormat.format(Date(time))
    fun formatDateTime(time: Long): String = "${formatDate(time)} ${formatTime(time)}"

    fun shortRelative(time: Long): String {
        val now = System.currentTimeMillis()
        val today0 = startOfDay(now)
        val target0 = startOfDay(time)
        val diffDays = ((target0 - today0) / 86_400_000L).toInt()
        return when (diffDays) {
            -1 -> "어제 " + formatTime(time)
            0 -> "오늘 " + formatTime(time)
            1 -> "내일 " + formatTime(time)
            in 2..6 -> {
                val cal = Calendar.getInstance().apply { timeInMillis = time }
                val dow = arrayOf("일", "월", "화", "수", "목", "금", "토")[cal.get(Calendar.DAY_OF_WEEK) - 1]
                "${dow}요일 " + formatTime(time)
            }
            else -> formatDate(time)
        }
    }

    fun dayKey(time: Long): String =
        Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDate().format(dayKeyFormat)

    fun dayKey(date: LocalDate): String = date.format(dayKeyFormat)

    fun toLocalDate(time: Long): LocalDate =
        Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDate()

    fun localDateToMillis(date: LocalDate, time: LocalTime = LocalTime.NOON): Long =
        ZonedDateTime.of(LocalDateTime.of(date, time), ZoneId.systemDefault())
            .toInstant().toEpochMilli()
}
