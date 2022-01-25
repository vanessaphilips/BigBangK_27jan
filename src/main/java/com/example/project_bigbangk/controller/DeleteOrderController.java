package com.example.project_bigbangk.controller;

import com.example.project_bigbangk.service.DeleteOrderService;
import com.example.project_bigbangk.service.Security.AuthenticateService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Karim Ajour on 25-1-2022 for Project_Big_Bangk
 */

@CrossOrigin
@RestController
public class DeleteOrderController {

    private AuthenticateService authenticateService;
    private DeleteOrderService deleteOrderService;

    @Autowired
    public DeleteOrderController(AuthenticateService authenticateService, DeleteOrderService deleteOrderService) {
        this.authenticateService = authenticateService;
        this.deleteOrderService = deleteOrderService;
    }

    @PostMapping("/deleteorder")
    @ResponseBody
    public ResponseEntity deleteOrder(@RequestHeader String authorization, @RequestBody int orderID) {
        if (authenticateService.authenticate(authorization)) {
            if (deleteOrderService.deleteOrder(orderID)){
                return ResponseEntity.status(200).body("OK");
            } else {
                return ResponseEntity.status(204).body("No Content");
            }
        }
        return ResponseEntity.status(401).body("token expired");
    }
    //stuur orderid naar methode in service (ook service in constructor van controller hierboven^)
    //in de service moet je op de een of andere manier bij delete order komen, moet dus een delete order methode in orderDAO
    //en die moet je via de rootrepo aanroepen
    //en als laatste response entity terugsturen met ok of zoiets

//    A successful response of DELETE requests SHOULD be an HTTP response code 200 (OK) if the response includes an entity describing the status.
//    The status should be 204 (No Content) if the action has been performed but the response does not include an entity.

}
