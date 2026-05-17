# OFFICIUM - Aplicacion Android

Documentacion del proyecto Android de OFFICIUM hasta el estado actual del desarrollo.

OFFICIUM es una aplicacion Android creada con Kotlin y Jetpack Compose. Su objetivo es servir como cliente movil para una plataforma orientada a usuarios desempleados y empresas, con flujos de autenticacion, registro, verificacion de cuenta, creacion de perfiles y consumo de una API externa.

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
- [10. Conexion con la API](#10-conexion-con-la-api)
- [11. Persistencia local](#11-persistencia-local)
- [12. Seguridad y cifrado](#12-seguridad-y-cifrado)
- [13. Modelos principales](#13-modelos-principales)
- [14. Recursos visuales](#14-recursos-visuales)
- [15. Pruebas](#15-pruebas)
- [16. Como ejecutar el proyecto](#16-como-ejecutar-el-proyecto)
- [17. Puntos pendientes o mejorables](#17-puntos-pendientes-o-mejorables)

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

El paquete principal de la aplicacion es:

```text
leo.rios.officium
```

La aplicacion utiliza Compose para construir la interfaz, Hilt para inyeccion de dependencias, Retrofit para comunicarse con la API, DataStore para guardar datos de sesion, Room para cache local y Tink para cifrar informacion sensible.

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
- Pantalla Home basica con cierre de sesion.
- Pantalla Detail.
- Pantalla Settings.
- Conexion preparada contra API local.
- Guardado cifrado del token y rol del usuario.
- Cache local de sectores y provincias con Room.
- Subida de imagenes mediante multipart.

Tambien existen pantallas y flujos que todavia parecen estar en una fase temprana o temporal, especialmente la redireccion desde `Splash` cuando el usuario tiene email o perfil pendiente.

## 3. Tecnologias utilizadas

| Tecnologia | Uso dentro del proyecto |
| --- | --- |
| Kotlin | Lenguaje principal de desarrollo |
| Jetpack Compose | Construccion de interfaces declarativas |
| Material 3 | Componentes visuales de la interfaz |
| Navigation Compose | Navegacion entre pantallas |
| Kotlin Serialization | Rutas tipadas y paso de datos entre pantallas |
| Hilt / Dagger | Inyeccion de dependencias |
| Retrofit | Cliente HTTP para consumir la API |
| Gson Converter | Conversion JSON en Retrofit |
| OkHttp | Cliente HTTP e interceptor de cabeceras |
| Coroutines / Flow | Operaciones asincronas y estados reactivos |
| DataStore Preferences | Persistencia local de datos de sesion |
| Google Tink | Cifrado de token y rol |
| Room | Base de datos local |
| Coil | Carga de imagenes |
| JUnit / Espresso | Base para pruebas unitarias e instrumentadas |

## 4. Estructura del proyecto

La logica principal se encuentra en:

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
|   +-- session/
|   +-- tinkCrypt/
+-- login/
+-- registro/
+-- recover/
+-- verificationCode/
+-- verifyProfile/
+-- desempleadoProfile/
+-- empresaProfile/
+-- home/
+-- detail/
+-- settings/
+-- splash/
+-- ui/theme/
```

### Paquete `core`

Contiene codigo compartido por toda la aplicacion:

- Cliente Retrofit.
- Interfaz de endpoints.
- DataStore.
- Base de datos Room.
- Navegacion.
- Estado de autenticacion.
- Cifrado con Tink.

### Paquetes funcionales

Cada funcionalidad principal esta separada en paquetes propios:

- `login`: inicio de sesion.
- `registro`: registro de usuario.
- `recover`: recuperacion de contrasena.
- `verificationCode`: validacion del codigo recibido por email.
- `verifyProfile`: seleccion del tipo de perfil.
- `desempleadoProfile`: formulario de perfil para usuarios desempleados.
- `empresaProfile`: formulario de perfil para empresas.
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

Compose esta activado mediante:

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
- Composables reutilizables.
- ViewModels.
- Modelos de entrada usados por formularios.

Ejemplo:

```text
login/
+-- presentation/
|   +-- views/
|   +-- viewModel/
|   +-- model/
|   +-- composables/
+-- domain/
+-- data/
```

### Domain

Contiene los repositorios que coordinan la comunicacion entre ViewModel, API, DataStore y base de datos local.

### Data

Contiene modelos de respuesta que representan los datos recibidos desde la API.

## 7. Navegacion de la aplicacion

La navegacion esta centralizada en:

```text
core/navigation/NavigationApp.kt
core/navigation/Screens.kt
```

El proyecto usa rutas tipadas con `@Serializable` y, cuando hace falta pasar objetos complejos, utiliza `Parcelable` con `@Parcelize`.

Rutas definidas:

- `Splash`
- `Login`
- `Register`
- `Recover`
- `Home`
- `Detail`
- `Settings`
- `VerificationCode`
- `VerifyProfile`
- `VerifyUnemployedProfile`
- `VerifyCompanyProfile`

Datos que se pasan entre pantallas:

- `VerificationData`: email, email de la app e id de usuario.
- `VerifyData`: email de la app e id de usuario.
- `SettingsInfo`: nombre, id y modo oscuro.

El punto inicial de la navegacion es:

```kotlin
startDestination = Splash
```

Desde `Splash`, la aplicacion comprueba el estado de autenticacion y redirige a:

- `Login`, si no hay sesion.
- `Home`, si el usuario esta autenticado.

Actualmente los estados `EMAIL_PENDING` y `PROFILE_PENDING` redirigen temporalmente a `Login`.

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
2. `LoginViewModel` valida que los campos no esten vacios.
3. Se crea un `LogInModel`.
4. `LogInRepository` llama a `apiLogIn`.
5. Si la respuesta es correcta, se guarda:
   - token de acceso.
   - rol del usuario.
6. El estado pasa a `AUTHENTICATED`.

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
3. Si el codigo es valido, se consulta el usuario autenticado.
4. Si el email aparece verificado, se crea un objeto `VerifyData`.
5. El estado pasa a `PROFILE_PENDING`.

### Cierre de sesion

El cierre de sesion limpia el DataStore mediante:

```kotlin
dataStoreManager.deleteStore()
```

Despues se limpian token, id de perfil y estado local del ViewModel.

## 9. Pantallas implementadas

### Splash

Pantalla inicial de carga. Ejecuta la comprobacion de sesion desde `LoginViewModel` y decide la ruta de entrada.

### Login

Pantalla de inicio de sesion. Permite:

- Introducir email.
- Introducir contrasena.
- Enviar credenciales a la API.
- Navegar hacia registro o recuperacion, segun la interfaz definida.

### Registro

Pantalla para crear usuario. Trabaja con:

- `RegisterViewModel`
- `RegisterRepository`
- endpoint `register`

### Recuperacion

Pantalla para solicitar recuperacion de contrasena. Trabaja con:

- `RecoverViewModel`
- `RecoverRepository`
- endpoint `recover`

### Verificacion de codigo

Pantalla para confirmar el codigo enviado por email. Trabaja con:

- `VerificationCodeViewModel`
- `VerificationCodeRepository`
- endpoint `verifyCode`
- endpoint `user`

### Seleccion de perfil

Pantalla donde el usuario elige entre:

- Crear perfil de desempleado.
- Crear perfil de empresa.

### Perfil de desempleado

Formulario para crear perfil de usuario desempleado.

Campos gestionados:

- Nombre.
- Apellido.
- DNI/NIE.
- Portfolio.
- Disponibilidad.
- Foto opcional.

El formulario prepara la imagen como `MultipartBody.Part` y envia los datos al endpoint `desempleado`.

### Perfil de empresa

Formulario para crear perfil de empresa.

Campos gestionados:

- Nombre de empresa.
- CIF.
- Sector.
- Ubicacion.
- Sitio web.
- Foto opcional.

Tambien carga catalogos de:

- Sectores.
- Provincias.

Estos catalogos se obtienen de API y se cachean localmente con Room.

### Home

Pantalla principal provisional tras autenticacion. Incluye:

- Barra superior con nombre de la app.
- Menu de opciones.
- Accion de cierre de sesion.
- Navegacion a detalle.

### Detail

Pantalla de detalle que recibe un nombre como parametro de navegacion.

### Settings

Pantalla de ajustes que recibe un objeto `SettingsInfo`.

## 10. Conexion con la API

La configuracion de red esta en:

```text
core/api/ApiClient.kt
core/api/ApiService.kt
core/api/ApiErrorParser.kt
```

La URL base configurada es:

```text
http://10.0.2.2:8000/api/
```

Esta direccion se usa habitualmente desde el emulador Android para acceder al servidor local de la maquina anfitriona.

### Endpoints definidos

| Metodo | Ruta | Funcion | Uso |
| --- | --- | --- | --- |
| POST | `login` | `apiLogIn` | Inicio de sesion |
| POST | `register` | `apiAddUser` | Registro de usuario |
| POST | `recover` | `apiRecoverPassword` | Recuperacion de contrasena |
| POST | `verifyCode` | `apiVerificationCode` | Verificacion de codigo |
| POST | `desempleado` | `apiRegisterProfile` | Registro de perfil desempleado |
| POST | `empresa` | `apiRegisterCompanyProfile` | Registro de perfil empresa |
| POST | `logout` | `apiLogout` | Cierre de sesion en API |
| GET | `testAuth` | `testAuth` | Prueba de autenticacion |
| GET | `user` | `authenticatedUser` | Obtener usuario autenticado |
| GET | `sector` | `apiGetSectors` | Obtener sectores |
| GET | `provincia` | `apiGetProvincias` | Obtener provincias |

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

`ApiErrorParser.kt` intenta leer errores JSON devueltos por la API y extraer:

- `Message`
- `ReasonPhrase`

Tambien soporta mensajes con arrays u objetos anidados, convirtiendolos a texto legible.

## 11. Persistencia local

El proyecto usa dos mecanismos de persistencia local:

### DataStore Preferences

Se usa para guardar datos de sesion:

- `access_token`
- `user_role`
- `refresh_token`
- `id_profile`

El DataStore se declara como:

```kotlin
val Context.dataStore by preferencesDataStore(name = "auth_prefs")
```

Funciones principales:

- `guardarTokens`
- `getAccessToken`
- `getApplicationToken`
- `getIdProfile`
- `getRole`
- `deleteStore`
- `saveAccessToken`
- `saveRole`
- `saveIdProfile`

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
- Evitar llamadas repetidas si ya existen datos locales.

## 12. Seguridad y cifrado

La clase `TinkManager` configura Google Tink con AEAD.

Configuracion usada:

- Keyset: `tink_keyset`
- SharedPreferences para keyset: `tink_prefs`
- Master key en Android Keystore: `android-keystore://tink_master_key`
- Plantilla criptografica: `AES256_GCM`

Actualmente se cifran antes de guardarse:

- Token de acceso.
- Rol del usuario.

El id de perfil se guarda sin cifrar.

La aplicacion tambien tiene permiso de internet en el manifest:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

Y permite trafico HTTP claro hacia `10.0.2.2` mediante:

```text
res/xml/network_security_config.xml
```

Esto es util en desarrollo local con servidor HTTP.

## 13. Modelos principales

### Login

- `LogInModel`
- `LoginResponse`
- `AuthData`
- `AuthenticatedUserResponse`
- `ApiMessageResponse`

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

### Perfil

- `VerifyData`
- `VerifyProfilModel`
- `RegisterClientResponse`
- `RegisterClientData`
- `SaverUser`
- `DesempleadoProfileModel`
- `EmpresaProfileModel`

### Catalogos

- `SectorResponse`
- `SectorData`
- `ProvinciaResponse`
- `ProvinciaData`
- `SectorEntity`
- `ProvinciaEntity`

## 14. Recursos visuales

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

Tambien existen los iconos launcher generados en las carpetas `mipmap-*`.

La configuracion de tema esta repartida entre:

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

## 15. Pruebas

El proyecto conserva las pruebas base creadas por Android Studio:

```text
app/src/test/java/leo/rios/officium/ExampleUnitTest.kt
app/src/androidTest/java/leo/rios/officium/ExampleInstrumentedTest.kt
```

Dependencias de pruebas incluidas:

- JUnit.
- AndroidX JUnit.
- Espresso.
- Compose UI Test JUnit4.

Por ahora no se observan pruebas especificas para los flujos reales de login, registro, perfiles, DataStore, Room o API.

## 16. Como ejecutar el proyecto

### Requisitos

- Android Studio.
- JDK compatible con Gradle/Android Studio.
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

Si se usa emulador Android, `10.0.2.2` representa el `localhost` de la maquina donde se ejecuta el backend.

Si se usa un dispositivo fisico, sera necesario cambiar la URL base por la IP local del ordenador o por una URL publica accesible desde el dispositivo.

## 17. Puntos pendientes o mejorables

Aspectos detectados que conviene revisar mas adelante:

- Redirigir correctamente desde `Splash` cuando el usuario este en `EMAIL_PENDING` o `PROFILE_PENDING`.
- Revisar si `minSdk = 35` es intencionado, porque limita la app a dispositivos muy recientes.
- Extraer la URL base de la API a configuracion por entorno.
- Completar pruebas unitarias para repositorios y ViewModels.
- Completar pruebas de UI para los flujos principales.
- Revisar textos visibles que aun estan en ingles o mezclados con espanol.
- Corregir textos con caracteres mal codificados en algunos mensajes internos.
- Evaluar si el id de perfil tambien deberia cifrarse en DataStore.
- Llamar al endpoint `logout` de la API si se desea invalidar la sesion en servidor, no solo limpiar el almacenamiento local.
- Mejorar la pantalla Home, que actualmente parece provisional.
