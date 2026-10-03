package dev.tulis.readbear.db.converters

import androidx.room.TypeConverter
import kotlin.time.Instant


class InstantConverter {

    @TypeConverter
    fun fromInstant(value: Instant?): Long? =
        value?.toEpochMilliseconds()

    @TypeConverter
    fun toInstant(value: Long?): Instant? =
        value?.let { Instant.fromEpochMilliseconds(it) }
}