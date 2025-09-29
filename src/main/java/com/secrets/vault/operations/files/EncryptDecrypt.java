package com.secrets.vault.operations.files;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.io.*;
import java.security.SecureRandom;


import static com.secrets.vault.operations.common.Utility.*;

public class EncryptDecrypt {
    private static Logger logger = LogManager.getLogger(EncryptDecrypt.class);

    private static void encryptFile(File inputFile,String password) throws Exception{
        File outputFile = new File(inputFile.getPath().concat(secureExtension));

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
        SecretKey secret = deriveKey(password,salt);

        /*
        Intialize AES-GCM cipher. An authenticated encryption mode.
        It encrypts data and attached a tag allowing the receiver
        to verify data wasn't tampered with and the password is correct
         */
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH,iv);
        cipher.init(Cipher.ENCRYPT_MODE,secret,gcmParameterSpec);

        //Use Input output Streams to support large files.
        try(FileInputStream fis = new FileInputStream(inputFile);
            FileOutputStream fos = new FileOutputStream(outputFile)){
            fos.write(salt);
            fos.write(iv);
            //Encrypts/decrypts in chunks
            try(CipherOutputStream cos = new CipherOutputStream(fos,cipher)){
                byte[] buffer = new byte[bufferSize]; // reads/writes 8KB chunks
                int bytesRead;
                while((bytesRead = fis.read(buffer)) != -1){
                    cos.write(buffer,0,bytesRead);
                }
            }catch (Exception e){
                throw new SecurityException("Encryption failed",e);
            }
        }
    }

    private static void decryptFile(File inputFile, String password) throws Exception{
        File outputFile = new File(inputFile.getPath().split("\\"+secureExtension)[0]);
        try(FileInputStream fis = new FileInputStream(inputFile)){
            byte[] salt = new byte[SALT_LENGTH];
            if(fis.read(salt) != SALT_LENGTH) throw new IOException("Salt Read Error!!!");

            byte[] iv = new byte[IV_LENGTH];
            if(fis.read(iv) != IV_LENGTH) throw new IOException("IV read Error!!!");

            SecretKey secret = deriveKey(password,salt);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH,iv);
            cipher.init(Cipher.DECRYPT_MODE,secret,gcmParameterSpec);

            try(CipherInputStream cis = new CipherInputStream(fis,cipher);
                FileOutputStream fos = new FileOutputStream(outputFile)){
                byte[] buffer = new byte[bufferSize];
                int bytesRead;
                while((bytesRead = cis.read(buffer)) != -1){
                    fos.write(buffer,0,bytesRead);
                }
            }catch (Exception e){
                throw new SecurityException("File authentication failed, Wrong password or corrupt file.",e);
            }
        }
    }

    public void manage(String filePath,String password,int ops) throws Exception {
        if(filePath == null || "".equals(filePath)) {
            logger.error("Invalid file or directory");
            throw new Exception("Invalid file or directory");
        }

        if(ops == 1){
            try {
                validatePassword(password);
            }catch (Exception e){
                logger.error("Password requirement failed " + e.getMessage());
                throw e;
            }
        }

        File input = new File(filePath);
        boolean bulkMode = false;

        if (input.isDirectory()) {
            logger.error("Provided path is a directory");
            bulkMode = true;
        }else {
            logger.info("Provided path is not a directory or does not exist.");
            if(input.isFile()){
                bulkMode = false;
            }else{
                logger.error("invalid file");
                throw new Exception("Invalid file");
            }
        }

        if(bulkMode){
            logger.info("Bulk Mode");
            File[] list = null;
            if(ops == 0){
                list = input.listFiles((dir, name) -> name.toLowerCase().endsWith(secureExtension));
            }else if(ops == 1){
                list = input.listFiles((dir, name) -> !name.toLowerCase().endsWith(secureExtension));
            }else{
                logger.error("Invalid operation");
                throw new Exception("Invalid operation");
            }

            if(list == null || list.length == 0){
                logger.error("No files found for operation");
                return;
            }

            for(File file : list){
                String fileName = file.getName();
                logger.info("Operating on "+fileName);
                try {
                    if(ops == 0){
                        logger.info("Decrypt Mode");
                        decryptFile(file.getAbsoluteFile(), password);
                        logger.info(fileName + " is decrypted successfully");
                    } else {
                        logger.info("Encrypt Mode");
                        encryptFile(file.getAbsoluteFile(),password);
                        logger.info(fileName + " is encrypted successfully");
                    }
                } catch (Exception e) {
                    logger.error("Operation failed for "+fileName, e.getMessage());
                }
            }

        }else {
            try {
                if(ops == 0){
                    logger.info("Decrypt Mode");
                    decryptFile(input, password);
                    logger.info(input.getName() + " is decrypted successfully");
                } else if (ops == 1){
                    logger.info("Encrypt Mode");
                    encryptFile(input,password);
                    logger.info(input.getName() + " is encrypted successfully");
                }else {
                    logger.error("Invalid operation");
                    throw new Exception("Invalid operation");
                }
            } catch (Exception e) {
                logger.error("Operation failed", e.getMessage());
                throw new Exception("Operation failed",e);
            }
        }

    }

}
