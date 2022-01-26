package com.example.project_bigbangk.service;

import com.example.project_bigbangk.repository.RootRepository;
import org.springframework.stereotype.Service;

/**
 * Created by Karim Ajour on 25-1-2022 for Project_Big_Bangk
 */
@Service
public class DeleteOrderService {

    private final RootRepository rootRepository;

    public DeleteOrderService(RootRepository rootRepository) {
        this.rootRepository = rootRepository;
    }

    public boolean deleteOrder (int orderId) {
        return rootRepository.deleteOrderByID(orderId);
    }
}
