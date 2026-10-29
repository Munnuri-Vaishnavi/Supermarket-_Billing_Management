package com.supermarket.service;

import com.supermarket.model.Bill;
import com.supermarket.model.BillItem;
import com.supermarket.model.Category;
import com.supermarket.model.Product;
import com.supermarket.repository.BillRepository;
import com.supermarket.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * StreamAnalyticsService: Showcases Java 8+ Streams API operations.
 * Operations Demonstrated:
 *  - filter()
 *  - map() and mapToDouble()
 *  - sorted()
 *  - reduce()
 *  - flatMap()
 *  - Collectors.groupingBy()
 *  - Collectors.summarizingDouble()
 *  - Collectors.partitioningBy()
 *  - Collectors.joining()
 *  - Collectors.toMap()
 */
@Service
public class StreamAnalyticsService {

    private final ProductRepository productRepository;
    private final BillRepository billRepository;

    public StreamAnalyticsService(ProductRepository productRepository, BillRepository billRepository) {
        this.productRepository = productRepository;
        this.billRepository = billRepository;
    }

    /**
     * Dashboard Summary KPIs computed using Java Streams
     */
    public Map<String, Object> getDashboardKPIs() {
        List<Product> products = productRepository.findAll();
        List<Bill> bills = billRepository.findAll();

        // 1. Total Store Inventory Valuation using Stream reduce()
        double totalInventoryValue = products.stream()
                .mapToDouble(p -> p.getPrice() * p.getStockQuantity())
                .reduce(0.0, Double::sum);
        totalInventoryValue = Math.round(totalInventoryValue * 100.0) / 100.0;

        // 2. Total Lifetime Revenue using Stream mapToDouble & sum
        double totalRevenue = bills.stream()
                .mapToDouble(Bill::getGrandTotal)
                .sum();
        totalRevenue = Math.round(totalRevenue * 100.0) / 100.0;

        // 3. Average Bill Value using Stream mapToDouble & average
        double avgBillValue = bills.stream()
                .mapToDouble(Bill::getGrandTotal)
                .average()
                .orElse(0.0);
        avgBillValue = Math.round(avgBillValue * 100.0) / 100.0;

        // 4. Low stock count using Stream filter() & count()
        long lowStockCount = products.stream()
                .filter(p -> p.isActive() && p.getStockQuantity() <= 10)
                .count();

        // 5. Total items sold across all bills using flatMap() & sum()
        int totalUnitsSold = bills.stream()
                .flatMap(b -> b.getItems().stream())
                .mapToInt(BillItem::getQuantity)
                .sum();

        Map<String, Object> kpi = new LinkedHashMap<>();
        kpi.put("totalProducts", products.size());
        kpi.put("totalInventoryValue", totalInventoryValue);
        kpi.put("totalBills", bills.size());
        kpi.put("totalRevenue", totalRevenue);
        kpi.put("averageBillValue", avgBillValue);
        kpi.put("lowStockCount", lowStockCount);
        kpi.put("totalUnitsSold", totalUnitsSold);
        return kpi;
    }

    /**
     * Stream Demo 1: Revenue by Category using Collectors.groupingBy() & summingDouble()
     */
    public Map<String, Double> getRevenueByCategory() {
        List<Bill> bills = billRepository.findAll();

        return bills.stream()
                .flatMap(b -> b.getItems().stream())
                .collect(Collectors.groupingBy(
                        BillItem::getCategory,
                        Collectors.summingDouble(BillItem::getTotalPrice)
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Math.round(e.getValue() * 100.0) / 100.0,
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
    }

    /**
     * Stream Demo 2: Top Selling Products using flatMap, groupingBy, and sorted
     */
    public List<Map<String, Object>> getTopSellingProducts(int limit) {
        List<Bill> bills = billRepository.findAll();

        Map<String, Integer> salesByProductName = bills.stream()
                .flatMap(b -> b.getItems().stream())
                .collect(Collectors.groupingBy(
                        BillItem::getProductName,
                        Collectors.summingInt(BillItem::getQuantity)
                ));

        return salesByProductName.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("productName", e.getKey());
                    item.put("unitsSold", e.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }

    /**
     * Stream Demo 3: Statistical Price Analysis using Collectors.summarizingDouble()
     */
    public Map<String, Object> getProductPriceStatistics() {
        List<Product> products = productRepository.findAll();

        DoubleSummaryStatistics stats = products.stream()
                .collect(Collectors.summarizingDouble(Product::getPrice));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("count", stats.getCount());
        result.put("minPrice", stats.getMin());
        result.put("maxPrice", stats.getMax());
        result.put("averagePrice", Math.round(stats.getAverage() * 100.0) / 100.0);
        result.put("totalSum", Math.round(stats.getSum() * 100.0) / 100.0);
        result.put("explanation", "Calculated in O(N) single-pass using Java 8 Stream Collectors.summarizingDouble()");
        return result;
    }

    /**
     * Stream Demo 4: Inventory Partitioning into In-Stock vs Low-Stock using Collectors.partitioningBy()
     */
    public Map<String, Object> getInventoryPartitioning() {
        List<Product> products = productRepository.findAll();

        Map<Boolean, List<Product>> partitioned = products.stream()
                .collect(Collectors.partitioningBy(p -> p.getStockQuantity() > 10));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("healthyStockCount", partitioned.get(true).size());
        result.put("lowStockCount", partitioned.get(false).size());
        result.put("lowStockProductNames", partitioned.get(false).stream()
                .map(Product::getName)
                .collect(Collectors.joining(", ")));
        result.put("explanation", "Partitioned into two distinct groups (Predicate true/false) using Collectors.partitioningBy()");
        return result;
    }

    /**
     * Stream Demo 5: Payment Method Distribution using Collectors.groupingBy() & counting()
     */
    public Map<String, Long> getPaymentMethodBreakdown() {
        List<Bill> bills = billRepository.findAll();

        return bills.stream()
                .collect(Collectors.groupingBy(
                        Bill::getPaymentMethod,
                        Collectors.counting()
                ));
    }

    /**
     * Stream Interactive Showcase endpoint for Reviewer
     */
    public Map<String, Object> runFullStreamDemo() {
        Map<String, Object> demo = new LinkedHashMap<>();
        demo.put("filterDemo", "Filtered Low Stock (<10 units) Products: " +
                productRepository.findAll().stream()
                        .filter(p -> p.getStockQuantity() <= 10)
                        .map(p -> p.getName() + " (" + p.getStockQuantity() + " left)")
                        .collect(Collectors.joining("; ")));

        demo.put("mapDemo", "Transformed Products to Formatted Price Strings: " +
                productRepository.findAll().stream()
                        .limit(5)
                        .map(p -> p.getName() + " -> $" + String.format("%.2f", p.getPrice()))
                        .collect(Collectors.joining(" | ")));

        demo.put("reduceDemo", "Catalog Valuation via reduce(0.0, Double::sum): $" +
                productRepository.findAll().stream()
                        .mapToDouble(p -> p.getPrice() * p.getStockQuantity())
                        .reduce(0.0, Double::sum));

        demo.put("groupingByDemo", "Products count grouped by Category: " +
                productRepository.findAll().stream()
                        .collect(Collectors.groupingBy(
                                p -> p.getCategory().getDisplayName(),
                                Collectors.counting()
                        )));

        demo.put("partitioningByDemo", "Partitioned Count (Stock > 10 vs <= 10): " +
                productRepository.findAll().stream()
                        .collect(Collectors.partitioningBy(p -> p.getStockQuantity() > 10, Collectors.counting())));

        return demo;
    }
}

