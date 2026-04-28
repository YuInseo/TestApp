package com.example.testapp.domain.model

enum class Recurrence(val label: String) {
    NONE("반복 없음"),
    DAILY("매일"),
    WEEKDAYS("평일"),
    WEEKLY("매주"),
    MONTHLY("매월"),
    YEARLY("매년");
}
