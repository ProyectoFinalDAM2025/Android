package leo.rios.officium.login.data

import com.google.gson.JsonObject

fun JsonObject?.getProfileId(): String? =
    getFirstString("IDDesempleado", "IDEmpresa", "IDAdministrador", "id")

fun JsonObject?.getProfileName(): String? =
    getCompanyName() ?: getFullName() ?: getFirstString("name")

fun JsonObject?.getProfilePhoto(): String? =
    getFirstString("Foto", "FotoPerfil", "foto", "photo")

fun String.normalizeProfileRole(): String {
    return when (this.lowercase()) {
        "usuario", "desempleado" -> "Desempleado"
        "empresa" -> "Empresa"
        else -> this
    }
}

private fun JsonObject?.getCompanyName(): String? =
    getFirstString("NombreEmpresa")

private fun JsonObject?.getFullName(): String? {
    if (this == null) return null

    val firstName = getFirstString("Nombre")
    val lastName = getFirstString("Apellido")

    return listOfNotNull(firstName, lastName)
        .joinToString(" ")
        .takeIf { it.isNotBlank() }
}

private fun JsonObject?.getFirstString(vararg keys: String): String? {
    if (this == null) return null

    return keys.firstNotNullOfOrNull { key ->
        get(key)?.takeIf { !it.isJsonNull }?.asString?.takeIf { it.isNotBlank() }
    }
}
