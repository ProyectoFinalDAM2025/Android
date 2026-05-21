# OFFICIUM - Aplicacion Android

Documentacion del proyecto Android de OFFICIUM hasta el estado actual del desarrollo.

OFFICIUM es una aplicacion Android creada con Kotlin y Jetpack Compose. Funciona como cliente movil para una plataforma orientada a usuarios desempleados y empresas. La app incluye autenticacion, registro, verificacion, creacion de perfiles, gestion de sesion, perfil de usuario, publicaciones, documentos multimedia y conexion con una API local.

## Indice

- [1. Resumen del proyecto](#1-resumen-del-proyecto)
- [2. Estado actual](#2-estado-actual)
- [3. Tecnologias utilizadas](#3-tecnologias-utilizadas)
- [4. Estructura del proyecto](#4-estructura-del-proyecto)
- [5. Configuracion Android y Gradle](#5-configuracion-android-y-gradle)
- [6. Arquitectura general](#6-arquitectura-general)
- [7. Navegacion de la aplicacion](#7-navegacion-de-la-aplicacion)
- [8. Flujo de autenticacion y sesion](#8-flujo-de-autenticacion-y-sesion)
- [9. Pantallas implementadas](#9-pantallas-implementadas)
- [10. Perfil de usuario, publicaciones y documentos](#10-perfil-de-usuario-publicaciones-y-documentos)
- [11. Conexion con la API](#11-conexion-con-la-api)
- [12. Persistencia local](#12-persistencia-local)
- [13. Seguridad y cifrado](#13-seguridad-y-cifrado)
- [14. Modelos principales](#14-modelos-principales)
- [15. Componentes reutilizables](#15-componentes-reutilizables)
- [16. Recursos visuales](#16-recursos-visuales)
- [17. Pruebas](#17-pruebas)
- [18. Como ejecutar el proyecto](#18-como-ejecutar-el-proyecto)
- [19. Puntos pendientes o mejorables](#19-puntos-pendientes-o-mejorables)

## 1. Resumen del proyecto

El proyecto Android se encuentra en la carpeta `Android/` y tiene como nombre de raiz Gradle `OFFICIUM`.

La aplicacion esta organizada como un unico modulo Android:

```text
Android/
+-- app/
+-- gradle/
+-- build.gradle.kts
+-- settings.gradle.kts
+-- gradle.properties
+-- gradlew
+-- gradlew.bat
+-- README.md
```

El paquete principal es:

```text
leo.rios.officium
```

La aplicacion utiliza Compose para construir la interfaz, Hilt para inyeccion de dependencias, Retrofit para comunicarse con la API, DataStore para guardar datos de sesion, Room para cache local, Tink para cifrado, Coil para imagenes y Media3/ExoPlayer para video.

## 2. Estado actual

Actualmente el proyecto incluye:

- Pantalla de splash inicial.
- Login de usuario.
- Registro de usuario.
- Recuperacion de contrasena.
- Verificacion de codigo por email.
- Seleccion del tipo de perfil.
- Creacion de perfil de desempleado.
- Creacion de perfil de empresa.
- Home con barra inferior reutilizable.
- Acceso al perfil desde Home.
- Perfil de usuario con cabecera, foto, rol, descripcion y metricas.
- Edicion de perfil de desempleado y empresa.
- Listado de publicaciones del usuario.
- Listado de fotos, videos y PDFs del usuario.
- Creacion de publicaciones con archivo opcional.
- Subida de documentos como foto, video o PDF.
- Reproduccion de video con Media3.
- Previsualizacion de PDFs mediante descarga a cache y render local.
- Logout contra API y limpieza local de sesion.
- Conexion preparada contra API local.
- Guardado cifrado del token y rol del usuario.
- Guardado local de datos basicos del perfil.
- Cache local de sectores y provincias con Room.
- Subida de imagenes y archivos mediante multipart.

Todavia hay pantallas y textos con aspecto provisional, especialmente la pantalla `Home`, algunos textos en ingles y la redireccion temporal desde `Splash` para estados pendientes.

## 3. Tecnologias utilizadas

| Tecnologia | Uso dentro del proyecto |
| --- | --- |
| Kotlin | Lenguaje principal de desarrollo |
| Jetpack Compose | Construccion de interfaces declarativas |
| Material 3 | Componentes visuales |
| Navigation Compose | Navegacion entre pantallas |
| Kotlin Serialization | Rutas tipadas y paso de datos |
| Kotlin Parcelize | Objetos navegables parcelables |
| Hilt / Dagger | Inyeccion de dependencias |
| Retrofit | Cliente HTTP para consumir la API |
| Gson Converter | Conversion JSON en Retrofit |
| OkHttp | Cliente HTTP, interceptor y multipart |
| Coroutines / Flow | Operaciones asincronas y estados reactivos |
| DataStore Preferences | Persistencia local de datos de sesion |
| Google Tink | Cifrado de token y rol |
| Room | Base de datos local |
| Coil 3 | Carga de imagenes remotas |
| Coil Network OkHttp | Soporte de red para Coil |
| Media3 ExoPlayer | Reproduccion de videos |
| Android PdfRenderer | Previsualizacion de PDFs |
| JUnit / Espresso | Base para pruebas unitarias e instrumentadas |

## 4. Estructura del proyecto

La logica principal esta en:

```text
app/src/main/java/leo/rios/officium/
```

Estructura general de paquetes:

```text
leo.rios.officium/
+-- core/
|   +-- api/
|   +-- database/
|   +-- dataStore/
|   +-- navigation/
|   +-- presentation/components/
|   +-- session/
|   +-- tinkCrypt/
+-- login/
+-- registro/
+-- recover/
+-- verificationCode/
+-- verifyProfile/
+-- desempleadoProfile/
+-- empresaProfile/
+-- userProfile/
+-- home/
+-- detail/
+-- settings/
+-- splash/
+-- ui/theme/
```

### Paquete `core`

Contiene codigo compartido:

- Cliente Retrofit.
- Interfaz de endpoints.
- Utilidad para convertir rutas de storage en URLs completas.
- DataStore.
- Base de datos Room.
- Navegacion.
- Componentes reutilizables.
- Estado de autenticacion.
- Cifrado con Tink.

### Paquetes funcionales

- `login`: inicio de sesion, logout y guardado de datos de perfil.
- `registro`: registro de usuario.
- `recover`: recuperacion de contrasena.
- `verificationCode`: validacion del codigo recibido por email.
- `verifyProfile`: seleccion del tipo de perfil.
- `desempleadoProfile`: formulario de perfil para desempleados.
- `empresaProfile`: formulario de perfil para empresas.
- `userProfile`: perfil del usuario, documentos, publicaciones y edicion.
- `home`: pantalla principal tras autenticacion.
- `detail`: pantalla de detalle.
- `settings`: pantalla de ajustes.
- `splash`: pantalla inicial.

## 5. Configuracion Android y Gradle

El modulo principal es `:app`.

Configuracion relevante:

```kotlin
namespace = "leo.rios.officium"
applicationId = "leo.rios.officium"
compileSdk = 35
minSdk = 35
targetSdk = 35
versionCode = 1
versionName = "1.0"
```

La app usa Java 11:

```kotlin
sourceCompatibility = JavaVersion.VERSION_11
targetCompatibility = JavaVersion.VERSION_11
jvmTarget = "11"
```

Compose esta activado:

```kotlin
buildFeatures {
    compose = true
}
```

Plugins principales:

- `com.android.application`
- `org.jetbrains.kotlin.android`
- `org.jetbrains.kotlin.plugin.compose`
- `org.jetbrains.kotlin.plugin.serialization`
- `org.jetbrains.kotlin.plugin.parcelize`
- `kotlin-kapt`
- `com.google.dagger.hilt.android`

Dependencias nuevas relevantes:

- `io.coil-kt.coil3:coil-network-okhttp`
- `androidx.media3:media3-exoplayer`
- `androidx.media3:media3-ui`

## 6. Arquitectura general

El proyecto sigue una organizacion cercana a MVVM:

```text
Pantalla Compose
    ->
ViewModel
    ->
Repository
    ->
ApiService / DataStore / Room
```

### Presentation

Incluye:

- Pantallas Compose.
- Componentes reutilizables.
- ViewModels.
- Modelos de entrada o estados visuales.

Ejemplo:

```text
userProfile/
+-- presentation/
|   +-- view/
|   +-- viewModel/
|   +-- model/
|   +-- composables/
+-- domain/
+-- data/
```

### Domain

Contiene repositorios que coordinan API, DataStore y Room.

### Data

Contiene respuestas de API y DTOs.

## 7. Navegacion de la aplicacion

La navegacion esta centralizada en:

```text
core/navigation/NavigationApp.kt
core/navigation/Screens.kt
```

El proyecto usa rutas tipadas con `@Serializable`. Para objetos complejos usa `Parcelable` con `@Parcelize`.

Rutas definidas:

- `Splash`
- `Login`
- `Register`
- `Recover`
- `Home`
- `Profile`
- `Detail`
- `Settings`
- `VerificationCode`
- `VerifyProfile`
- `VerifyUnemployedProfile`
- `VerifyCompanyProfile`

El punto inicial es:

```kotlin
startDestination = Splash
```

Desde `Splash`, la app comprueba la sesion y redirige a:

- `Login`, si no hay sesion.
- `Home`, si el usuario esta autenticado.

Los estados `EMAIL_PENDING` y `PROFILE_PENDING` siguen redirigiendo temporalmente a `Login`.

La ruta `Profile` abre `UserProfileScreen`. Tanto `Home` como `Profile` protegen la pantalla: si el estado pasa a `LOGGED_OUT`, navegan a `Login` limpiando el back stack.

## 8. Flujo de autenticacion y sesion

El estado de autenticacion se representa con:

```kotlin
enum class AuthState {
    LOGGED_OUT,
    EMAIL_PENDING,
    PROFILE_PENDING,
    AUTHENTICATED
}
```

### Inicio de sesion

Flujo actual:

1. El usuario introduce email y contrasena.
2. `LoginViewModel` valida que no esten vacios.
3. Se crea un `LogInModel`.
4. `LogInRepository` llama a `apiLogIn`.
5. Si la respuesta es correcta, se guarda:
   - token de acceso.
   - rol normalizado.
   - id de perfil.
   - nombre de perfil.
   - foto de perfil.
   - JSON completo del perfil.
6. El estado pasa a `AUTHENTICATED`.

El rol se normaliza con `normalizeProfileRole`:

- `usuario` o `desempleado` pasan a `Desempleado`.
- `empresa` pasa a `Empresa`.

### Registro

Flujo actual:

1. El usuario introduce email y contrasena.
2. `RegisterViewModel` valida los campos.
3. `RegisterRepository` llama a `apiAddUser`.
4. Si la API devuelve token, se guarda en DataStore.
5. El estado pasa a `EMAIL_PENDING`.

### Recuperacion de contrasena

Flujo actual:

1. El usuario introduce su email.
2. `RecoverViewModel` valida que no este vacio.
3. `RecoverRepository` llama a `apiRecoverPassword`.
4. Se muestra el mensaje devuelto por la API.

### Verificacion de codigo

Flujo actual:

1. El usuario introduce el codigo recibido.
2. `VerificationCodeViewModel` envia email y codigo a la API.
3. Si el codigo es valido, consulta el usuario autenticado.
4. Si el email esta verificado, crea un `VerifyData`.
5. El estado pasa a `PROFILE_PENDING`.

### Cierre de sesion

El logout ahora llama a la API mediante:

```kotlin
apiLogout()
```

Despues limpia DataStore y estados locales:

- token.
- id de perfil.
- foto de perfil.
- rol.
- nombre.
- JSON de perfil.

Aunque la llamada de logout falle, la app limpia la sesion local y vuelve a `Login`.

## 9. Pantallas implementadas

### Splash

Pantalla inicial. Ejecuta `checkAuthStatus` y decide la ruta de entrada.

### Login

Pantalla de inicio de sesion. Permite enviar credenciales, guardar sesion y navegar al flujo autenticado.

### Registro

Pantalla para crear usuario con `RegisterViewModel`, `RegisterRepository` y endpoint `register`.

### Recuperacion

Pantalla para solicitar recuperacion de contrasena mediante endpoint `recover`.

### Verificacion de codigo

Pantalla para validar el codigo enviado por email. Usa endpoints `verifyCode` y `user`.

### Seleccion de perfil

Pantalla donde el usuario elige entre:

- Crear perfil de desempleado.
- Crear perfil de empresa.

### Perfil de desempleado

Formulario para crear perfil de desempleado.

Campos gestionados:

- Nombre.
- Apellido.
- DNI/NIE.
- Portfolio.
- Disponibilidad.
- Ubicacion.
- Foto opcional.

El formulario envia datos al endpoint `desempleado` como multipart.

### Perfil de empresa

Formulario para crear perfil de empresa.

Campos gestionados:

- Nombre de empresa.
- CIF.
- Sector.
- Ubicacion.
- Sitio web.
- Foto opcional.

Tambien carga catalogos de sectores y provincias, obtenidos desde API y cacheados con Room.

### Home

Pantalla principal provisional tras autenticacion. Incluye:

- Barra superior.
- Menu con cierre de sesion.
- Acceso al perfil mediante la barra inferior.
- Barra inferior `OfficiumBottomNavigation`.
- Foto de perfil cargada desde la sesion local.

### Profile

Pantalla nueva de perfil de usuario. Permite ver, editar y subir contenido relacionado con el usuario autenticado.

### Detail

Pantalla de detalle que recibe un nombre como parametro.

### Settings

Pantalla de ajustes que recibe un objeto `SettingsInfo`.

## 10. Perfil de usuario, publicaciones y documentos

La funcionalidad de perfil esta en:

```text
userProfile/
+-- data/
+-- domain/
+-- presentation/
```

### Datos cargados en el perfil

`UserProfileViewModel` carga:

- Nombre del perfil.
- Rol.
- Foto.
- Descripcion generada desde el JSON del perfil.
- JSON completo del perfil.
- Sectores.
- Provincias.
- Publicaciones.
- Fotos.
- Videos.
- PDFs.

### Tabs del perfil

El perfil usa `ProfileTab`:

```kotlin
enum class ProfileTab {
    Posts,
    Photos,
    Videos,
    Pdfs
}
```

### Subida de contenido

La subida se controla con `ProfileUploadType`:

```kotlin
Publication
Photo
Video
Pdf
```

Tipos soportados:

| Tipo | Uso | MIME |
| --- | --- | --- |
| `Publication` | Crear publicacion | `*/*` |
| `Photo` | Subir foto/documento | `image/*` |
| `Video` | Subir video/documento | `video/*` |
| `Pdf` | Subir PDF/documento | `application/pdf` |

Las publicaciones se envian al endpoint `publicacion` y los documentos al endpoint `documento`.

### Listado de publicaciones

`ProfilePublicationList` muestra publicaciones del usuario. Soporta:

- Texto de publicacion.
- Imagen adjunta.
- Video adjunto.
- PDF adjunto.

Los videos dentro de publicaciones usan `OfficiumVideoPlayer`. La lista detecta el video mas visible y reproduce ese elemento.

### Grid de documentos

`ProfileDocumentGrid` muestra:

- Fotos en grid.
- Videos con thumbnail e icono de reproduccion.
- PDFs con icono y nombre de archivo.

### Previsualizacion de contenido

La pantalla de perfil incluye:

- Dialogo de video.
- Dialogo de PDF.
- Render de pagina PDF con `PdfRenderer`.
- Descarga temporal del PDF a cache.
- Navegacion entre paginas del PDF con botones anterior/siguiente.

### Edicion de perfil

Desde el perfil se puede editar:

Perfil de empresa:

- Nombre de empresa.
- CIF.
- Sector.
- Ubicacion.
- Sitio web.

Perfil de desempleado:

- Nombre.
- Apellido.
- DNI/NIE.
- Portfolio.
- Disponibilidad.
- Ubicacion.

La actualizacion se envia como multipart usando `_method = PUT` sobre:

- `desempleado/{id}`
- `empresa/{id}`

Al actualizar correctamente, se guarda de nuevo el perfil en DataStore.

## 11. Conexion con la API

La configuracion de red esta en:

```text
core/api/ApiClient.kt
core/api/ApiService.kt
core/api/ApiErrorParser.kt
core/api/StorageUrl.kt
```

La URL base de la API es:

```text
http://10.0.2.2:8000/api/
```

La URL base para archivos de storage es:

```text
http://10.0.2.2:8000
```

`toStorageUrl()` convierte rutas relativas en URLs completas y deja intactas URLs que ya empiezan por `http://` o `https://`.

### Endpoints definidos

| Metodo | Ruta | Funcion | Uso |
| --- | --- | --- | --- |
| POST | `login` | `apiLogIn` | Inicio de sesion |
| POST | `register` | `apiAddUser` | Registro de usuario |
| POST | `recover` | `apiRecoverPassword` | Recuperacion de contrasena |
| POST | `verifyCode` | `apiVerificationCode` | Verificacion de codigo |
| POST | `desempleado` | `apiRegisterProfile` | Crear perfil desempleado |
| POST | `empresa` | `apiRegisterCompanyProfile` | Crear perfil empresa |
| POST | `logout` | `apiLogout` | Logout en API |
| GET | `testAuth` | `testAuth` | Prueba de autenticacion |
| GET | `user` | `authenticatedUser` | Obtener usuario autenticado |
| GET | `sector` | `apiGetSectors` | Obtener sectores |
| GET | `provincia` | `apiGetProvincias` | Obtener provincias |
| GET | `documentos/fotosByIDUsuario` | `apiGetMyPhotos` | Fotos del usuario |
| GET | `documentos/videosByIDUsuario` | `apiGetMyVideos` | Videos del usuario |
| GET | `documentos/pdfsByIDUsuario` | `apiGetMyPdfs` | PDFs del usuario |
| GET | `publicaciones/postsByUsuario` | `apiGetMyPublications` | Publicaciones del usuario |
| POST | `publicacion` | `apiCreatePublication` | Crear publicacion |
| POST | `documento` | `apiCreateDocument` | Subir documento |
| POST | `desempleado/{id}` | `apiUpdateDesempleadoProfile` | Actualizar perfil desempleado con `_method=PUT` |
| POST | `empresa/{id}` | `apiUpdateEmpresaProfile` | Actualizar perfil empresa con `_method=PUT` |

### Interceptor HTTP

El cliente OkHttp agrega siempre:

```text
Accept: application/json
```

Si hay token guardado, tambien agrega:

```text
Authorization: Bearer <token>
```

### Manejo de errores

`ApiErrorParser.kt` intenta leer errores JSON y extraer:

- `Message`
- `ReasonPhrase`

Tambien soporta arrays u objetos anidados, convirtiendolos a texto legible.

## 12. Persistencia local

El proyecto usa DataStore y Room.

### DataStore Preferences

Se usa para guardar datos de sesion y perfil:

- `access_token`
- `user_role`
- `refresh_token`
- `id_profile`
- `profile_name`
- `profile_photo`
- `profile_json`

El DataStore se declara como:

```kotlin
val Context.dataStore by preferencesDataStore(name = "auth_prefs")
```

Funciones principales:

- `guardarTokens`
- `getAccessToken`
- `getApplicationToken`
- `getIdProfile`
- `getProfileName`
- `getProfilePhoto`
- `getProfileJson`
- `getRole`
- `deleteStore`
- `saveAccessToken`
- `saveRole`
- `saveIdProfile`
- `saveProfileBasicData`

### Room

La base de datos local se llama:

```text
officium_database
```

Entidades actuales:

- `SectorEntity`
- `ProvinciaEntity`

DAOs actuales:

- `SectorDao`
- `ProvinciaDao`

Uso principal:

- Guardar sectores recibidos desde API.
- Guardar provincias recibidas desde API.
- Reutilizar catalogos en formularios de empresa y perfil.

## 13. Seguridad y cifrado

La clase `TinkManager` configura Google Tink con AEAD.

Configuracion usada:

- Keyset: `tink_keyset`
- SharedPreferences para keyset: `tink_prefs`
- Master key en Android Keystore: `android-keystore://tink_master_key`
- Plantilla criptografica: `AES256_GCM`

Actualmente se cifran:

- Token de acceso.
- Rol del usuario.

Se guardan sin cifrar:

- Id de perfil.
- Nombre de perfil.
- Foto de perfil.
- JSON del perfil.

La app declara permiso de internet:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

Y permite trafico HTTP claro hacia `10.0.2.2` mediante:

```text
res/xml/network_security_config.xml
```

Esto facilita el desarrollo con backend local.

## 14. Modelos principales

### Login

- `LogInModel`
- `LoginResponse`
- `AuthData`
- `AuthenticatedUserResponse`
- `ApiMessageResponse`
- `ProfileJsonMapper`

`AuthData` contiene ahora:

- `token`
- `profile`
- `rol`

El perfil llega como `JsonObject`, lo que permite soportar perfiles de empresa o desempleado con estructuras diferentes.

### Registro

- `RegisterModel`
- `RegisterResponse`
- `DataRegister`

### Recuperacion

- `RecoverModel`
- `RecoverResponse`

### Verificacion

- `VerificationCodeModel`
- `VerificationCodeResponse`
- `VerificationData`

### Creacion de perfil

- `VerifyData`
- `VerifyProfilModel`
- `RegisterClientResponse`
- `RegisterClientData`
- `SaverUser`
- `DesempleadoProfileModel`
- `EmpresaProfileModel`

### Perfil de usuario

- `DocumentoListResponse`
- `DocumentoDto`
- `PublicacionListResponse`
- `PublicacionResponse`
- `DocumentoResponse`
- `PublicacionDto`
- `ProfileUpdateResponse`
- `ProfileUpdateData`
- `ProfileTab`
- `ProfileUploadType`

### Catalogos

- `SectorResponse`
- `SectorData`
- `ProvinciaResponse`
- `ProvinciaData`
- `SectorEntity`
- `ProvinciaEntity`

## 15. Componentes reutilizables

### `OfficiumBottomNavigation`

Barra inferior reutilizable con:

- Home.
- Seccion secundaria.
- Notificaciones.
- Busqueda.
- Perfil.

Carga la imagen de perfil con Coil y usa `toStorageUrl()` para resolver rutas relativas.

### `OfficiumVideoPlayer`

Componente Compose que integra Media3/ExoPlayer mediante `AndroidView`.

Caracteristicas:

- Reproduce una URL de video.
- Soporta mute.
- Soporta controles visibles u ocultos.
- Repite el video en bucle.
- Libera el player con `DisposableEffect`.

### `ProfilePublicationList`

Lista de publicaciones. Renderiza texto, imagen, video o PDF segun el tipo de archivo.

### `ProfileDocumentGrid`

Grid para fotos, videos y PDFs del usuario.

## 16. Recursos visuales

Los recursos graficos estan en:

```text
app/src/main/res/drawable/
```

Recursos actuales destacados:

- `login.png`
- `recover.png`
- `profile.png`
- `code.png`
- `acount.png`
- `acount2.png`
- `dise_o_sin_t_tulo__1_.png`

Tambien existen iconos launcher en las carpetas `mipmap-*`.

La configuracion de tema esta en:

```text
app/src/main/res/values/
app/src/main/java/leo/rios/officium/ui/theme/
```

Archivos importantes:

- `colors.xml`
- `themes.xml`
- `Color.kt`
- `Theme.kt`
- `Type.kt`

## 17. Pruebas

El proyecto conserva las pruebas base:

```text
app/src/test/java/leo/rios/officium/ExampleUnitTest.kt
app/src/androidTest/java/leo/rios/officium/ExampleInstrumentedTest.kt
```

Dependencias de pruebas incluidas:

- JUnit.
- AndroidX JUnit.
- Espresso.
- Compose UI Test JUnit4.

Por ahora no se observan pruebas especificas para login, registro, perfil, publicaciones, documentos, DataStore, Room o API.

## 18. Como ejecutar el proyecto

### Requisitos

- Android Studio.
- JDK compatible con Android Studio.
- Emulador o dispositivo con API 35, ya que `minSdk = 35`.
- Backend de OFFICIUM ejecutandose localmente en el puerto `8000`.

### Abrir en Android Studio

1. Abrir Android Studio.
2. Seleccionar la carpeta `Android/`.
3. Esperar a que Gradle sincronice.
4. Ejecutar el modulo `app`.

### Ejecutar desde terminal

En Windows:

```powershell
.\gradlew.bat assembleDebug
```

En Linux/macOS:

```bash
./gradlew assembleDebug
```

### API local

La app apunta a:

```text
http://10.0.2.2:8000/api/
```

Si se usa emulador Android, `10.0.2.2` representa el `localhost` de la maquina anfitriona.

Si se usa un dispositivo fisico, sera necesario cambiar la URL base por la IP local del ordenador o por una URL publica accesible desde el dispositivo.

## 19. Puntos pendientes o mejorables

Aspectos detectados que conviene revisar:

- Redirigir correctamente desde `Splash` cuando el usuario este en `EMAIL_PENDING` o `PROFILE_PENDING`.
- Revisar si `minSdk = 35` es intencionado, porque limita la app a dispositivos muy recientes.
- Extraer la URL base de API y storage a configuracion por entorno.
- Completar pruebas unitarias para repositorios y ViewModels.
- Completar pruebas de UI para login, perfil, publicaciones y documentos.
- Revisar textos visibles que aun estan en ingles o mezclados con espanol.
- Corregir textos con caracteres mal codificados en algunos mensajes internos.
- Evaluar si `profile_json`, `profile_name`, `profile_photo` e `id_profile` deben cifrarse.
- Revisar el comportamiento de descarga de PDFs para evitar bloqueos si el archivo es grande.
- Mejorar la pantalla Home, que todavia parece provisional.

