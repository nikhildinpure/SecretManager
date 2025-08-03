package com.secrets.vault.operations.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility {
    public static final int SALT_LENGTH = 16;
    public static final int IV_LENGTH = 12; //12 bytes(96 bits)
    public static final int KEY_LENGTH = 256;
    public static final int PBKDF2_ITERATIONS = 100_000; //increase for better security
    public static final int GCM_TAG_LENGTH = 128; //bits
    public static final String secureExtension = ".enc";
    public static final int bufferSize = 8192;
    private static Logger logger = LogManager.getLogger(Utility.class);

    public static boolean validatePassword(String password) throws Exception{
        if(password.length() < 16){
            throw new Exception("Minimum 16 characters of password");
        }
        logger.info("Length validated");

        Set<Character> charSet = new HashSet<>();
        for(char c : password.toCharArray()){
            if(!charSet.add(c)){
                throw new Exception("Duplicate characters are not allowed, char "+c);
            }
        }
        logger.info("No Duplicates in password");

        final String passwordRegex="^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{16,}$";
        Pattern pattern = Pattern.compile(passwordRegex);
        Matcher matcher = pattern.matcher(password);
        if(!matcher.matches()){
            throw new Exception("Password should atleast contain one small case letter, one uppercase letter, a digit, and special character from @#$%^&+=!");
        }
        logger.info("Password validated");
        return matcher.matches();

    }

    public static SecretKey deriveKey(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        /*
        Use PBKDF2 to generate a cyrptographic key from password and salt, with high number of iterations to slow down brute force attacks
        */
        SecretKeyFactory secretFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(),salt,PBKDF2_ITERATIONS,KEY_LENGTH);
        SecretKey tmp = secretFactory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(),"AES");
    }
}
