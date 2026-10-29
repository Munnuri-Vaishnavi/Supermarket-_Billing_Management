/**
 * FreshMart Supermarket Billing Management System
 * Frontend Application Script
 */

// Global State
let allProducts = [];
let currentCategory = 'ALL';
let cart = []; // In-memory List of cart items
let currentDiscountRate = 0.0;
let currentCouponCode = '';
let selectedPaymentMethod = 'CASH';
let selectedRestockProductId = null;

// Chart Instances
let categoryChart = null;
let topProductsChart = null;
let paymentMethodsChart = null;

// Initialize on DOM Ready
document.addEventListener('DOMContentLoaded', () => {
    initClock();
    setupNavigation();
    setupPOS();
    setupInventory();
    setupBilling();
    
    // Load initial data
    loadProducts();
});

// ----------------------------------------------------
// 1. Clock & UI Utilities
// ----------------------------------------------------
function initClock() {
    const clockEl = document.getElementById('liveClock');
    const updateTime = () => {
        const now = new Date();
        clockEl.textContent = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    };
    updateTime();
    setInterval(updateTime, 1000);
}

function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type === 'success' ? 'toast-success' : 'toast-error'}`;
    toast.innerHTML = `<i class="fa-solid ${type === 'success' ? 'fa-circle-check' : 'fa-circle-exclamation'}"></i> <span>${message}</span>`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 3200);
}

function setupNavigation() {
    const navButtons = document.querySelectorAll('.nav-tab-btn');
    const tabPanes = document.querySelectorAll('.tab-pane');

    navButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            const targetId = btn.getAttribute('data-tab');

            navButtons.forEach(b => b.classList.remove('active'));
            tabPanes.forEach(pane => pane.classList.remove('active'));

            btn.classList.add('active');
            const targetPane = document.getElementById(targetId);
            if (targetPane) targetPane.classList.add('active');

            // Trigger specific tab refreshes
            if (targetId === 'tab-inventory') {
                loadInventoryTable();
            } else if (targetId === 'tab-analytics') {
                loadAnalyticsDashboard();
            } else if (targetId === 'tab-history') {
                loadBillHistory();
            }
        });
    });
}

// ----------------------------------------------------
// 2. POS Billing Counter Logic
// ----------------------------------------------------
function setupPOS() {
    // Barcode Search
    const barcodeInput = document.getElementById('barcodeInput');
    barcodeInput.addEventListener('keyup', (e) => {
        if (e.key === 'Enter') {
            const query = barcodeInput.value.trim();
            if (query) handleBarcodeEnter(query);
        } else {
            filterProductsGrid();
        }
    });

    // Simulate Barcode Scanner button
    document.getElementById('btnSimulateScan').addEventListener('click', () => {
        const sampleBarcodes = ['890103001', '890103006', '890103011', '890103015', '890103007', '890103019'];
        const randomCode = sampleBarcodes[Math.floor(Math.random() * sampleBarcodes.length)];
        barcodeInput.value = randomCode;
        handleBarcodeEnter(randomCode);
    });

    // Category Filter Pills
    const pills = document.querySelectorAll('.cat-pill');
    pills.forEach(pill => {
        pill.addEventListener('click', () => {
            pills.forEach(p => p.classList.remove('active'));
            pill.classList.add('active');
            currentCategory = pill.getAttribute('data-category');
            filterProductsGrid();
        });
    });

    // Clear Cart
    document.getElementById('btnClearCart').addEventListener('click', () => {
        if (cart.length === 0) return;
        if (confirm('Are you sure you want to clear current cart?')) {
            clearCart();
            showToast('Cart cleared', 'success');
        }
    });

    // Coupon Apply
    document.getElementById('btnApplyCoupon').addEventListener('click', () => {
        const input = document.getElementById('couponInput');
        applyCoupon(input.value.trim());
    });

    // Payment Method selector
    const payButtons = document.querySelectorAll('.payment-btn');
    payButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            payButtons.forEach(b => b.classList.remove('active'));
            btn.classList.add('active');
            selectedPaymentMethod = btn.getAttribute('data-method');
        });
    });

    // Checkout Submit
    document.getElementById('btnCheckout').addEventListener('click', submitCheckout);

    // Modal close
    document.getElementById('btnCloseReceiptModal').addEventListener('click', () => {
        document.getElementById('receiptModal').classList.remove('active');
    });
}

async function loadProducts() {
    try {
        const res = await fetch('/api/products');
        if (!res.ok) throw new Error('Failed to load products');
        allProducts = await res.json();
        renderProductGrid(allProducts);
    } catch (err) {
        console.error(err);
        showToast('Error connecting to backend server', 'error');
    }
}

function filterProductsGrid() {
    const query = document.getElementById('barcodeInput').value.trim().toLowerCase();
    
    // Filter using category and search term
    const filtered = allProducts.filter(p => {
        const matchesCategory = (currentCategory === 'ALL' || p.category === currentCategory);
        const matchesQuery = !query || 
            p.name.toLowerCase().includes(query) || 
            p.barcode.toLowerCase().includes(query);
        return matchesCategory && matchesQuery;
    });

    renderProductGrid(filtered);
}

function renderProductGrid(products) {
    const grid = document.getElementById('productGrid');
    if (!products || products.length === 0) {
        grid.innerHTML = `
            <div style="grid-column: 1/-1; text-align: center; padding: 3rem 1rem; color: #94a3b8;">
                <i class="fa-solid fa-magnifying-glass" style="font-size: 2rem; margin-bottom: 0.5rem; display: block;"></i>
                <p>No products found matching criteria</p>
            </div>
        `;
        return;
    }

    grid.innerHTML = products.map(p => {
        let stockClass = 'stock-good';
        let stockLabel = `${p.stockQuantity} in stock`;
        if (p.stockQuantity <= 0) {
            stockClass = 'stock-empty';
            stockLabel = 'Out of Stock';
        } else if (p.stockQuantity <= 10) {
            stockClass = 'stock-low';
            stockLabel = `Low: ${p.stockQuantity} left`;
        }

        const icon = p.imageUrl || '📦';

        return `
            <div class="product-card" onclick="addToCart(${p.id})">
                <div class="product-card-icon">${icon}</div>
                <div class="product-card-title" title="${p.name}">${p.name}</div>
                <div class="product-card-meta">
                    <span>${p.barcode}</span>
                    <span class="stock-tag ${stockClass}">${stockLabel}</span>
                </div>
                <div class="product-card-bottom">
                    <div>
                        <span class="product-card-price">$${p.price.toFixed(2)}</span>
                        <span class="product-card-unit">/${p.unit}</span>
                    </div>
                    <button class="btn-add-cart" onclick="event.stopPropagation(); addToCart(${p.id})" title="Add to cart">
                        <i class="fa-solid fa-plus"></i>
                    </button>
                </div>
            </div>
        `;
    }).join('');
}

function handleBarcodeEnter(code) {
    const found = allProducts.find(p => p.barcode === code || p.barcode.endsWith(code));
    if (found) {
        addToCart(found.id);
        document.getElementById('barcodeInput').value = '';
        filterProductsGrid();
        showToast(`Scanned: ${found.name}`, 'success');
    } else {
        filterProductsGrid();
    }
}

function addToCart(productId) {
    const product = allProducts.find(p => p.id === productId);
    if (!product) return;

    if (product.stockQuantity <= 0) {
        showToast(`Cannot add "${product.name}" - Out of stock!`, 'error');
        return;
    }

    const existing = cart.find(item => item.productId === productId);
    if (existing) {
        if (existing.quantity >= product.stockQuantity) {
            showToast(`Max stock reached for ${product.name} (${product.stockQuantity})`, 'error');
            return;
        }
        existing.quantity += 1;
        existing.totalPrice = Math.round((existing.quantity * existing.unitPrice) * 100) / 100;
    } else {
        cart.push({
            productId: product.id,
            barcode: product.barcode,
            name: product.name,
            category: product.category,
            unitPrice: product.price,
            quantity: 1,
            totalPrice: product.price
        });
    }

    renderCart();
}

function updateCartQuantity(productId, delta) {
    const item = cart.find(i => i.productId === productId);
    if (!item) return;

    const product = allProducts.find(p => p.id === productId);
    const newQty = item.quantity + delta;

    if (newQty <= 0) {
        removeFromCart(productId);
        return;
    }

    if (product && newQty > product.stockQuantity) {
        showToast(`Cannot exceed available stock (${product.stockQuantity})`, 'error');
        return;
    }

    item.quantity = newQty;
    item.totalPrice = Math.round((item.quantity * item.unitPrice) * 100) / 100;
    renderCart();
}

function removeFromCart(productId) {
    cart = cart.filter(i => i.productId !== productId);
    renderCart();
}

function clearCart() {
    cart = [];
    currentDiscountRate = 0.0;
    currentCouponCode = '';
    document.getElementById('couponInput').value = '';
    document.getElementById('custNameInput').value = '';
    document.getElementById('custPhoneInput').value = '';
    renderCart();
}

function applyQuickCoupon(code) {
    document.getElementById('couponInput').value = code;
    applyCoupon(code);
}

function applyCoupon(code) {
    if (!code) {
        currentDiscountRate = 0.0;
        currentCouponCode = '';
        renderCart();
        return;
    }

    const coupons = {
        'SAVE10': 0.10,
        'SUPER20': 0.20,
        'FESTIVE15': 0.15,
        'WELCOME5': 0.05
    };

    const upper = code.toUpperCase();
    if (coupons[upper] !== undefined) {
        currentDiscountRate = coupons[upper];
        currentCouponCode = upper;
        showToast(`Promo "${upper}" applied: ${(currentDiscountRate * 100)}% OFF!`, 'success');
    } else {
        showToast('Invalid Coupon Code', 'error');
        currentDiscountRate = 0.0;
        currentCouponCode = '';
    }
    renderCart();
}

function renderCart() {
    const container = document.getElementById('cartItemsList');
    const badge = document.getElementById('cartCountBadge');
    const btnCheckout = document.getElementById('btnCheckout');

    const totalItems = cart.reduce((sum, i) => sum + i.quantity, 0);
    badge.textContent = `${totalItems} ${totalItems === 1 ? 'item' : 'items'}`;

    if (cart.length === 0) {
        container.innerHTML = `
            <div class="cart-empty-state">
                <i class="fa-solid fa-cart-shopping"></i>
                <p>Cart is empty</p>
                <small>Click products or scan barcode to add</small>
            </div>
        `;
        document.getElementById('cartSubtotal').textContent = '$0.00';
        document.getElementById('discountRow').style.display = 'none';
        document.getElementById('cartTax').textContent = '$0.00';
        document.getElementById('cartGrandTotal').textContent = '$0.00';
        btnCheckout.disabled = true;
        return;
    }

    container.innerHTML = cart.map(item => `
        <div class="cart-item-row">
            <div class="cart-item-info">
                <div class="cart-item-name">${item.name}</div>
                <div class="cart-item-rate">$${item.unitPrice.toFixed(2)} each</div>
            </div>
            <div class="cart-item-qty-controls">
                <button class="btn-qty" onclick="updateCartQuantity(${item.productId}, -1)">-</button>
                <span class="cart-item-qty-text">${item.quantity}</span>
                <button class="btn-qty" onclick="updateCartQuantity(${item.productId}, 1)">+</button>
            </div>
            <div class="cart-item-total">$${item.totalPrice.toFixed(2)}</div>
            <button class="btn-remove-item" onclick="removeFromCart(${item.productId})" title="Remove item">
                <i class="fa-solid fa-xmark"></i>
            </button>
        </div>
    `).join('');

    // Totals Calculation (Mirroring Streams Backend)
    const subtotal = cart.reduce((sum, i) => sum + (i.quantity * i.unitPrice), 0);
    const discountAmount = subtotal * currentDiscountRate;
    const taxable = Math.max(0, subtotal - discountAmount);
    const tax = taxable * 0.05;
    const grandTotal = taxable + tax;

    document.getElementById('cartSubtotal').textContent = `$${subtotal.toFixed(2)}`;

    const discountRow = document.getElementById('discountRow');
    if (currentDiscountRate > 0) {
        discountRow.style.display = 'table-row';
        document.getElementById('discountTag').textContent = `${currentCouponCode} - ${(currentDiscountRate * 100)}%`;
        document.getElementById('cartDiscount').textContent = `-$${discountAmount.toFixed(2)}`;
    } else {
        discountRow.style.display = 'none';
    }

    document.getElementById('cartTax').textContent = `$${tax.toFixed(2)}`;
    document.getElementById('cartGrandTotal').textContent = `$${grandTotal.toFixed(2)}`;
    btnCheckout.disabled = false;
}

async function submitCheckout() {
    if (cart.length === 0) return;

    const custName = document.getElementById('custNameInput').value.trim() || 'Walk-in Shopper';
    const custPhone = document.getElementById('custPhoneInput').value.trim();

    const payload = {
        customerName: custName,
        customerPhone: custPhone,
        paymentMethod: selectedPaymentMethod,
        couponCode: currentCouponCode,
        items: cart
    };

    try {
        const btn = document.getElementById('btnCheckout');
        btn.disabled = true;
        btn.innerHTML = `<i class="fa-solid fa-spinner fa-spin"></i> Processing Sale...`;

        const res = await fetch('/api/billing/checkout', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Checkout failed');

        showToast(`Bill #${data.billNumber} Generated Successfully!`, 'success');

        // Display Receipt Modal
        displayReceiptModal(data);

        // Reset cart and reload updated stock
        clearCart();
        loadProducts();

    } catch (err) {
        console.error(err);
        showToast(err.message, 'error');
    } finally {
        const btn = document.getElementById('btnCheckout');
        btn.disabled = cart.length === 0;
        btn.innerHTML = `<i class="fa-solid fa-circle-check"></i> Complete Sale & Print Bill`;
    }
}

