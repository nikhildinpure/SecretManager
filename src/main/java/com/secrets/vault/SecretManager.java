package com.secrets.vault;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SecretManager {
    private static Logger logger = LogManager.getLogger(SecretManager.class);

    public static void main(String[] args) {
        logger.info("Info log Initialize");
        logger.debug("Debug log Initialize");
        logger.error("Error log Initialize");

    }
}
