package com.secrets.vault;

import com.google.api.services.drive.model.File;
import com.secrets.vault.operations.bean.Secret;
import com.secrets.vault.operations.files.EncryptDecrypt;
import com.secrets.vault.operations.store.GoogleDrive;
import com.secrets.vault.operations.store.Passwords;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.GeneralSecurityException;
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
                System.out.println("6. Delete Secret");
                System.out.println("7. Download Secret");
                System.out.println("8. Update Secret");
                System.out.println("9. Upload Secret");
                System.out.println("0. Exit");

                int option = 6;
                System.out.print("Enter your choice (0-9): ");
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
                        System.out.println("Provide Following inputs to Delete secret");
                        deleteSecret();
                        break;
                    case 7:
                        System.out.println("Provide Following inputs to Download secret Store from Drive");
                        downloadSecretStore();
                        break;
                    case 8:
                        System.out.println("Provide Following inputs to Upload file secret");
                        uploadSecretStore();
                        break;
                    case 9:
                        System.out.println("Provide Following inputs to Update secret");
                        uploadSecretStore();
                        break;
                    case 0:
                        System.out.println("Thank you, Good Bye!!!");
                        isRunning = false;
                        break;
                    default:
                        System.out.println("Enter your choice (0-9): ");
                        break;
                }
            } catch (Exception e) {
                System.out.println("*************************");
                e.printStackTrace();
                System.out.println("*************************");
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
            System.out.println("*********** :( Encrypt Files Failed **********");
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
            System.out.println("*********** :( Decrypt Files Failed **********");
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
            System.out.println("********** :( Store secret Failed **********");
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
            System.out.println("============================");
            for(Secret secret : secretList){
                System.out.println("App/For Name : "+secret.getAppName());
            }
            System.out.println("============================");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("********** :( List secret Failed **********");
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
            System.out.println("============================");
            for(Secret secret : secretList){
                if(secret.getAppName().equalsIgnoreCase(appName)) {
                    System.out.println("App/For Name");
                    System.out.println(secret.getAppName());
                    passwords.readSecret(secret, masterPassword);
                    break;
                }
            }
            System.out.println("============================");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("********** :( Read secret Failed **********");
        }
    }

    public static void deleteSecret(){
        Passwords passwords = new Passwords();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter file path to store");
        String storePath = scanner.next();
        System.out.println("Enter your master password");
        String masterPassword = scanner.next();
        System.out.println("Enter App/For Name");
        String appName = scanner.next();
        try {
            passwords.deleteSecret(storePath,masterPassword,appName);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("********** :( delete secret Failed **********");
        }

    }

    public static void downloadSecretStore(){
        GoogleDrive drive = new GoogleDrive();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter file name");
        String fileName = scanner.next();
        System.out.println("Enter file path to store");
        String downloadPath = scanner.next();
        try {
            String fileId = "";
            List<File> secureStorageFiles = drive.listSecrets();
            for(File file : secureStorageFiles){
                if(file.getName().equalsIgnoreCase(fileName)){
                    fileId = file.getName();
                    break;
                }
            }
            if(!fileId.isEmpty()){
                drive.downloadFile(fileId,downloadPath);
                System.out.println("=========================");
                System.out.println("Download Success");
                System.out.println("=========================");
            }else{
                throw new Exception("File Id empty");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("********** :( Download File Failed **********");
        }

    }

    public static void uploadSecretStore(){
        GoogleDrive drive = new GoogleDrive();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter full file path to store");
        String filePath = scanner.next();
        System.out.println("Enter file name to modify existing file (leave blank if not exists)");
        String fileName = scanner.next();
        try {
            String fileId = "";
            if(!fileName.isEmpty() && null != fileName){
                List<File> secureStorageFiles = drive.listSecrets();
                for(File file : secureStorageFiles){
                    if(file.getName().equalsIgnoreCase(fileName)){
                        fileId = file.getName();
                        break;
                    }
                }

                if(!fileId.isEmpty()){
                    drive.updateFile(fileId,filePath);
                    System.out.println("=========================");
                    System.out.println("Update Success for file Id : "+fileId);
                    System.out.println("=========================");
                }else{
                    throw new Exception("File Id empty");
                }
            }else {
                fileId = drive.UploadSecrets(filePath);
                System.out.println("=========================");
                System.out.println("Upload Success with file Id : " + fileId);
                System.out.println("=========================");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("********** :( Upload/Update File Failed **********");
        }
    }
}
