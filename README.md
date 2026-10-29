# 🛒 FreshMart Supermarket Billing & Inventory Management System

A production-ready **Java Full Stack (JFS)** project engineered specifically to demonstrate **Java Collections Framework**, **Java 8+ Streams API**, and **Relational Database Persistence (H2 / Spring Data JPA)** with a modern, high-speed Point of Sale (POS) user interface.

---

## 🚀 Quick Start (How to Run for Your Review)

### Method 1: One-Click Run (Recommended)
Simply double-click the **`run.bat`** file in the project root folder.
- It will verify Java 21/17.
- It launches the Spring Boot backend server.
- It automatically opens your browser to **`http://localhost:8080`**!

### Method 2: Command Line
```powershell
mvn spring-boot:run
```
or run the pre-built standalone executable JAR:
```powershell
java -jar target/supermarket-billing-system-1.0.0.jar
```
Then navigate to: **`http://localhost:8080`**

---

## 🎯 Architecture & Project Structure

```
c:\Users\Vaishu\Downloads\JFS_Project\
├── pom.xml                                   # Maven configuration (Java 21, Spring Boot 3.3.4, H2)
├── run.bat                                   # 1-click execution script with auto browser launch
├── README.md                                 # Complete documentation & Viva examination guide
├── data/                                     # Embedded H2 Database persistent storage
└── src/main/
    ├── java/com/supermarket/
    │   ├── SupermarketBillingApplication.java # Spring Boot Main Entrypoint
    │   ├── config/
    │   │   └── DataInitializer.java          # Seeds 25+ realistic products, customers & bills
    │   ├── model/                            # Domain Entities & DTOs
    │   │   ├── Product.java                  # Product JPA Entity (Barcode, Price, Stock, etc.)
    │   │   ├── Category.java                 # Enum (Grocery, Dairy, Snacks, Beverages, etc.)
    │   │   ├── Customer.java                 # Customer Entity (Loyalty points & Tiers)
    │   │   ├── Bill.java                     # Invoice Entity (Subtotal, Tax, Discounts, Grand Total)
    │   │   ├── BillItem.java                 # Individual line items in a bill
    │   │   ├── CartItem.java                 # In-memory POS Cart model
    │   │   └── BillRequest.java              # Checkout Request DTO
    │   ├── repository/                       # Spring Data JPA Repositories
    │   │   ├── ProductRepository.java
    │   │   ├── BillRepository.java
    │   │   └── CustomerRepository.java
    │   ├── service/                          # Core Business Logic & Showcase
    │   │   ├── ProductService.java           # Inventory stock tracking & CRUD
    │   │   ├── BillingService.java           # Tax, Coupon codes, Cart validation, Invoicing
    │   │   ├── CollectionsDemoService.java   # Dedicated Java Collections framework demonstrations
    │   │   └── StreamAnalyticsService.java   # Dedicated Java 8+ Streams API operations
    │   └── controller/                       # REST Controllers
    │       ├── ProductController.java
    │       ├── BillingController.java
    │       ├── AnalyticsController.java
    │       └── CollectionsStreamsDemoController.java # Reviewer viva endpoints
    └── resources/
        ├── application.properties            # H2 DB config & server port 8080
        └── static/                           # Modern Frontend UI
            ├── index.html                    # Single-Page POS & ERP Dashboard
            ├── css/style.css                 # Clean retail styling & thermal receipt print CSS
            └── js/app.js                     # Dynamic POS cart, Chart.js integrations & live demos
```

---

## 💡 How the 3 Core Criteria are Implemented

### 1. Java Collections Framework (In-Depth)
All implementations are located in **`CollectionsDemoService.java`** and interactive live on the **Reviewer Showcase** tab:

| Collection Class | Interface | Where & Why It Is Used in Supermarket POS | Time Complexity |
|---|---|---|---|
| **`ArrayList`** | `List` | Used for the customer POS shopping cart items and product catalog filtering. Provides random access by index and dynamic resizing. | $O(1)$ read, amortized $O(1)$ insertion |
| **`LinkedList`** | `List` / `Queue` | Used for FIFO cashier order processing queues (`addLast`, `removeFirst`). Allows $O(1)$ constant time node removals without shifting array elements. | $O(1)$ queue poll / push |
| **`HashSet`** | `Set` | Used for instant deduplication of customer phone numbers and unique product categories in orders using `hashCode()` and `equals()`. | $O(1)$ lookup / insertion |
| **`TreeSet`** | `NavigableSet` | Used for maintaining automatically sorted unique product price tiers in natural order using a Red-Black Tree. | $O(\log N)$ |
| **`HashMap`** | `Map` | Used for in-memory **Barcode-to-Product Lookup Cache**. Cashier barcode scanners perform instantaneous lookups without hitting the DB every keystroke. | $O(1)$ average lookup |
| **`TreeMap`** | `NavigableMap` | Used for sorted inventory category counts sorted alphabetically by category name. | $O(\log N)$ |
| **`PriorityQueue`** | `Queue` | Configured as a **Min-Heap** (`Comparator.comparingInt(Product::getStockQuantity)`) to automatically surface the top urgent low-stock items for warehouse replenishment. | $O(\log N)$ insert / poll |
| **`ArrayDeque`** | `Deque` | Used as a high-performance Cashier Undo / Redo scanner stack. | $O(1)$ push / pop |

---

### 2. Java 8+ Streams API (In-Depth)
All implementations are located in **`StreamAnalyticsService.java`** and visualized via live Chart.js graphs on the **Streams Analytics** tab:

1. **`filter()`**:
   - Isolates active products with low stock:
     ```java
     products.stream()
             .filter(p -> p.isActive() && p.getStockQuantity() <= 10)
             .count();
     ```
