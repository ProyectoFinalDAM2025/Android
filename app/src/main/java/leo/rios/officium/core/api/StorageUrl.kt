package leo.rios.officium.core.api

private const val STORAGE_BASE_URL = "https://api.officium.es"

fun String?.toStorageUrl(): String? {
    if (this.isNullOrBlank()) return null
    if (startsWith("http://") || startsWith("https://")) return this

    return "$STORAGE_BASE_URL/${trimStart('/')}"
}
