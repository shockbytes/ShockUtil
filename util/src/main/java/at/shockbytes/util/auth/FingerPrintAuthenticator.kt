package at.shockbytes.util.auth

import android.app.Activity
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat
import android.support.v4.os.CancellationSignal
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * @author Martin Macheiner
 * Date: 26.04.2017.
 */


abstract class FingerPrintAuthenticator(private val context: Activity,
                                        private val fingerprintManager: FingerprintManagerCompat) {

    abstract val keyName: String

    private var keyStore: KeyStore? = null
    private var cipher: Cipher? = null

    private val cryptoObject: FingerprintManagerCompat.CryptoObject?
        get() = if (cipher != null) {
            FingerprintManagerCompat.CryptoObject(cipher)
        } else null

    fun setup(): Boolean {
        generateKey()
        return initializeCipher()
    }

    fun authenticate(callback: FingerprintManagerCompat.AuthenticationCallback) {
        fingerprintManager.authenticate(cryptoObject, 0, CancellationSignal(),
                callback, null)
    }

    fun deriveRealmKey(result: FingerprintManagerCompat.AuthenticationResult?): ByteArray {
        // TODO
        return ByteArray(0)
    }

    fun deriveKey(result: FingerprintManagerCompat.AuthenticationResult?, len: Int): ByteArray {
        // TODO
        return ByteArray(0)
    }

    private fun generateKey() {

        try {

            keyStore = KeyStore.getInstance("AndroidKeyStore")
            val keyGenerator = KeyGenerator.getInstance("AES", "AndroidKeyStore")
            keyStore?.load(null)

            if (keyStore?.getKey(keyName, null) == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    keyGenerator.init(KeyGenParameterSpec.Builder(keyName,
                            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                            .setUserAuthenticationRequired(true)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                            .build())
                } else {
                    keyGenerator.init(SecureRandom())
                }
                val key = keyGenerator.generateKey()
                keyStore?.setKeyEntry(keyName, key, null, null)
                keyStore?.store(null)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun initializeCipher(): Boolean {

        return try {
            cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")

            keyStore?.load(null)
            val key = keyStore?.getKey(keyName, null) as SecretKey
            cipher?.init(Cipher.ENCRYPT_MODE, key)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}
