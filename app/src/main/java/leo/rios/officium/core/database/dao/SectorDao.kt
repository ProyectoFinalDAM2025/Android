package leo.rios.officium.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import leo.rios.officium.core.database.entity.SectorEntity

@Dao
interface SectorDao {
    @Query("SELECT * FROM sectors ORDER BY nombre")
    suspend fun getAll(): List<SectorEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sectors: List<SectorEntity>)
}
