package stockdate.presentation.controller;

import stockdate.presentation.dto.request.CreateBatchRequest;
import stockdate.presentation.dto.response.BatchDTO;
import stockdate.presentation.dto.response.DashboardDTO;
import stockdate.service.ExpirationAlertService;
import stockdate.service.InventoryManager;
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

@RestController
@RequestMapping("/api")
public class InventoryController {

    private final InventoryManager inventoryManager;
    private final ExpirationAlertService expirationAlertService;

    public InventoryController(InventoryManager inventoryManager, ExpirationAlertService expirationAlertService) {
        this.inventoryManager = inventoryManager;
        this.expirationAlertService = expirationAlertService;
    }

    @GetMapping("/inventory")
    public List<BatchDTO> getBatches() {
        return inventoryManager.getBatches();
    }

    @PostMapping("/inventory")
    @ResponseStatus(HttpStatus.CREATED)
    public BatchDTO registerBatch(@RequestBody CreateBatchRequest request) {
        return inventoryManager.registerBatch(request);
    }

    @PutMapping("/inventory/{id}")
    public BatchDTO updateBatch(@PathVariable long id, @RequestBody CreateBatchRequest request) {
        return inventoryManager.updateBatch(id, request);
    }

    @GetMapping("/dashboard")
    public DashboardDTO getDashboard() {
        return expirationAlertService.getDashboard();
    }
}
