package com.supermarket.controller;

import com.supermarket.service.StreamAnalyticsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final StreamAnalyticsService streamAnalyticsService;

    public AnalyticsController(StreamAnalyticsService streamAnalyticsService) {
        this.streamAnalyticsService = streamAnalyticsService;
    }

    @GetMapping("/kpis")
    public Map<String, Object> getDashboardKPIs() {
        return streamAnalyticsService.getDashboardKPIs();
    }

    @GetMapping("/category-revenue")
    public Map<String, Double> getRevenueByCategory() {
        return streamAnalyticsService.getRevenueByCategory();
    }

    @GetMapping("/top-selling")
    public List<Map<String, Object>> getTopSellingProducts(@RequestParam(defaultValue = "5") int limit) {
        return streamAnalyticsService.getTopSellingProducts(limit);
    }

    @GetMapping("/price-statistics")
    public Map<String, Object> getPriceStatistics() {
        return streamAnalyticsService.getProductPriceStatistics();
    }

    @GetMapping("/inventory-partition")
    public Map<String, Object> getInventoryPartition() {
        return streamAnalyticsService.getInventoryPartitioning();
    }

    @GetMapping("/payment-methods")
    public Map<String, Long> getPaymentMethods() {
        return streamAnalyticsService.getPaymentMethodBreakdown();
    }

    @GetMapping("/stream-demo")
    public Map<String, Object> getStreamDemo() {
        return streamAnalyticsService.runFullStreamDemo();
    }
}

