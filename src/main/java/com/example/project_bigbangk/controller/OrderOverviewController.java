package com.example.project_bigbangk.controller;

import com.example.project_bigbangk.BigBangkApplicatie;
import com.example.project_bigbangk.model.Bank;
import com.example.project_bigbangk.model.Client;
import com.example.project_bigbangk.model.DTO.OrderDTO;
import com.example.project_bigbangk.model.DTO.TransActionDTO;
import com.example.project_bigbangk.model.DTO.WalletOwner;
import com.example.project_bigbangk.model.Orders.*;
import com.example.project_bigbangk.service.OrderOverViewService;
import com.example.project_bigbangk.service.Security.AuthenticateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.example.project_bigbangk.BigBangkApplicatie.bigBangk;

/**
 * Controller created by Vanessa Philips & Pieter Jan Bleichrodt, 24/01/22
 * Overview for all orders
 */

@CrossOrigin
@RestController
public class OrderOverviewController {

    private final AuthenticateService authenticateService;
    private final OrderOverViewService orderOverViewService;
    private final Logger logger = LoggerFactory.getLogger(OrderOverviewController.class);
    private static ObjectMapper MAPPER;
    private Bank bigBangK;

    public OrderOverviewController(AuthenticateService authenticateService, OrderOverViewService orderOverViewService, BigBangkApplicatie bigBangkApplicatie) {
        super();
        this.authenticateService = authenticateService;
        this.orderOverViewService = orderOverViewService;
        this.bigBangK = bigBangkApplicatie.getBank();
        logger.info("New OrderOverviewController");
    }

    @GetMapping("/orderoverview")
    @ResponseBody
    public ResponseEntity<String> getAllOrders(@RequestHeader String authorization, @RequestBody String date) {
        if (!authorization.split(" ")[0].equals("Bearer")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Got to login");
        }
        if (authenticateService.authenticate(authorization)) {
            Client client = authenticateService.getClientFromToken(authorization);
            List<OrderDTO> orderDTOList = new ArrayList<>();
            orderDTOList.addAll(convertLimitBuysToOrderDTOs(orderOverViewService.getAllLimitBuy(), client));
            orderDTOList.addAll(convertLimitSellsToOrderDTOs(orderOverViewService.getAllLimitSell(), client));
            orderDTOList.addAll(convertStoplossSellsToOrderDTOs(orderOverViewService.getAllStopLossSells(), client));
            try {
                ObjectNode jsonBody = MAPPER.createObjectNode();
                MAPPER.registerModule(new JSR310Module());
                MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
                jsonBody.put("orders", MAPPER.writeValueAsString(orderDTOList))
                        .put("updateInterval", String.valueOf(BigBangkApplicatie.UPDATE_INTERVAL_PRICEUPDATESERVICE));
                return ResponseEntity.status(HttpStatus.OK).body(MAPPER.writeValueAsString(jsonBody));
            } catch (JsonProcessingException e) {
                logger.error(e.getMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token expired");
    }

    @GetMapping("/clienttransactions")
    @ResponseBody
    public ResponseEntity<String> getClientTransactions(@RequestHeader String authorization, @RequestBody String date) {
        if (!authorization.split(" ")[0].equals("Bearer")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Got to login");
        }
        if (authenticateService.authenticate(authorization)) {
            Client client = authenticateService.getClientFromToken(authorization);
            List<TransActionDTO> transactionDTOList = new ArrayList<>();
            transactionDTOList.addAll(convertTransactionDTO(client));
            try {
                ObjectNode jsonBody = MAPPER.createObjectNode();
                MAPPER.registerModule(new JSR310Module());
                MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
                jsonBody.put("transactions", MAPPER.writeValueAsString(transactionDTOList))
                        .put("updateInterval", String.valueOf(BigBangkApplicatie.UPDATE_INTERVAL_PRICEUPDATESERVICE));
                return ResponseEntity.status(HttpStatus.OK).body(MAPPER.writeValueAsString(jsonBody));
            } catch (JsonProcessingException e) {
                logger.error(e.getMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token expired");
    }

    private List<TransActionDTO> convertTransactionDTO(Client client) {
        List<TransActionDTO> transActionDTOS = new ArrayList<>();
        List<Transaction> transactions = client.getWallet().getTransaction();
        for (Transaction transaction : transactions) {
            WalletOwner walletSeller = WalletOwner.OTHERCLIENT;
            WalletOwner walletBuyer = WalletOwner.OTHERCLIENT;
            if (transaction.getSellerWallet().equals(client.getWallet())) {
                walletSeller = WalletOwner.CURRENTCLIENT;
            } else if (transaction.getSellerWallet().equals(bigBangk.getWallet())) {
                walletSeller = WalletOwner.BANK;
            }
            if (transaction.getBuyerWallet().equals(client.getWallet())) {
                walletBuyer = WalletOwner.CURRENTCLIENT;
            } else if (transaction.getBuyerWallet().equals(bigBangk.getWallet())) {
                walletBuyer = WalletOwner.BANK;
            }
            TransActionDTO transActionDTO = new TransActionDTO(transaction.getAsset(),
                    transaction.getPriceExcludingFee(), transaction.getAssetAmount(),
                    transaction.getDate(), transaction.getFee(), walletSeller, walletBuyer);
            transActionDTOS.add(transActionDTO);
        }return transActionDTOS;
    }

    private List<OrderDTO> convertLimitBuysToOrderDTOs(List<Limit_Buy> orders, Client client) {
        List<OrderDTO> orderDTOList = new ArrayList<>();
        for (Limit_Buy limit_buy : orders) {
            WalletOwner walletOwner = WalletOwner.OTHERCLIENT;
            if (limit_buy.getBuyer().equals(client.getWallet())) {
                walletOwner = WalletOwner.CURRENTCLIENT;
            }
            orderDTOList.add(createOrderDTO(limit_buy, TransactionType.LIMIT_BUY, walletOwner));
        }
        return orderDTOList;
    }

    private List<OrderDTO> convertLimitSellsToOrderDTOs(List<Limit_Sell> orders, Client client) {
        List<OrderDTO> orderDTOList = new ArrayList<>();
        for (Limit_Sell limit_sell : orders) {
            WalletOwner walletOwner = WalletOwner.OTHERCLIENT;
            if (limit_sell.getSeller().equals(client.getWallet())) {
                walletOwner = WalletOwner.CURRENTCLIENT;
            }
            orderDTOList.add(createOrderDTO(limit_sell, TransactionType.LIMIT_SELL, walletOwner));
        }
        return orderDTOList;
    }

    private List<OrderDTO> convertStoplossSellsToOrderDTOs(List<Stoploss_Sell> orders, Client client) {
        List<OrderDTO> orderDTOList = new ArrayList<>();
        for (Stoploss_Sell stoploss_sell : orders) {
            WalletOwner walletOwner = WalletOwner.OTHERCLIENT;
            if (stoploss_sell.getSeller().equals(client.getWallet())) {
                walletOwner = WalletOwner.CURRENTCLIENT;
            }
            orderDTOList.add(createOrderDTO(stoploss_sell, TransactionType.STOPLOSS_SELL, walletOwner));
        }
        return orderDTOList;
    }

    private OrderDTO createOrderDTO(AbstractOrder abstractorder, TransactionType transactionType, WalletOwner walletOwner) {
        return new OrderDTO(abstractorder.getAsset().getCode(),
                transactionType.toString(),
                abstractorder.getOrderLimit(),
                abstractorder.getAssetAmount(),
                abstractorder.getOrderId(),
                walletOwner,
                abstractorder.getDate());
    }
}