function displayReceiptModal(bill) {
    document.getElementById('rBillNo').textContent = bill.billNumber;
    document.getElementById('rDateTime').textContent = new Date(bill.dateTime || Date.now()).toLocaleString();
    document.getElementById('rCustName').textContent = bill.customerName || 'Walk-in Shopper';
    document.getElementById('rCustPhone').textContent = bill.customerPhone || 'N/A';
    document.getElementById('rPayMode').textContent = bill.paymentMethod || 'CASH';

    // Itemized lines
    const itemsContainer = document.getElementById('rItemsList');
    itemsContainer.innerHTML = (bill.items || []).map(item => `
        <div class="r-row" style="margin-bottom: 3px;">
            <span style="flex: 2; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">${item.productName}</span>
            <span style="flex: 1; text-align: center;">${item.quantity} x $${item.unitPrice.toFixed(2)}</span>
            <span style="flex: 1; text-align: right;">$${item.totalPrice.toFixed(2)}</span>
        </div>
    `).join('');

    document.getElementById('rSubtotal').textContent = `$${bill.subtotal.toFixed(2)}`;
    
    const rDiscountRow = document.getElementById('rDiscountRow');
    if (bill.discountAmount && bill.discountAmount > 0) {
        rDiscountRow.style.display = 'flex';
        document.getElementById('rDiscount').textContent = `-$${bill.discountAmount.toFixed(2)} (${bill.couponCode || 'Promo'})`;
    } else {
        rDiscountRow.style.display = 'none';
    }

    const halfTax = (bill.taxAmount || 0) / 2.0;
    document.getElementById('rCGST').textContent = `$${halfTax.toFixed(2)}`;
    document.getElementById('rSGST').textContent = `$${halfTax.toFixed(2)}`;
    document.getElementById('rGrandTotal').textContent = `$${bill.grandTotal.toFixed(2)}`;

    const pts = Math.floor(bill.grandTotal / 50);
    document.getElementById('rLoyaltyPts').textContent = `+${pts} pts earned`;

    document.getElementById('receiptModal').classList.add('active');
}

