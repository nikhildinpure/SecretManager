package com.secrets.vault;

import com.secrets.vault.operations.bean.Secret;
import com.secrets.vault.operations.files.EncryptDecrypt;
import com.secrets.vault.operations.store.Passwords;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Scanner;


public class SecretManager {
    private static Logger logger = LogManager.getLogger(SecretManager.class);

    public static void main(String[] args) {
        logger.info("Info log Initialize");
        logger.debug("Debug log Initialize");
        logger.error("Error log Initialize");
        boolean isRunning = true;
        while (isRunning) {
            Scanner scanner = new Scanner(System.in);
            try {
                System.out.println("What do you want to do:");
                System.out.println("1. Encrypt Files");
                System.out.println("2. Decrypt Files");
                System.out.println("3. Store Secret");
                System.out.println("4. List Secrets");
                System.out.println("5. Read Secret");
                System.out.println("6. Exit");

                int option = 6;
                System.out.print("Enter your choice (1-6): ");
                option = scanner.nextInt();
                switch (option) {
                    case 1:
                        System.out.println("Provide Following inputs to Encrypt Files");
                        encryptFiles();
                        break;
                    case 2:
                        System.out.println("Provide Following inputs to Decrypt Files");
                        decryptFiles();
                        break;
                    case 3:
                        System.out.println("Provide Following inputs to Store secret");
                        storeSecret();
                        break;
                    case 4:
                        System.out.println("Provide Following inputs to List secrets");
                        listSecret();
                        break;
                    case 5:
                        System.out.println("Provide Following inputs to Read secret");
                        readSecret();
                        break;
                    case 6:
                        System.out.println("Thank you, Good Bye!!!");
                        isRunning = false;
                        break;
                    default:
                        System.out.println("Enter your choice (1-6): ");
                        break;
                }
            } catch (Exception e) {
                System.out.println("*************************");
                e.printStackTrace();
                System.out.println("*************************");
                System.out.println("Enter your choice (1-6): ");
                scanner.nextInt();
            }
        }

    }

    public static void encryptFiles(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter path or complete file path for encryption");
        String filePath = scanner.next();
        System.out.println("Enter master password to encrypt files");
        String masterPassword = scanner.next();
        try {
            EncryptDecrypt ed = new EncryptDecrypt();
            ed.manage(filePath,masterPassword,1);
            System.out.println("*************************");
            System.out.println("File Encrypted");
            System.out.println("*************************");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Encrypt Files Failed");
        }finally {
            scanner.close();
        }
    }

    public static void decryptFiles(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Make sure, the files you are trying to decrypt are encrypted using this tool only and I hope your remember your password");
        System.out.println("Enter path or complete file path for decryption");
        String filePath = scanner.next();
        System.out.println("Enter your master password");
        String masterPassword = scanner.next();
        try {
            EncryptDecrypt ed = new EncryptDecrypt();
            ed.manage(filePath,masterPassword,0);
            System.out.println("*************************");
            System.out.println("File Decrypted");
            System.out.println("*************************");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Decrypt Files Failed");
        }finally {
            scanner.close();
        }
    }

    public static void storeSecret(){
        Passwords passwords = new Passwords();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter file path to store");
        String storePath = scanner.next();
        System.out.println("Enter your master password");
        String masterPassword = scanner.next();
        System.out.println("Enter App/For Name");
        String appName = scanner.next();
        System.out.println("Enter your App password");
        String appPassword = scanner.next();

        try {
            passwords.storeSecretOperations(storePath,appName,masterPassword,appPassword,false);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("********** Store secret Failed **********");
        }
    }

    public static void listSecret(){
        Passwords passwords = new Passwords();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter file path to store");
        String storePath = scanner.next();
        System.out.println("Enter your master password");
        String masterPassword = scanner.next();
        try {
            String readableStore = passwords.getReadableStore(storePath,masterPassword);
            List<Secret> secretList = passwords.listSecrets(readableStore,masterPassword);
            for(Secret secret : secretList){
                System.out.println("App/For Name");
                System.out.println(secret.getAppName());
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("********** List secret Failed **********");
        }
    }

    public static void readSecret(){
        Passwords passwords = new Passwords();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter file path to store");
        String storePath = scanner.next();
        System.out.println("Enter your master password");
        String masterPassword = scanner.next();
        try {
            String readableStore = passwords.getReadableStore(storePath,masterPassword);
            List<Secret> secretList = passwords.listSecrets(readableStore,masterPassword);
            System.out.println("Enter App/For Name from the list");
            String appName = scanner.next();
            for(Secret secret : secretList){
                if(secret.getAppName().equalsIgnoreCase(appName)) {
                    System.out.println("App/For Name");
                    System.out.println(secret.getAppName());
                    passwords.readSecret(secret, masterPassword);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("********** Read secret Failed **********");
        }
    }
}
