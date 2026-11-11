package com.example.stockdate.presentation.controller;

import com.example.stockdate.presentation.dto.request.CreateBatchRequest;
import com.example.stockdate.presentation.dto.response.BatchDTO;
import com.example.stockdate.service.InventoryManager;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//To do: The getter and setters of the InventoryController.
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryManager inventoryManager;

    public InventoryController(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    @GetMapping
    public List<BatchDTO> getBatches() {
        return inventoryManager.getBatches();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BatchDTO registerBatch(@RequestBody CreateBatchRequest request) {
        return inventoryManager.registerBatch(request);
    }

    @PutMapping("/{id}")
    public BatchDTO updateBatch(@PathVariable long id, @RequestBody CreateBatchRequest request) {
        return inventoryManager.updateBatch(id, request);
    }
}
