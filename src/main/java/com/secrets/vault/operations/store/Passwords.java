package com.secrets.vault.operations.store;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.secrets.vault.operations.bean.Secret;
import com.secrets.vault.operations.files.EncryptDecrypt;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.io.*;
import java.lang.reflect.Type;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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

    private static boolean storeSecret(String storePath, String appName, String masterPassword,String passwordToStore,boolean override) throws Exception {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File storeFile = new File(storePath);
        List<Secret> secrets = new ArrayList<>();
        boolean found = false;

        try {
            // Check if the file exists and is not empty
            if (storeFile.exists() && storeFile.length() > 0) {
                try (Reader reader = new FileReader(storeFile)) {
                    // Define the type for deserialization using TypeToken to handle generics
                    Type projectListType = new TypeToken<List<Secret>>() {}.getType();
                    secrets = gson.fromJson(reader, projectListType);
                }
            }

            for (Secret secret : secrets){
                if(secret.getAppName().equalsIgnoreCase(appName)){
                    if(!override){
                        System.out.println("secret for App Name " + appName + " already exists" );
                        throw new Exception(appName +" already exists!!!, Send override flag if you want to replace secret");
                    }
                    System.out.println("Found secret for App Name " + appName + " Updating secret...");
                    try {
                        secret.setSecret(encryptText(masterPassword, passwordToStore));
                        found = true;
                        break;
                    }catch (Exception e) {
                        throw new Exception("Update Failed. Failed to encrypt password "+e.getMessage());
                    }
                }
            }

            if(!found){
                System.out.println("Adding Secret to Secret Store");
                try {
                    secrets.add(new Secret(appName, encryptText(masterPassword, passwordToStore)));
                }catch (Exception e) {
                    throw new Exception("Failed to Add, Failed to encrypt password " + e.getMessage());
                }
            }

            try (Writer writer = new FileWriter(storePath)) {
                gson.toJson(secrets, writer);
            }

            return true;
        }catch (Exception e){
            throw new Exception(e.getMessage());
        }

    }

    public boolean storeSecretOperations(String storePath, String appName, String masterPassword,String passwordToStore,boolean override) throws Exception {
        EncryptDecrypt ed = new EncryptDecrypt();
        File secretStore = new File(storePath);
        if(secretStore.exists()){
            storePath = getReadableStore(storePath,masterPassword);
            boolean isStored = storeSecret(storePath, appName, masterPassword,passwordToStore,override);
            if(isStored){
                System.out.println("Secret Stored Successfully");
                ed.manage(storePath, masterPassword, 1);
                System.out.println("Securing Store...");
            }
            if(new File(storePath).delete()){
                System.out.println("Secret Store Secured");
            }
        }else{
            //first time user or store not exists.
            if (secretStore.createNewFile()) {
                System.out.println("Secret Store created: " + secretStore.getName());
                storeSecret(secretStore.getAbsolutePath(), appName, masterPassword, passwordToStore,false);
                ed.manage(storePath, masterPassword, 1);
                File store = new File(secretStore.getAbsolutePath()+".enc");
                if(store.exists()){
                    System.out.println("Secret Store Created at "+store.getPath());
                    if(secretStore.delete()){
                        System.out.println("Secret Store Secured");
                    };
                }
            } else {
                System.out.println("SecretStore already exists.");
            }
        }
        return false;
    }

    public String readSecret(Secret app, String masterPassword) throws Exception {

        if(!app.getAppName().isEmpty()) {
            try {
                System.out.println("Reading secret... ");
                return decryptText(masterPassword, app.getSecret());
            }catch (Exception e){
                throw new Exception(e.getMessage());
            }
        }
        return "Something went wrong!!!";
    }

    public boolean deleteSecret(String storePath,String masterPassword, String appName) throws Exception {
        EncryptDecrypt ed = new EncryptDecrypt();
        File secretStore = new File(storePath);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<Secret> secrets = new ArrayList<>();
        if (secretStore.exists()) {
            storePath = getReadableStore(storePath, masterPassword);
            try {
                // Check if the file exists and is not empty
                try (Reader reader = new FileReader(storePath)) {
                    // Define the type for deserialization using TypeToken to handle generics
                    Type projectListType = new TypeToken<List<Secret>>(){}.getType();
                    secrets = gson.fromJson(reader, projectListType);
                }
                for (Secret secret : secrets) {
                    if (secret.getAppName().equalsIgnoreCase(appName)) {
                        secrets.remove(secret);
                        System.out.println(secret.getAppName() + " removed");
                        break;
                    }
                }

                try (Writer writer = new FileWriter(storePath)) {
                    gson.toJson(secrets, writer);
                }

                System.out.println(appName + " deleted");
                ed.manage(storePath, masterPassword, 1);
                System.out.println("Store Secured");
                return true;
            }catch (Exception e) {
                System.out.println("Failed to delete "+appName);
                throw new Exception(e.getMessage());
            }
        }else{
            System.out.println(appName + " does not exist");
            return false;
        }
    }

    public List<Secret> listSecrets(String readableStore, String masterPassword) throws Exception {
        List<Secret> secrets = null;
        if(!readableStore.isEmpty()){
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (Reader reader = new FileReader(readableStore)) {
                // Define the type for deserialization using TypeToken to handle generics
                Type projectListType = new TypeToken<List<Secret>>() {
                }.getType();
                secrets = gson.fromJson(reader, projectListType);
            }
        }
        return secrets;
    }

    public String getReadableStore(String storePath,String masterPassword) throws Exception {
        File secretStore = new File(storePath);
        String readableStore = null;
        if (secretStore.exists()) {
            EncryptDecrypt ed = new EncryptDecrypt();
            ed.manage(storePath, masterPassword, 0);
            int dotIndex = secretStore.getName().lastIndexOf('.');
            if (dotIndex > 0) { // Ensure the dot is not the first character (e.g., ".bashrc")
                readableStore = secretStore.getParent() + File.separator + secretStore.getName().substring(0, dotIndex);
            } else {
                throw new Exception("Failed to read store");
            }
        }
        return readableStore;
    }
}
