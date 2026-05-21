package leo.rios.officium.core.api

private const val STORAGE_BASE_URL = "http://10.0.2.2:8000"

fun String?.toStorageUrl(): String? {
    if (this.isNullOrBlank()) return null
    if (startsWith("http://") || startsWith("https://")) return this

    return "$STORAGE_BASE_URL/${trimStart('/')}"
}
