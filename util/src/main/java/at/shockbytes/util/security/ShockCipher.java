package at.shockbytes.util.security;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Martin Macheiner
 *         Date: 26.12.2016.
 */

public class ShockCipher {


    private byte[] initVector;
    private SecretKey secretKey;
    private Cipher encryptionCipher;
    private Cipher decryptionCipher;

    public ShockCipher() {
        initializeKey();
    }

    private void initializeKey(){

        try {

            SecureRandom rand = new SecureRandom();
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(256, rand);
            secretKey = generator.generateKey();
            initVector = generateInitializationVector();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initializeCiphers(String encodedIv, String encodedKey) throws Exception {

        // Check if host sends initialization data or use own created
        if (encodedIv != null) {
            initVector = Base64.decode(encodedIv, Base64.NO_WRAP);
        }
        if (encodedKey != null) {
            byte[] decodedKey = Base64.decode(encodedKey, Base64.NO_WRAP);
            secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        }
        IvParameterSpec ivParams = new IvParameterSpec(initVector);

        //Initialize encryption cipher
        encryptionCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        encryptionCipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParams);

        //Initialize decryption cipher
        decryptionCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        decryptionCipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams);
    }

    private byte[] generateInitializationVector() throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bytes = new byte[256];
        new SecureRandom().nextBytes(bytes);
        md.update(bytes);

        byte[] digest = md.digest();
        byte[] iv = new byte[16];
        for(int i = 0; i < 16; i++){
            iv[i] = digest[i*2];
        }
        return iv;
    }

    public String getEncodedSecretKey() {
        return Base64.encodeToString(secretKey.getEncoded(), Base64.NO_WRAP);
    }

    public String getEncodedInitializationVector() {
        return Base64.encodeToString(initVector, Base64.NO_WRAP);
    }

    public String encryptMessage(String plain) throws Exception {

        if(plain == null || plain.isEmpty()){
            return null;
        }

        byte[] encryptedBytes = encryptionCipher.doFinal(plain.getBytes("UTF-8"));
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP);
    }

    private byte[] encryptByteArray(byte[] payload) throws Exception {

        if(payload == null){
            return null;
        }

        return encryptionCipher.doFinal(payload);
        //return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP);
    }

    private byte[] decryptByteArray(byte[] encryptedPayload) throws Exception {

        if (encryptedPayload == null) {
            return null;
        }

        return decryptionCipher.doFinal(encryptedPayload);
    }

    public String decryptMessage(String encrypted) throws Exception {

        if(encrypted == null || encrypted.isEmpty()){
            return null;
        }

        byte[] encodedBytes = Base64.decode(encrypted, Base64.NO_WRAP);
        return new String(decryptionCipher.doFinal(encodedBytes));
    }
}
