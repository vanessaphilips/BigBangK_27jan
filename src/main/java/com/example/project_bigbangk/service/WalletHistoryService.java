package com.example.project_bigbangk.service;

import com.example.project_bigbangk.model.Client;
import com.example.project_bigbangk.model.Orders.Transaction;
import com.example.project_bigbangk.repository.RootRepository;
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
    private final RootRepository rootRepository;

    @Autowired
    public WalletHistoryService (AuthenticateService authenticateService, RootRepository rootRepository) {
        super();
        this.authenticateService = authenticateService;
        this.rootRepository = rootRepository;
    }

    public List<Transaction> getWalletHistoryClient(String token){
        Client client = authenticateService.getClientFromToken(token);
        rootRepository.fillWalletWithTransactions(client);
        return client.getWallet().getTransaction();
    }
}