// ----------------------------------------------------
// 3. Inventory Management Logic
// ----------------------------------------------------
function setupInventory() {
    // Search in inventory
    const searchInput = document.getElementById('inventorySearchInput');
    searchInput.addEventListener('keyup', () => {
        const q = searchInput.value.trim().toLowerCase();
        const filtered = allProducts.filter(p => 
            p.name.toLowerCase().includes(q) || 
            p.barcode.toLowerCase().includes(q) ||
            p.category.toLowerCase().includes(q)
        );
        renderInventoryTableRows(filtered);
    });

    // Add Product Modal
    const addModal = document.getElementById('addProductModal');
    document.getElementById('btnOpenAddProduct').addEventListener('click', () => {
        addModal.classList.add('active');
    });
    document.getElementById('btnCloseAddProductModal').addEventListener('click', () => {
        addModal.classList.remove('active');
    });

    // Form submission
    document.getElementById('addProductForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const newProduct = {
            barcode: document.getElementById('mBarcode').value.trim(),
            name: document.getElementById('mName').value.trim(),
            category: document.getElementById('mCategory').value,
            unit: document.getElementById('mUnit').value.trim() || 'pack',
            price: parseFloat(document.getElementById('mPrice').value),
            costPrice: parseFloat(document.getElementById('mCostPrice').value),
            stockQuantity: parseInt(document.getElementById('mStock').value),
            imageUrl: '📦'
        };

        try {
            const res = await fetch('/api/products', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(newProduct)
            });
            if (!res.ok) throw new Error('Failed to create product');
            showToast('New product added to inventory!', 'success');
            addModal.classList.remove('active');
            document.getElementById('addProductForm').reset();
            await loadProducts();
            loadInventoryTable();
        } catch (err) {
            showToast(err.message, 'error');
        }
    });

    // Restock Modal
    const restockModal = document.getElementById('restockModal');
    document.getElementById('btnCloseRestockModal').addEventListener('click', () => {
        restockModal.classList.remove('active');
    });

    document.getElementById('btnConfirmRestock').addEventListener('click', async () => {
        const qty = parseInt(document.getElementById('restockQtyInput').value);
        if (!qty || qty <= 0) return;

        try {
            const res = await fetch(`/api/products/${selectedRestockProductId}/restock`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ quantity: qty })
            });
            if (!res.ok) throw new Error('Failed to restock');
            showToast('Stock replenished successfully!', 'success');
            restockModal.classList.remove('active');
            await loadProducts();
            loadInventoryTable();
        } catch (err) {
            showToast(err.message, 'error');
        }
    });
}

