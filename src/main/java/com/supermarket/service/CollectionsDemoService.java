package com.supermarket.service;

import com.supermarket.model.CartItem;
import com.supermarket.model.Product;
import com.supermarket.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * CollectionsDemoService: Demonstrates Java Collections Framework in Supermarket operations.
 * Covers:
 *  1. List (ArrayList, LinkedList)
 *  2. Set (HashSet, TreeSet, LinkedHashSet)
 *  3. Map (HashMap, TreeMap, LinkedHashMap)
 *  4. Queue & Deque (PriorityQueue, ArrayDeque)
 */
@Service
public class CollectionsDemoService {

    private final ProductRepository productRepository;

    public CollectionsDemoService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * 1. LIST DEMONSTRATION:
     * - ArrayList: Dynamic array for Cart items & price sorting with custom Comparator
     * - LinkedList: Queue of pending cashier orders (FIFO)
     */
    public Map<String, Object> demonstrateListOperations() {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Product> products = productRepository.findAll();

        // A. ArrayList: Add and Sort items by price using Comparator
        List<Product> arrayListDemo = new ArrayList<>(products);
        arrayListDemo.sort(Comparator.comparingDouble(Product::getPrice));

        List<Map<String, Object>> sortedPreview = new ArrayList<>();
        for (int i = 0; i < Math.min(5, arrayListDemo.size()); i++) {
            Product p = arrayListDemo.get(i);
            Map<String, Object> item = new HashMap<>();
            item.put("rank", i + 1);
            item.put("name", p.getName());
            item.put("price", p.getPrice());
            item.put("category", p.getCategory().name());
            sortedPreview.add(item);
        }

        // B. LinkedList: Order processing simulation
        LinkedList<String> orderQueue = new LinkedList<>();
        orderQueue.addLast("Order #101 (Walk-in)");
        orderQueue.addLast("Order #102 (Online Express)");
        orderQueue.addLast("Order #103 (VIP Member)");
        String processedOrder = orderQueue.removeFirst(); // FIFO poll

        result.put("collection", "List Interface (ArrayList & LinkedList)");
        result.put("description", "ArrayList provides O(1) random access and dynamic resizing. LinkedList provides O(1) node additions/removals for queue pipelines.");
        result.put("arrayListSortedCheapest5", sortedPreview);
        result.put("linkedListInitialQueueSize", 3);
        result.put("linkedListProcessedFirst", processedOrder);
        result.put("linkedListRemainingQueue", orderQueue);
        return result;
    }

    /**
     * 2. SET DEMONSTRATION:
     * - HashSet: Eliminating duplicate customer phone numbers or categories
     * - TreeSet: Automatically sorted unique product price points
     */
    public Map<String, Object> demonstrateSetOperations() {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Product> products = productRepository.findAll();

        // A. HashSet: Unique categories from all products
        Set<String> uniqueCategories = new HashSet<>();
        for (Product p : products) {
            uniqueCategories.add(p.getCategory().getDisplayName());
        }

        // B. TreeSet: Automatically sorted unique prices
        TreeSet<Double> sortedUniquePrices = new TreeSet<>();
        for (Product p : products) {
            sortedUniquePrices.add(p.getPrice());
        }

        result.put("collection", "Set Interface (HashSet & TreeSet)");
        result.put("description", "HashSet guarantees element uniqueness using hashCode() and equals() in O(1). TreeSet sorts elements in natural order using a Red-Black Tree in O(log n).");
        result.put("hashSetUniqueCategoriesCount", uniqueCategories.size());
        result.put("hashSetCategories", uniqueCategories);
        result.put("treeSetLowestPrice", sortedUniquePrices.isEmpty() ? 0.0 : sortedUniquePrices.first());
        result.put("treeSetHighestPrice", sortedUniquePrices.isEmpty() ? 0.0 : sortedUniquePrices.last());
        result.put("treeSetPriceTiersCount", sortedUniquePrices.size());
        return result;
    }

    /**
     * 3. MAP DEMONSTRATION:
     * - HashMap: O(1) Barcode-to-Product Lookup cache
     * - TreeMap: Natural sorted map of category product counts
     */
    public Map<String, Object> demonstrateMapOperations() {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Product> products = productRepository.findAll();

        // A. HashMap: Quick Barcode Lookup Cache
        Map<String, Product> barcodeCache = new HashMap<>();
        for (Product p : products) {
            barcodeCache.put(p.getBarcode(), p);
        }

        // Test lookup with first available barcode
        String sampleBarcode = products.isEmpty() ? "N/A" : products.get(0).getBarcode();
        Product foundProduct = barcodeCache.get(sampleBarcode);

        // B. TreeMap: Sorted inventory count by Category name
        TreeMap<String, Integer> categoryStockMap = new TreeMap<>();
        for (Product p : products) {
            String cat = p.getCategory().getDisplayName();
            categoryStockMap.put(cat, categoryStockMap.getOrDefault(cat, 0) + p.getStockQuantity());
        }

        result.put("collection", "Map Interface (HashMap & TreeMap)");
        result.put("description", "HashMap provides O(1) key-value retrieval via hashing. TreeMap maintains keys in ascending order using a Red-Black Tree.");
        result.put("hashMapCacheTotalEntries", barcodeCache.size());
        result.put("hashMapSampleLookupBarcode", sampleBarcode);
        result.put("hashMapSampleFoundItem", foundProduct != null ? foundProduct.getName() + " ($" + foundProduct.getPrice() + ")" : "Not Found");
        result.put("treeMapCategoryStockSorted", categoryStockMap);
        return result;
    }

    /**
     * 4. QUEUE & PRIORITY QUEUE DEMONSTRATION:
     * - PriorityQueue: Min-Heap prioritizing products with the lowest stock for urgent warehouse restocking
     * - ArrayDeque: Fast LIFO stack for POS Cashier Undo action
     */
    public Map<String, Object> demonstrateQueueOperations() {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Product> products = productRepository.findAll();

        // A. PriorityQueue: Min-Heap based on stockQuantity
        PriorityQueue<Product> restockMinHeap = new PriorityQueue<>(
                Comparator.comparingInt(Product::getStockQuantity)
        );
        restockMinHeap.addAll(products);

        List<Map<String, Object>> topUrgentRestocks = new ArrayList<>();
        int count = Math.min(3, restockMinHeap.size());
        for (int i = 0; i < count; i++) {
            Product p = restockMinHeap.poll(); // Retrieves smallest stock item first
            if (p != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("priority", i + 1);
                item.put("name", p.getName());
                item.put("stockLeft", p.getStockQuantity());
                item.put("barcode", p.getBarcode());
                topUrgentRestocks.add(item);
            }
        }

        // B. ArrayDeque: Cashier Scanner Undo Stack
        ArrayDeque<String> cashierScanStack = new ArrayDeque<>();
        cashierScanStack.push("Scanned Item A: Milk");
        cashierScanStack.push("Scanned Item B: Bread");
        cashierScanStack.push("Scanned Item C: Butter");
        String undoneAction = cashierScanStack.pop(); // Undo last scan

        result.put("collection", "Queue & Deque (PriorityQueue & ArrayDeque)");
        result.put("description", "PriorityQueue maintains elements ordered by a Comparator as a binary heap. ArrayDeque provides resizable array-backed stack/queue operations faster than Stack/LinkedList.");
        result.put("priorityQueueUrgentRestocks", topUrgentRestocks);
        result.put("arrayDequeUndoneAction", undoneAction);
        result.put("arrayDequeCurrentStack", cashierScanStack);
        return result;
    }
}

