package com.example.project_bigbangk.controller;
/*
@Author Philip Beeltje, Studentnummer: 500519452
*/

import com.example.project_bigbangk.model.Client;
import com.example.project_bigbangk.model.DTO.OrderDTO;
import com.example.project_bigbangk.repository.RootRepository;
import com.example.project_bigbangk.service.Orderservice;
import com.example.project_bigbangk.service.Security.AuthenticateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
public class OrderController{

    private Orderservice orderservice;
    private AuthenticateService authenticateService;
    private RootRepository rootRepository;

    public OrderController(Orderservice orderservice, AuthenticateService authenticateService, RootRepository rootRepository){
        super();
        this.orderservice = orderservice;
        this.authenticateService = authenticateService;
        this.rootRepository = rootRepository;
    }

    /**
     * Handles request to place an order from the frontend.
     * First checks the user's login token then sends the orderDTO to be handles in the orderservice.
     * Based on the orderservice will send a response entity back to the frontend.
     * @param authorization Bearer Token in header
     * @param orderDTO orderDTO filled based on received JSON
     * @return ResponseEntity for frontend
     */
    @PostMapping("/placeorder")
    public ResponseEntity<?> placeOrder(@RequestHeader String authorization, @RequestBody OrderDTO orderDTO) {
        if (authenticateService.authenticate(authorization)) {
            Client client = authenticateService.getClientFromToken(authorization);
            String orderMessage = orderservice.handleOrderByType(orderDTO, client);
                return ResponseEntity.status(201).body(orderMessage);
            }
        return ResponseEntity.status(401).body("token expired");
    }

    @PostMapping("/getcurrentprice")
    public ResponseEntity<?> getLatestPrice(@RequestHeader String authorization, @RequestBody String assetCode) {
        if (authenticateService.authenticate(authorization)) {
            double price = rootRepository.getCurrentPriceByAssetCode(assetCode);
            return ResponseEntity.status(201).body(price);
        }
        return ResponseEntity.status(401).body("token expired");
    }



}
