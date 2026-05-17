package leo.rios.officium.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import leo.rios.officium.core.database.dao.ProvinciaDao
import leo.rios.officium.core.database.dao.SectorDao
import leo.rios.officium.core.database.entity.ProvinciaEntity
import leo.rios.officium.core.database.entity.SectorEntity

@Database(
    entities = [
        SectorEntity::class,
        ProvinciaEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class OfficiumDatabase : RoomDatabase() {
    abstract fun sectorDao(): SectorDao
    abstract fun provinciaDao(): ProvinciaDao
}
