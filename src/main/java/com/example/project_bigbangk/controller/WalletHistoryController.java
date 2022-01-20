package com.example.project_bigbangk.controller;

import com.example.project_bigbangk.model.Orders.Transaction;
import com.example.project_bigbangk.model.Wallet;
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
        super();
        this.authenticateService = authenticateService;
        this.walletHistoryService = walletHistoryService;
    }

    @PostMapping("/walletHistory")
    @ResponseBody
    public ResponseEntity<String> gotoWalletHistoryScreen(@RequestHeader String authorization){
        if (authenticateService.authenticate(authorization)){
            try {
                Wallet wallet = walletHistoryService.getWalletHistoryClient(authorization);
                System.out.println(wallet);
                String jsonWalletHistory = MAPPER.writeValueAsString(wallet);
                System.out.println("test");
                return ResponseEntity.ok().body(jsonWalletHistory);
            } catch (JsonProcessingException exception) {
                logger.error(exception.getMessage());
            }
        }
        System.out.println("ben er nog");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token expired");
    }
}
