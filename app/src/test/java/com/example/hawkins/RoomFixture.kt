package com.example.hawkins

import com.example.hawkins.domain.model.*

fun roomFixture() = RoomContract(
    mapOf("diorama" to "diorama", "normal_world" to "normal_world", "alternate_world" to "alternate_world"),
    linkedMapOf("normal" to Visibility(listOf("normal_world"), listOf("alternate_world")), "alternate" to Visibility(listOf("alternate_world"), listOf("normal_world"))),
    "normal", "diorama", 14f, 8f, .78f, emptyList(), listOf("r", "u", "n"), 650, emptyMap(),
)
