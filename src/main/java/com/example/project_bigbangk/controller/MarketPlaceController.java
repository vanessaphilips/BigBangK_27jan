// Created by Deek
// Creation date 12/20/2021

package com.example.project_bigbangk.controller;

import com.example.project_bigbangk.BigBangkApplicatie;
import com.example.project_bigbangk.model.DTO.PriceHistoryDTO;
import com.example.project_bigbangk.service.MarketPlaceService;
import com.example.project_bigbangk.service.Security.AuthenticateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin
@RestController
public class MarketPlaceController {

    private final Logger logger = LoggerFactory.getLogger(MarketPlaceController.class);
    private final AuthenticateService authenticateService;
    private final MarketPlaceService marketPlaceService;
    private final ObjectMapper MAPPER = new ObjectMapper();

    public MarketPlaceController(AuthenticateService authenticateService, MarketPlaceService marketPlaceService) {
        super();
        this.authenticateService = authenticateService;
        this.marketPlaceService = marketPlaceService;
        logger.info("New MarketPlaceController");
    }

    /**
     * Handles request for retrieving priceHsitoryData
     *
     * Returns status.unauthorized jwt is expired or invalid
     * @param authorization the jwt token send in the header
     * @param date in the past for defining period over which the pricehistory is gathered
     * @return Response with status.ok and priceHistoriesDTO object. This is a List of priceHistory,
     * which cantains an Asset and a list of priceDateDTO's. PriceDateDTO contains a price and a date in string format
     * status.unauthorized when bad login
     */
    @PostMapping("/marketplace")
    @ResponseBody
    public ResponseEntity<String> getPriceHistories(@RequestHeader String authorization, @RequestBody String date) {
        if (!authorization.split(" ")[0].equals("Bearer")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Got to login");
        }
        if (authenticateService.authenticate(authorization)) {
            LocalDateTime dateTime = LocalDateTime.parse(date);
            List<PriceHistoryDTO> priceHistoriesDTO;
            priceHistoriesDTO = marketPlaceService.getAllPriceHistoriesFromDate(dateTime);
            try {
                ObjectNode jsonBody = MAPPER.createObjectNode();
                jsonBody.put("priceHistory", MAPPER.writeValueAsString(priceHistoriesDTO))
                        .put("updateInterval", String.valueOf(BigBangkApplicatie.UPDATE_INTERVAL_PRICEUPDATESERVICE));
                return ResponseEntity.status(HttpStatus.OK).body(MAPPER.writeValueAsString(jsonBody));
            } catch (JsonProcessingException e) {
                logger.error(e.getMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("token expired");
    }


}