package com.james.disasterapp

import java.util.concurrent.Executors

object AdminArea {
    val suggestions = listOf(
        Pair("Semua", "all"),
        Pair("Aceh", "ID-AC"),
        Pair("Bali", "ID-BA"),
        Pair("Kep Bangka Belitung", "ID-BB"),
        Pair("Banten", "ID-BT"),
        Pair("Bengkulu", "ID-BE"),
        Pair("Jawa Tengah", "ID-JT"),
        Pair("Kalimantan Tengah", "ID-KT"),
        Pair("Sulawesi Tengah", "ID-ST"),
        Pair("Jawa Timur", "ID-JI"),
        Pair("Kalimantan Timur", "ID-KI"),
        Pair("Nusa Tenggara Timur", "ID-NT"),
        Pair("Gorontalo", "ID-GO"),
        Pair("DKI Jakarta", "ID-JK"),
        Pair("Jambi", "ID-JA"),
        Pair("Lampung", "ID-LA"),
        Pair("Maluku", "ID-MA"),
        Pair("Kalimantan Utara", "ID-KU"),
        Pair("Maluku Utara", "ID-MU"),
        Pair("Sulawesi Utara", "ID-SA"),
        Pair("Sumatera Utara", "ID-SU"),
        Pair("Papua", "ID-PA"),
        Pair("Riau", "ID-RI"),
        Pair("Kepulauan Riau", "ID-KR"),
        Pair("Sulawesi Tenggara", "ID-SG"),
        Pair("Kalimantan Selatan", "ID-KS"),
        Pair("Sulawesi Selatan", "ID-SN"),
        Pair("Sumatera Selatan", "ID-SS"),
        Pair("DI Yogyakarta", "ID-YO"),
        Pair("Jawa Barat", "ID-JB"),
        Pair("Kalimantan Barat", "ID-KB"),
        Pair("Nusa Tenggara Barat", "ID-NB"),
        Pair("Papua Barat", "ID-PB"),
        Pair("Sulawesi Barat", "ID-SR"),
        Pair("Sumatera Barat", "ID-SB"),
    )

    val typeDisaster = listOf("flood", "earthquake", "fire", "haze", "wind", "volcano")

    const val PROVINCE = "all"
}

const val NOTIFICATION_CHANNEL_NAME = "Course Channel"
const val NOTIFICATION_CHANNEL_ID = "notify-schedule"
const val NOTIFICATION_ID = 32
const val ID_REPEATING = 101
private val SINGLE_EXECUTOR = Executors.newSingleThreadExecutor()

fun executeThread(f: () -> Unit) {
    SINGLE_EXECUTOR.execute(f)
}