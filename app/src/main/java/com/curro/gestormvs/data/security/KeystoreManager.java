package com.curro.gestormvs.data.security;

import android.content.Context;
import android.util.Log;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.RegistryConfiguration;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import com.google.crypto.tink.integration.android.AndroidKeysetManager;

import java.nio.charset.StandardCharsets;
import java.util.Base64;


public class KeystoreManager {

    private static final String KEYSET_NAME      = "GAMV_Keystore";
    private static final String PREF_FILE_NAME   = "keyset_prefs";
    private static final String MASTER_KEY_URI   = "android-keystore://gamv_key";

    private final Aead aead; // Encryption scheme


    public KeystoreManager(Context context) throws Exception {
        AeadConfig.register();

        // Create or load keyset, protected by the Android Keystore System
        KeysetHandle keysetHandle = new AndroidKeysetManager.Builder()
                .withSharedPref(context, KEYSET_NAME, PREF_FILE_NAME)
                .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
                .withMasterKeyUri(MASTER_KEY_URI)
                .build()
                .getKeysetHandle();

        aead = keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead.class);
    }

    /**
     * Encrypts a String and returns the result as a Base64-encoded string.
     *
     * @param plainText Plain text to encrypt
     * @param aad       Authenticated associated data (can be "" if not needed)
     * @return          Base64-encoded ciphertext, or null if encryption failed
     */
    public String encrypt(String plainText, String aad) throws Exception {
            byte[] ciphertext = aead.encrypt(
                    plainText.getBytes(StandardCharsets.UTF_8),
                    aad.getBytes(StandardCharsets.UTF_8)
            );
            return Base64.getEncoder().encodeToString(ciphertext);
    }

    /**
     * Decrypts a Base64-encoded encrypted String.
     *
     * @param encryptedText Base64-encoded ciphertext
     * @param aad           The same associated data used during encryption
     * @return              Decrypted plain text, or null if decryption failed
     */
    public String decrypt(String encryptedText, String aad) throws Exception {
        byte[] ciphertext = Base64.getDecoder().decode(encryptedText);
        byte[] plaintext  = aead.decrypt(
                ciphertext,
                aad.getBytes(StandardCharsets.UTF_8)
        );
        return new String(plaintext, StandardCharsets.UTF_8);
    }

}
