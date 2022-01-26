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

    @DeleteMapping("/deleteorder/{orderID}")
    @ResponseBody
    public ResponseEntity deleteOrder(@RequestHeader String authorization, @PathVariable int orderID) {
        if (authenticateService.authenticate(authorization)) {
            if (deleteOrderService.deleteOrder(orderID)){
                return ResponseEntity.status(200).body("OK");
            } else {
                return ResponseEntity.status(204).body("No Content");
            }
        }
        return ResponseEntity.status(401).body("token expired");
    }
}
