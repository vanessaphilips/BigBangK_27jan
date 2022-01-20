package com.example.project_bigbangk.service;

import com.example.project_bigbangk.model.Client;
import com.example.project_bigbangk.service.Security.AuthenticateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author Kelly Speelman - de Jonge
 */

@Service
public class WalletHistoryService {
    private final AuthenticateService authenticateService;

    @Autowired
    public WalletHistoryService (AuthenticateService authenticateService) {
        super();
        this.authenticateService = authenticateService;
    }

    public List getWalletHistoryClient(String token){
        Client client = authenticateService.getClientFromToken(token);

        return null;
    }
}
