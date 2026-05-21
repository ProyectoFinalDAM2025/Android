package leo.rios.officium.core.dataStore

import androidx.datastore.preferences.core.stringPreferencesKey

val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
val ACCESS_ROLE_KEY = stringPreferencesKey("user_role")
val APPLICATION_TOKEN_KEY = stringPreferencesKey("refresh_token")
val ID_PROFILE_KEY = stringPreferencesKey("id_profile")
val PROFILE_NAME_KEY = stringPreferencesKey("profile_name")
val PROFILE_PHOTO_KEY = stringPreferencesKey("profile_photo")
val PROFILE_JSON_KEY = stringPreferencesKey("profile_json")
