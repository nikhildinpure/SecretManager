package com.secrets.vault.operations.files;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.Arrays;

import static com.secrets.vault.operations.common.Utility.validatePassword;
import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EncryptDecryptTest {
    private static Logger logger = LogManager.getLogger(EncryptDecryptTest.class);

    public static String sampleInputPath = "src/test/resources/";
    public static String sampleFile = "sample.txt";
    public static String textToWrite = "This is encryption and decryption test for files";
    @BeforeAll
    public static void setup(){
        logger.info("Set up Started");
        File testFile = new File(sampleInputPath.concat(sampleFile));
        try (FileOutputStream fos = new FileOutputStream(testFile)){
            byte[] text = textToWrite.getBytes();
            fos.write(text);
            logger.info("Text successfully written into test file");
        } catch (IOException e) {
            logger.error("EncryptDecryptTest setup failed");
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    public static void cleanup(){
        logger.info("Clean up Started");
        File file = new File(sampleInputPath);
        File[] list  = file.listFiles();
        assert list != null;
        Arrays.stream(list).toList().forEach(File::delete);
        logger.info("Clean up Completed");
    }

    @Test
    @Order(1)
    public void manageFileNegativePathTest(){
        EncryptDecrypt ed = new EncryptDecrypt();
        try {
            ed.manage(null,"AbcdEfgh!@12345678",2);
        } catch (Exception e) {
            assertEquals(e.getMessage(),"Invalid file or directory");
        }
    }

    @Test
    @Order(2)
    public void manageFileNegativeFileTest(){
        EncryptDecrypt ed = new EncryptDecrypt();
        try {
            ed.manage("src/test/resources/sample.png","AbcdEfgh!@12345678",1);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Invalid file"));
        }
    }

    @Test
    @Order(3)
    public void manageFileNegativeOpsTest(){
        EncryptDecrypt ed = new EncryptDecrypt();
        try {
            ed.manage(sampleInputPath.concat(sampleFile),"AbcdEfgh!@12345678",2);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Operation failed"));
        }
    }

    @Test
    @Order(4)
    public void encryptFileTest(){
        EncryptDecrypt ed = new EncryptDecrypt();
        try {
            ed.manage(sampleInputPath.concat(sampleFile),"AbcdEfgh!@12345678",1);
            File file = new File(sampleInputPath.concat(sampleFile).concat(".enc"));
            assertTrue(file.exists());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @Order(5)
    public void decryptFilePasswordTest() {
        EncryptDecrypt ed = new EncryptDecrypt();
        try {
            ed.manage(sampleInputPath.concat(sampleFile).concat(".enc"), "Strongpassword@1234", 0);
        }catch (Exception e) {
            assertTrue(e.getMessage().contains("Operation failed"));
        }
    }

    @Test
    @Order(6)
    public void decryptFileTest(){
        EncryptDecrypt ed = new EncryptDecrypt();
        try {
            File file = new File(sampleInputPath.concat(sampleFile));
            file.renameTo(new File(sampleInputPath.concat(sampleFile).concat(".orig")));
            ed.manage(sampleInputPath.concat(sampleFile).concat(".enc"),"AbcdEfgh!@12345678",0);
            assertTrue(file.exists());
            File decrypyedFile = new File(sampleInputPath.concat(sampleFile));
            try(FileInputStream fis = new FileInputStream(decrypyedFile);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
            ){
                String line;
                while ((line = br.readLine()) != null) {
                    logger.info(line);
                    assertEquals(textToWrite,line);
                }
            } catch (IOException e) {
                fail();
                throw new RuntimeException(e);
            }

        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @Order(7)
    public void encryptFileBulkTest(){
        EncryptDecrypt ed = new EncryptDecrypt();
        try {
            ed.manage(sampleInputPath,"AbcdEfgh!@12345678",1);
            File file = new File(sampleInputPath);
            File[] list  = file.listFiles();
            assertEquals(4,list.length);
            sleep(1000);
            Arrays.stream(list).toList().forEach((f) -> {if(!f.getName().endsWith(".enc")){ f.delete(); }});
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    @Order(8)
    public void decryptFileBulkTest(){
        EncryptDecrypt ed = new EncryptDecrypt();
        try {
            ed.manage(sampleInputPath,"AbcdEfgh!@12345678",0);
            File file = new File(sampleInputPath);
            String[] list  = file.list();
            assertEquals(4,list.length);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void negativePasswordTest(){
        String shortPassword = "AbcdefghIjk@123";
        String duplicateChars = "Abcdefgh@123AIjk";
        String allSmallChars = "abcdefghi@1234jk";
        String allCapsChars = "ABCDEFGHI^#7891JK";
        String allDigitsChars = "1234567890!#&+=@";
        String noDigitsChars = "ABCDEFGHI^#JKlmnop";
        String noSpecialChars = "ABCDEFGHI12JKlmnop";

        Exception thrown = Assertions.assertThrows(Exception.class,() -> validatePassword(shortPassword),"Case TooShort");
        assertTrue(thrown.getMessage().contains("Minimum 16 characters of password"));

        thrown = Assertions.assertThrows(Exception.class,() -> validatePassword(duplicateChars),"Duplicate characters");
        assertTrue(thrown.getMessage().contains("Duplicate characters"));

        thrown = Assertions.assertThrows(Exception.class,() -> validatePassword(allSmallChars),"All smallcase");
        assertTrue(thrown.getMessage().contains("one uppercase letter"));

        thrown = Assertions.assertThrows(Exception.class,() -> validatePassword(allCapsChars),"All Caps");
        assertTrue(thrown.getMessage().contains("one small case"));

        thrown = Assertions.assertThrows(Exception.class,() -> validatePassword(allDigitsChars),"All digit");
        assertTrue(thrown.getMessage().contains("one uppercase letter"));

        thrown = Assertions.assertThrows(Exception.class,() -> validatePassword(noDigitsChars),"No digit");
        assertTrue(thrown.getMessage().contains("a digit"));

        thrown = Assertions.assertThrows(Exception.class,() -> validatePassword(noSpecialChars),"No special char");
        assertTrue(thrown.getMessage().contains("special character from @#$%^&+=!"));




    }
}