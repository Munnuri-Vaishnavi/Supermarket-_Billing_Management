package com.supermarket.controller;

import com.supermarket.model.Bill;
import com.supermarket.model.BillRequest;
import com.supermarket.service.BillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
@CrossOrigin(origins = "*")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody BillRequest request) {
        try {
            Bill generatedBill = billingService.generateBill(request);
            return ResponseEntity.ok(generatedBill);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to generate bill: " + e.getMessage()));
        }
    }

    @GetMapping("/bills")
    public List<Bill> getAllBills() {
        return billingService.getAllBills();
    }

    @GetMapping("/bills/{id}")
    public ResponseEntity<Bill> getBillById(@PathVariable Long id) {
        return billingService.getBillById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/bills/number/{billNumber}")
    public ResponseEntity<Bill> getBillByNumber(@PathVariable String billNumber) {
        return billingService.getBillByNumber(billNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/coupons")
    public Map<String, Object> getCoupons() {
        return billingService.getAvailableCoupons();
    }
}

