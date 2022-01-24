package com.example.project_bigbangk.controller;

import com.example.project_bigbangk.BigBangkApplicatie;
import com.example.project_bigbangk.model.DTO.PriceHistoryDTO;
import com.example.project_bigbangk.service.Security.AuthenticateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller created by Vanessa Philips, 24/01/22
 * Overview for all open orders
 */

@CrossOrigin
@RestController
public class OrderOverviewController {

    private final AuthenticateService authenticateService;
    private final Logger logger = LoggerFactory.getLogger(OrderOverviewController.class);

    public OrderOverviewController(AuthenticateService authenticateService) {
        super();
        this.authenticateService = authenticateService;
        logger.info("New OrderOverviewController");
    }

//    @PostMapping("/orderoverview")
//    @ResponseBody
//    public ResponseEntity<String> getAllPlacedOrders(@RequestHeader String authorization, @RequestBody String date) {
//        if (!authorization.split(" ")[0].equals("Bearer")) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Got to login");
//        }
//
//        // TODO methode afmaken wanneer service gereed is
//        if (authenticateService.authenticate(authorization)) {
//            LocalDateTime dateTime = LocalDateTime.parse(date);
//            List<PriceHistoryDTO> priceHistoriesDTO;
//            priceHistoriesDTO = marketPlaceService.getAllPriceHistoriesFromDate(dateTime);
//            try {
//                ObjectNode jsonBody = MAPPER.createObjectNode();
//                jsonBody.put("priceHistory", MAPPER.writeValueAsString(priceHistoriesDTO))
//                        .put("updateInterval", String.valueOf(BigBangkApplicatie.UPDATE_INTERVAL_PRICEUPDATESERVICE));
//                return ResponseEntity.status(HttpStatus.OK).body(MAPPER.writeValueAsString(jsonBody));
//            } catch (JsonProcessingException e) {
//                logger.error(e.getMessage());
//            }
//        }
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token expired");
//    }
}