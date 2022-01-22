package com.example.project_bigbangk.service.Security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
/**
 * Deze service simuleert een secret key
 * @author Pieter Jan Bleichrodt
 *
 * reation date 12/3/2021
 */
@Service
public class SecretKeyService implements ISecretKeyService {

    private final Logger logger = LoggerFactory.getLogger(SecretKeyService.class);
    private static final String SECRETKEY = "secret";

    public SecretKeyService() {
        super();
        logger.info("New SecretService");
    }

    public String getSecretKey(){
        return SECRETKEY;
    }
}