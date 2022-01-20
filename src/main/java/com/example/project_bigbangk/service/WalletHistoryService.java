package com.example.project_bigbangk.service;

import com.example.project_bigbangk.model.Client;
import com.example.project_bigbangk.model.Orders.Transaction;
import com.example.project_bigbangk.model.Wallet;
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

    public Wallet getWalletHistoryClient(String token){
        Client client = authenticateService.getClientFromToken(token);
        rootRepository.fillWalletWithTransactions(client);
        //TODO regel 33 verwijderen als de transaction in de controller kan werken.
        client.getWallet().setTransaction(null);
        return client.getWallet();
    }
}
