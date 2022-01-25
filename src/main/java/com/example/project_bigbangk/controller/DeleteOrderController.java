package com.example.project_bigbangk.controller;

import com.example.project_bigbangk.service.Security.AuthenticateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Karim Ajour on 25-1-2022 for Project_Big_Bangk
 */

@CrossOrigin
@RestController
public class DeleteOrderController {

    private AuthenticateService authenticateService;

    public DeleteOrderController(AuthenticateService authenticateService) {
        this.authenticateService = authenticateService;
    }

    @PostMapping("/deleteorder")
    @ResponseBody
    public ResponseEntity deleteOrder(@RequestHeader String authorization, @RequestBody int orderID) {
        if (authenticateService.authenticate(authorization)) {
            //stuur orderid naar methode in service (ook service in constructor van controller hierboven^)
            //in de service moet je op de een of andere manier bij delete order komen, moet dus een delete order methode in orderDAO
            //en die moet je via de rootrepo aanroepen
            //en als laatste response entity terugsturen met ok of zoiets
        }
        return null;
    }


}
