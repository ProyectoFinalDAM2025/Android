package leo.rios.officium.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "provincias")
data class ProvinciaEntity(
    @PrimaryKey val id: Int,
    val ine: String,
    val name: String
)