function loadInventoryTable() {
    renderInventoryTableRows(allProducts);

    // Compute KPIs using Streams concept on frontend
    const totalCount = allProducts.length;
    const lowStockCount = allProducts.filter(p => p.stockQuantity <= 10).length;
    const valuation = allProducts.reduce((sum, p) => sum + (p.price * p.stockQuantity), 0);

    document.getElementById('invTotalProducts').textContent = totalCount;
    document.getElementById('invLowStock').textContent = lowStockCount;
    document.getElementById('invValuation').textContent = `$${valuation.toFixed(2)}`;
}

function renderInventoryTableRows(products) {
    const tbody = document.getElementById('inventoryTableBody');
    if (!products || products.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; padding: 2rem; color: #94a3b8;">No inventory items found</td></tr>`;
        return;
    }

    tbody.innerHTML = products.map(p => {
        let stockBadge = `<span class="stock-tag stock-good">${p.stockQuantity} ${p.unit}</span>`;
        if (p.stockQuantity <= 0) {
            stockBadge = `<span class="stock-tag stock-empty">Out of Stock</span>`;
        } else if (p.stockQuantity <= 10) {
            stockBadge = `<span class="stock-tag stock-low">Low: ${p.stockQuantity} ${p.unit}</span>`;
        }

        return `
            <tr>
                <td><code>${p.barcode}</code></td>
                <td><strong>${p.name}</strong></td>
                <td><span style="font-size: 0.8rem; background: #e2e8f0; padding: 0.2rem 0.5rem; border-radius: 4px;">${p.category}</span></td>
                <td><strong>$${p.price.toFixed(2)}</strong></td>
                <td>$${p.costPrice.toFixed(2)}</td>
                <td>${stockBadge}</td>
                <td>
                    <button class="btn-showcase-run" onclick="openRestockModal(${p.id}, '${p.name.replace(/'/g, "\\'")}', ${p.stockQuantity})" title="Restock">
                        <i class="fa-solid fa-plus"></i> Restock
                    </button>
                    <button class="btn-clear-cart" onclick="deleteProduct(${p.id})" title="Delete product">
                        <i class="fa-solid fa-trash"></i>
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

function openRestockModal(id, name, currentStock) {
    selectedRestockProductId = id;
    document.getElementById('restockItemName').textContent = `${name} (Current Stock: ${currentStock})`;
    document.getElementById('restockQtyInput').value = 20;
    document.getElementById('restockModal').classList.add('active');
}

function setRestockQty(amount) {
    document.getElementById('restockQtyInput').value = amount;
}

async function deleteProduct(id) {
    if (!confirm('Are you sure you want to deactivate this product?')) return;
    try {
        const res = await fetch(`/api/products/${id}`, { method: 'DELETE' });
        if (res.ok) {
            showToast('Product deactivated', 'success');
            await loadProducts();
            loadInventoryTable();
        }
    } catch (err) {
        showToast('Failed to delete product', 'error');
    }
}

// ----------------------------------------------------
// 4. Java Streams & Analytics Dashboard
// ----------------------------------------------------
async function loadAnalyticsDashboard() {
    try {
        // Fetch KPIs
        const kpiRes = await fetch('/api/analytics/kpis');
        const kpi = await kpiRes.json();
        document.getElementById('analyticsTotalRevenue').textContent = `$${kpi.totalRevenue.toFixed(2)}`;
        document.getElementById('analyticsTotalBills').textContent = kpi.totalBills;
        document.getElementById('analyticsAvgBill').textContent = `$${kpi.averageBillValue.toFixed(2)}`;
        document.getElementById('analyticsUnitsSold').textContent = kpi.totalUnitsSold;

        // Fetch Price Summary Stats
        const statsRes = await fetch('/api/analytics/price-statistics');
        const stats = await statsRes.json();
        document.getElementById('statMinPrice').textContent = `$${stats.minPrice.toFixed(2)}`;
        document.getElementById('statMaxPrice').textContent = `$${stats.maxPrice.toFixed(2)}`;
        document.getElementById('statAvgPrice').textContent = `$${stats.averagePrice.toFixed(2)}`;

        // Render Charts
        renderCategoryRevenueChart();
        renderTopProductsChart();
        renderPaymentMethodsChart();

    } catch (err) {
        console.error('Analytics load error:', err);
    }
}

async function renderCategoryRevenueChart() {
    try {
        const res = await fetch('/api/analytics/category-revenue');
        const data = await res.json();
        const labels = Object.keys(data);
        const values = Object.values(data);

        const ctx = document.getElementById('categoryRevenueChart').getContext('2d');
        if (categoryChart) categoryChart.destroy();

        categoryChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: values,
                    backgroundColor: ['#10b981', '#0284c7', '#f59e0b', '#ec4899', '#8b5cf6', '#14b8a6', '#64748b'],
                    borderWidth: 2,
                    borderColor: '#ffffff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'right', labels: { boxWidth: 12, font: { size: 11 } } },
                    tooltip: {
                        callbacks: {
                            label: (ctx) => ` $${ctx.raw.toFixed(2)}`
                        }
                    }
                }
            }
        });
    } catch (err) {
        console.error(err);
    }
}

async function renderTopProductsChart() {
    try {
        const res = await fetch('/api/analytics/top-selling?limit=5');
        const list = await res.json();
        const labels = list.map(item => item.productName);
        const values = list.map(item => item.unitsSold);

        const ctx = document.getElementById('topProductsChart').getContext('2d');
        if (topProductsChart) topProductsChart.destroy();

        topProductsChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Units Sold',
                    data: values,
                    backgroundColor: '#059669',
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                indexAxis: 'y',
                scales: {
                    x: { beginAtZero: true, ticks: { stepSize: 1 } }
                },
                plugins: {
                    legend: { display: false }
                }
            }
        });
    } catch (err) {
        console.error(err);
    }
}

async function renderPaymentMethodsChart() {
    try {
        const res = await fetch('/api/analytics/payment-methods');
        const data = await res.json();
        const labels = Object.keys(data);
        const values = Object.values(data);

        const ctx = document.getElementById('paymentMethodsChart').getContext('2d');
        if (paymentMethodsChart) paymentMethodsChart.destroy();

        paymentMethodsChart = new Chart(ctx, {
            type: 'pie',
            data: {
                labels: labels,
                datasets: [{
                    data: values,
                    backgroundColor: ['#3b82f6', '#10b981', '#8b5cf6']
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'bottom', labels: { boxWidth: 12 } }
                }
            }
        });
    } catch (err) {
        console.error(err);
    }
}

// ----------------------------------------------------
// 5. Bill History Logic
// ----------------------------------------------------
function setupBilling() {
    const search = document.getElementById('billHistorySearch');
    search.addEventListener('keyup', () => {
        loadBillHistory(search.value.trim().toLowerCase());
    });
}

async function loadBillHistory(query = '') {
    try {
        const res = await fetch('/api/billing/bills');
        const bills = await res.json();
        const tbody = document.getElementById('billHistoryTableBody');

        const filtered = !query ? bills : bills.filter(b => 
            b.billNumber.toLowerCase().includes(query) ||
            (b.customerName && b.customerName.toLowerCase().includes(query)) ||
            (b.customerPhone && b.customerPhone.includes(query))
        );

        if (!filtered || filtered.length === 0) {
            tbody.innerHTML = `<tr><td colspan="8" style="text-align: center; padding: 2rem; color: #94a3b8;">No past bills found</td></tr>`;
            return;
        }

        tbody.innerHTML = filtered.map(b => `
            <tr>
                <td><strong>${b.billNumber}</strong></td>
                <td>${new Date(b.dateTime).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })}</td>
                <td>${b.customerName || 'Walk-in'}</td>
                <td>${b.customerPhone || 'N/A'}</td>
                <td>${b.items ? b.items.length : 0} items</td>
                <td><span style="font-size: 0.75rem; background: #e0f2fe; color: #0369a1; padding: 0.15rem 0.45rem; border-radius: 4px; font-weight: 600;">${b.paymentMethod}</span></td>
                <td><strong style="color: var(--primary);">$${b.grandTotal.toFixed(2)}</strong></td>
                <td>
                    <button class="btn-showcase-run" onclick="viewHistoricalBill(${b.id})" title="Print or view bill">
                        <i class="fa-solid fa-receipt"></i> View Receipt
                    </button>
                </td>
            </tr>
        `).join('');

    } catch (err) {
        console.error(err);
    }
}

async function viewHistoricalBill(billId) {
    try {
        const res = await fetch(`/api/billing/bills/${billId}`);
        const bill = await res.json();
        displayReceiptModal(bill);
    } catch (err) {
        showToast('Error opening receipt', 'error');
    }
}

// ----------------------------------------------------
// 6. Reviewer Viva & Showcase Demonstrator
// ----------------------------------------------------
async function runDemo(endpoint, consoleId) {
    const consoleEl = document.getElementById(consoleId);
    consoleEl.textContent = `⚡ Executing: /api/demo/${endpoint} on Spring Boot Server...`;

    try {
        const startTime = performance.now();
        const res = await fetch(`/api/demo/${endpoint}`);
        const data = await res.json();
        const elapsed = (performance.now() - startTime).toFixed(1);

        consoleEl.textContent = `// Execution Time: ${elapsed}ms\n// HTTP 200 OK Response:\n\n` + JSON.stringify(data, null, 2);
    } catch (err) {
        consoleEl.textContent = `❌ Error executing demo: ${err.message}`;
    }
}

