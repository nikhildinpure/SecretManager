package com.secrets.vault.operations.store;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;

import static com.secrets.vault.operations.common.Utility.*;

public class Passwords {

    private static String encryptText(String masterPassword,String password) throws Exception {
        SecureRandom random = new SecureRandom();
        /*
        Generate salt and IV
        Initialization Vector /Nonce
        Random per encryption. Ensures that encrypting the same file twice (with same password)
        yields different outputs
        */
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);

        byte[] iv = new byte[IV_LENGTH];
        random.nextBytes(iv);

        //Derive unique key from password and salt
        SecretKey secret = deriveKey(masterPassword,salt);

        /*
        Intialize AES-GCM cipher. An authenticated encryption mode.
        It encrypts data and attached a tag allowing the receiver
        to verify data wasn't tampered with and the password is correct
         */
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH,iv);
        cipher.init(Cipher.ENCRYPT_MODE,secret,gcmParameterSpec);

        byte[] encryptedBytes = cipher.doFinal(password.getBytes());

        byte[] combined = new byte[salt.length + iv.length + encryptedBytes.length];
        System.arraycopy(salt, 0, combined, 0, salt.length);
        System.arraycopy(iv, 0, combined, salt.length, iv.length);
        System.arraycopy(encryptedBytes, 0, combined, (salt.length+iv.length), encryptedBytes.length);

        String encodedPassword=Base64.getEncoder().encodeToString(combined);
        System.out.println(encodedPassword);
        return encodedPassword;
        //decryptText("1234",encodedPassword);
    }

    private static String decryptText(String masterPassword,String encryptedPasswordString) throws Exception {

        byte[] decodedPassword = Base64.getDecoder().decode(encryptedPasswordString);

        byte[] salt = new byte[SALT_LENGTH];
        System.arraycopy(decodedPassword,0,salt,0,salt.length);

        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(decodedPassword,salt.length,iv,0,iv.length);

        SecretKey secret = deriveKey(masterPassword,salt);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH,iv);
        cipher.init(Cipher.DECRYPT_MODE,secret,gcmParameterSpec);

        byte[] passwordBytes = new byte[decodedPassword.length - (SALT_LENGTH+IV_LENGTH)];
        System.arraycopy(decodedPassword,(SALT_LENGTH+IV_LENGTH),passwordBytes,0,passwordBytes.length);

        byte[] originalPasswordBytes = cipher.doFinal(passwordBytes);
        String originalPassword = new String(originalPasswordBytes);
        System.out.println(originalPassword);
        return originalPassword;

    }
}
