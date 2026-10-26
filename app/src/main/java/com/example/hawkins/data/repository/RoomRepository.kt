package com.example.hawkins.data.repository

import com.example.hawkins.domain.model.RoomContract

interface RoomRepository { suspend fun load(): RoomContract }
