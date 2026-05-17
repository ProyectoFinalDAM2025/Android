package leo.rios.officium.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import leo.rios.officium.core.database.entity.ProvinciaEntity

@Dao
interface ProvinciaDao {
    @Query("SELECT * FROM provincias ORDER BY name")
    suspend fun getAll(): List<ProvinciaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(provincias: List<ProvinciaEntity>)
}