2. **`map()` & `mapToDouble()`**:
   - Computes total subtotal and grand lifetime revenue:
     ```java
     double totalRevenue = bills.stream()
                                .mapToDouble(Bill::getGrandTotal)
                                .sum();
     ```
3. **`sorted()` & `limit()`**:
   - Ranks the Top 5 Best-Selling Products by unit volume sold:
     ```java
     salesMap.entrySet().stream()
             .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
             .limit(5)
             .collect(Collectors.toList());
     ```
4. **`reduce()`**:
   - Calculates total retail warehouse valuation:
     ```java
     double totalValuation = products.stream()
             .mapToDouble(p -> p.getPrice() * p.getStockQuantity())
             .reduce(0.0, Double::sum);
     ```
5. **`Collectors.groupingBy()`**:
   - Groups sales revenue by Category (powers the Doughnut Chart):
     ```java
     bills.stream()
          .flatMap(b -> b.getItems().stream())
          .collect(Collectors.groupingBy(
                  BillItem::getCategory,
                  Collectors.summingDouble(BillItem::getTotalPrice)
          ));
     ```
6. **`Collectors.summarizingDouble()`**:
   - Generates comprehensive single-pass price statistics (Min, Max, Average, Sum, Count):
     ```java
     DoubleSummaryStatistics stats = products.stream()
             .collect(Collectors.summarizingDouble(Product::getPrice));
     ```
7. **`Collectors.partitioningBy()`**:
   - Divides product catalog into two distinct groups based on boolean predicate (Stock > 10 vs Stock $\le$ 10).
8. **`flatMap()`**:
   - Flattens nested item lists across all historical bills into a unified stream for product-level sales analysis.

---

### 3. Database & Persistence Layer
- **Engine**: Embedded **H2 Relational Database**.
- **Persistence Mode**: Persisted to local disk at `./data/supermarketdb.mv.db` (data is preserved even when restarted).
- **Zero Configuration**: Reviewers do **NOT** need MySQL or PostgreSQL installed; it runs seamlessly out of the box!
- **Tables Created**:
  1. `PRODUCTS`: SKU barcode, name, category, price, cost price, stock quantity, unit.
  2. `BILLS`: Bill invoice number, date/time, customer details, payment method, tax, discount, grand total.
  3. `BILL_ITEMS`: Foreign key relation to Bill, product SKU, quantity, unit price, total price.
  4. `CUSTOMERS`: Phone number, name, loyalty points, membership tier (Bronze, Silver, Gold, Platinum).
- **H2 Database Console**:
  - Open: **`http://localhost:8080/h2-console`**
  - JDBC URL: `jdbc:h2:file:./data/supermarketdb`
  - User: `sa` | Password: `password`
  - You can run raw SQL queries (`SELECT * FROM PRODUCTS;`) directly in front of the reviewer!

---

## 🖥️ User Interface Highlights

1. **POS Billing Counter**:
   - Instant search and barcode scanning simulation.
   - Category filtering pills (Grocery, Dairy, Beverages, Snacks, Produce, etc.).
   - Interactive cart drawer with quantity increment/decrement.
   - Promo discount codes:
     * `SAVE10` = 10% Instant Discount
     * `SUPER20` = 20% Super Saver
     * `FESTIVE15` = 15% Festive Special
   - Authentic **Thermal Cash Receipt Modal** with itemized breakdown, tax computation, loyalty points, and 1-click **Browser Print / Save PDF**.
2. **Product & Inventory Management**:
   - Real-time stock status badges (Healthy in green, Low in yellow, Out of stock in red).
   - Add new product modal and 1-click restock dialog.
3. **Streams Analytics Dashboard**:
   - Real-time KPI summary (Total Sales, Bills Issued, Average Order Value).
   - 3 Interactive Chart.js visual charts (Revenue by Category, Top 5 Products, Payment Modes).
   - Price statistics summary.
4. **Reviewer Showcase Viva Lab**:
   - Interactive live testing console with buttons to run and display List, Set, Map, Queue, and Streams operations on demand.

---

## 🎓 Viva / Review Q&A Cheat-Sheet

**Q1: Why did you use `ArrayList` for Cart and `LinkedList` for Orders?**
> *Answer*: `ArrayList` provides $O(1)$ instant random access by index which is ideal for cart item updates and price sorting. `LinkedList` implements the `Queue` interface and allows $O(1)$ constant time addition and removal from head/tail (`addLast`/`removeFirst`), making it ideal for FIFO cashier order pipelines.

**Q2: How does `HashMap` improve performance in your Billing System?**
> *Answer*: When a barcode scanner reads an item, `HashMap` performs an $O(1)$ lookup using `hashCode()` to find the product in memory, rather than executing expensive database `SELECT` queries repeatedly during high-speed checkout.

**Q3: What is the benefit of Java Streams over traditional for-loops?**
> *Answer*: Streams provide a declarative, functional approach that eliminates boilerplate accumulator variables, minimizes mutation bugs, and allows seamless chaining of operations like `.filter()`, `.map()`, and `Collectors.groupingBy()`. They can also be converted to parallel streams (`.parallelStream()`) with zero code rewrite.

**Q4: How is stock consistency maintained during concurrent billing?**
> *Answer*: In `BillingService.java`, the checkout method is annotated with `@Transactional`. Before saving the bill, it checks if `product.getStockQuantity() >= cartItem.getQuantity()`. If stock is insufficient, it throws an exception and rolls back the database transaction automatically.

**Q5: Where is the database stored?**
> *Answer*: We use an embedded H2 database configured with `jdbc:h2:file:./data/supermarketdb`. It creates a local persistent database file, so data is saved across restarts without requiring an external server installation.

