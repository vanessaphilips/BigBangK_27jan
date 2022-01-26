package com.example.project_bigbangk.controller;

import com.example.project_bigbangk.BigBangkApplicatie;
import com.example.project_bigbangk.model.Client;
import com.example.project_bigbangk.model.DTO.OrderDTO;
import com.example.project_bigbangk.model.DTO.WalletOwner;
import com.example.project_bigbangk.model.Orders.Limit_Buy;
import com.example.project_bigbangk.model.Orders.TransactionType;
import com.example.project_bigbangk.service.OrderOverViewService;
import com.example.project_bigbangk.service.Security.AuthenticateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller created by Vanessa Philips, 24/01/22
 * Overview for all open orders
 */

@CrossOrigin
@RestController
public class OrderOverviewController {

    private final AuthenticateService authenticateService;
    private final OrderOverViewService orderOverViewService;
    private final Logger logger = LoggerFactory.getLogger(OrderOverviewController.class);
    private static ObjectMapper MAPPER;

    public OrderOverviewController(AuthenticateService authenticateService, OrderOverViewService orderOverViewService) {
        super();
        this.authenticateService = authenticateService;
        this.orderOverViewService = orderOverViewService;
        logger.info("New OrderOverviewController");
    }

    @GetMapping("/orderoverview")
    @ResponseBody
    public ResponseEntity<String> getAllOrders(@RequestHeader String authorization, @RequestBody String date) {
        if (!authorization.split(" ")[0].equals("Bearer")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Got to login");
        }
        // TODO methode afmaken wanneer service gereed is
        if (authenticateService.authenticate(authorization)) {
            Client client = authenticateService.getClientFromToken(authorization);
            List<OrderDTO> orderDTOList = new ArrayList<>();
            convertOrderToOrderDTO(orderOverViewService.getAllLimitBuy(), client);
            orderOverViewService.getAllLimitSell();
            orderOverViewService.getAllStopLossSells();
//            try {
//                ObjectNode jsonBody = MAPPER.createObjectNode();
//                jsonBody.put("priceHistory", MAPPER.writeValueAsString(priceHistoriesDTO))
//                        .put("updateInterval", String.valueOf(BigBangkApplicatie.UPDATE_INTERVAL_PRICEUPDATESERVICE));
//                return ResponseEntity.status(HttpStatus.OK).body(MAPPER.writeValueAsString(jsonBody));
//            } catch (JsonProcessingException e) {
//                logger.error(e.getMessage());
//            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token expired");
    }

    private List<OrderDTO> convertOrderToOrderDTO(List<Limit_Buy> orders, Client client) {
        List<OrderDTO> orderDTOList = new ArrayList<>();
        for (Limit_Buy limit_buy : orders) {
            WalletOwner walletOwner = WalletOwner.OTHERCLIENT;
            if (limit_buy.getBuyer().equals(client.getWallet())) {
                walletOwner = WalletOwner.CURRENTCLIENT;
            }
            OrderDTO orderDTO = new OrderDTO(limit_buy.getAsset().getCode(),
                    TransactionType.LIMIT_BUY.toString(),
                    limit_buy.getOrderLimit(),
                    limit_buy.getAssetAmount(),
                    limit_buy.getOrderId(),
                    walletOwner,
                    limit_buy.getDate());
            orderDTOList.add(orderDTO);
        }
        return orderDTOList;
    }
}