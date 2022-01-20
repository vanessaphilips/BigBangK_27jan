package com.example.project_bigbangk.controller;

import com.example.project_bigbangk.service.Security.AuthenticateService;
import com.example.project_bigbangk.service.WalletHistoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * @Author Kelly Speelman - de Jonge
 */

@CrossOrigin
@RestController
public class WalletHistoryController {
    private final Logger logger = LoggerFactory.getLogger(WalletController.class);
    private final AuthenticateService authenticateService;
    private final WalletHistoryService walletHistoryService;
    private final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    public WalletHistoryController(AuthenticateService authenticateService, WalletHistoryService walletHistoryService) {
        this.authenticateService = authenticateService;
        this.walletHistoryService = walletHistoryService;
    }

    @GetMapping("/wallet/history")
    @ResponseBody
    public ResponseEntity<String> gotoWalletHistoryScreen(@RequestHeader String authorization){
        if (authenticateService.authenticate(authorization)){
            try {
                List lijst = walletHistoryService.getWalletHistoryClient(authorization);
                String jsonWalletHistory = MAPPER.writeValueAsString(lijst);
                return ResponseEntity.ok().body(jsonWalletHistory);
            } catch (JsonProcessingException exception) {
                logger.error(exception.getMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token expired");
    }
}
