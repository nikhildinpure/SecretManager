package com.secrets.vault;

import com.secrets.vault.operations.store.Passwords;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SecretManager {
    private static Logger logger = LogManager.getLogger(SecretManager.class);

    public static void main(String[] args) {
        logger.info("Info log Initialize");
        logger.debug("Debug log Initialize");
        logger.error("Error log Initialize");


        int option = 1;
        switch (option){
            case(1):
                System.out.println("Store Passwords");

            case(2):
                System.out.println("get Password");
        }

    }
}
