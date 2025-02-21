package leo.rios.officium.core.tinkCrypt

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager

class TinkManager(context: Context) {


    companion object{
        private const val KEYSET_NAME = "tink_keyset"                               /* Nombre del archivo donde se almacenará el conjunto de claves (keyset) en las preferencias compartidas. */
        private const val PREFERENCE_FILE = "tink_prefs"                            /* Nombre del archivo de preferencias que se usará para almacenar el conjunto de claves.*/
        private const val MASTER_KEY_URI = "android-keystore://tink_master_key"     /*: URI que apunta a una clave maestra almacenada en el sistema de claves de Android (Android Keystore). Esta clave se usa para cifrar el keyset.*/
    }

    val aead: Aead

    //Inicialización
    init {
        AeadConfig.register()                                                       /*Inicializa las configuraciones necesarias para usar las primitivas de AEAD proporcionadas por Tink. Es un paso obligatorio para usar Tink.*/

        //Creación o carga de un conjunto de claves
        val keysetHandle: KeysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, PREFERENCE_FILE)                  /*Especifica que el conjunto de claves (keyset) se almacenará en SharedPreferences del dispositivo.*/
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))                        /*Define el esquema de la clave. En este caso, AES256_GCM se usa para el cifrado autenticado con AES de 256 bits y el modo GCM.*/
            .withMasterKeyUri(MASTER_KEY_URI)                                       /*Especifica que la clave maestra estará almacenada en el Android Keystore, una ubicación segura administrada por el sistema operativo.*/
            .build()                                                                /*Construye el AndroidKeysetManager. Si el keyset ya existe, lo recupera; de lo contrario, crea uno nuevo.*/
            .keysetHandle                                                           /*Representa un identificador del keyset que se puede usar para realizar operaciones criptográficas.*/

        aead = keysetHandle.getPrimitive(Aead::class.java)                          /*getPrimitive: Crea una instancia de AEAD a partir del keyset cargado.
                                                                                      AEAD: Permite realizar operaciones de cifrado y descifrado con autenticación. Verás su uso en la clase que utiliza este TinkManager.*/
    }

}

/*

1. PROPÓSITO GENERAL.

    - TinkManager administra un sistema de claves criptográficas utilizando:

    - AndroidKeysetManager: Permite almacenar claves en un almacenamiento seguro en Android.

    - Tink AEAD (Authenticated Encryption with Associated Data): Se utiliza para cifrar y descifrar datos de manera segura.


        COMPANION OBJECT

            Un companion object en Kotlin es una característica que permite declarar un objeto único asociado a una clase.
            Es similar al concepto de miembros estáticos en lenguajes como Java o C#.

            CARACTERÍSTICAS PRINCIPALES.

            1._ Asociado a la clase, no a una instancia:
                    Las funciones y propiedades dentro de un companion object se pueden acceder directamente desde la clase sin necesidad de crear una instancia.
            2._ Es único:
                    Solo puede haber un companion object por clase.
            3._ Se comporta como un singleton:
                    Es como una instancia única de una clase dentro de otra clase.
            4._ Puede implementar interfaces y heredar de clases (aunque no es común).



*/

