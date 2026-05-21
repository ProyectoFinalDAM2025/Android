package leo.rios.officium.core.dataStore

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import leo.rios.officium.core.tinkCrypt.TinkManager
import javax.inject.Inject


class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tinkManager= TinkManager(context)

    suspend fun guardarTokens(accessToken: String, role: String){
        withContext(Dispatchers.IO){
            val encryptedAccessToken = tinkManager.aead.encrypt(accessToken.toByteArray(),null)
            val encryptedRole = tinkManager.aead.encrypt(role.toByteArray(),null)

            val encryptAccessString = Base64.encodeToString(encryptedAccessToken, Base64.DEFAULT)
            val encryptRoleString = Base64.encodeToString(encryptedRole, Base64.DEFAULT)

            context.dataStore.edit { preference ->
                preference[ACCESS_TOKEN_KEY] = encryptAccessString
                preference[ACCESS_ROLE_KEY] = encryptRoleString
            }
        }
    }

    fun getAccessToken(): Flow<String?>{
        return context.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN_KEY]?.let { data ->
                val encryptedBytes = Base64.decode(data, Base64.DEFAULT)
                val decryptedBytes = tinkManager.aead.decrypt(encryptedBytes,null)
                String(decryptedBytes)
            }
        }
    }

    fun getApplicationToken(): Flow<String?>{
        return context.dataStore.data.map { preferences ->
            preferences[APPLICATION_TOKEN_KEY]?.let { data ->
                val encryptedApplication = Base64.decode(data, Base64.DEFAULT)
                val decryptedApplication = tinkManager.aead.decrypt(encryptedApplication, null)
                String(decryptedApplication)
            }
        }
    }

    fun getIdProfile(): Flow<String?> {
        return context.dataStore.data.map { references ->
            references[ID_PROFILE_KEY]
        }
    }

    fun getProfileName(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[PROFILE_NAME_KEY]
        }
    }

    fun getProfilePhoto(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[PROFILE_PHOTO_KEY]
        }
    }

    fun getProfileJson(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[PROFILE_JSON_KEY]
        }
    }

    fun getRole(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[ACCESS_ROLE_KEY]?.let { data ->
                val encryptedBytes = Base64.decode(data, Base64.DEFAULT)
                val decryptedBytes = tinkManager.aead.decrypt(encryptedBytes,null)
                String(decryptedBytes)
            }
        }
    }//val role = dataStoreManager.getRole().first()  Para obtener el valor una sola vez

    suspend fun deleteStore(){
        withContext(Dispatchers.IO){
            context.dataStore.edit { preferences ->
                preferences.remove(ACCESS_TOKEN_KEY)
                preferences.remove(APPLICATION_TOKEN_KEY)
                preferences.remove(ACCESS_ROLE_KEY)
                preferences.remove(ID_PROFILE_KEY)
                preferences.remove(PROFILE_NAME_KEY)
                preferences.remove(PROFILE_PHOTO_KEY)
                preferences.remove(PROFILE_JSON_KEY)
            }
        }
    }

    suspend fun saveAccessToken(accessToken: String) {
        withContext(Dispatchers.IO) {
            val encryptedAccessToken = tinkManager.aead.encrypt(accessToken.toByteArray(), null)
            val encryptedAccessString = Base64.encodeToString(encryptedAccessToken, Base64.DEFAULT)

            context.dataStore.edit { preferences ->
                preferences[ACCESS_TOKEN_KEY] = encryptedAccessString
            }
        }
    }

    suspend fun saveRole(role: String) {
        withContext(Dispatchers.IO) {
            val encryptedRole = tinkManager.aead.encrypt(role.toByteArray(), null)
            val encryptedRoleString = Base64.encodeToString(encryptedRole, Base64.DEFAULT)

            context.dataStore.edit { preferences ->
                preferences[ACCESS_ROLE_KEY] = encryptedRoleString
            }
        }
    }

    suspend fun saveIdProfile(idProfile: String) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { preferences ->
                preferences[ID_PROFILE_KEY] = idProfile
            }
        }
    }

    suspend fun saveProfileBasicData(
        idProfile: String?,
        profileName: String?,
        profilePhoto: String?,
        profileJson: String?
    ) {
        withContext(Dispatchers.IO) {
            context.dataStore.edit { preferences ->
                if (!idProfile.isNullOrBlank()) {
                    preferences[ID_PROFILE_KEY] = idProfile
                }
                if (!profileName.isNullOrBlank()) {
                    preferences[PROFILE_NAME_KEY] = profileName
                }
                if (!profilePhoto.isNullOrBlank()) {
                    preferences[PROFILE_PHOTO_KEY] = profilePhoto
                }
                if (!profileJson.isNullOrBlank()) {
                    preferences[PROFILE_JSON_KEY] = profileJson
                }
            }
        }
    }
}
