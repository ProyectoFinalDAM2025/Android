package leo.rios.officium.userProfile.presentation.model

enum class ProfileUploadType(
    val title: String,
    val documentType: String?,
    val mimeType: String
) {
    Publication("Publicacion", null, "*/*"),
    Photo("Foto", "Foto", "image/*"),
    Video("Video", "Video", "video/*"),
    Pdf("PDF", "PDF", "application/pdf")
}
