-- 1. Products Table
CREATE TABLE IF NOT EXISTS products (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    generic_name TEXT,
    manufacturer TEXT,
    description TEXT,
    pack_size INTEGER DEFAULT 1,
    min_stock_level INTEGER DEFAULT 10,
    shelf_location TEXT
);

-- 2. Batches Table
CREATE TABLE IF NOT EXISTS batches (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id INTEGER,
    batch_no TEXT,
    expiry_date TEXT,
    qty_on_hand INTEGER,
    cost_price REAL,
    trade_price REAL,
    company_discount REAL DEFAULT 0.0,
    sales_tax REAL DEFAULT 0.0,
    discount_percent REAL,
    is_active INTEGER DEFAULT 1,
    FOREIGN KEY(product_id) REFERENCES products(id)
);

-- 3. Dealers Table
CREATE TABLE IF NOT EXISTS dealers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    company_name TEXT,
    phone TEXT,
    address TEXT,
    license_no TEXT
);

-- 4. Customers Table
-- type DEFAULT 'REGULAR' — all code paths insert REGULAR, so the default matches.
-- current_balance removed — balance is calculated live from getDynamicCustomerBalance().
CREATE TABLE IF NOT EXISTS customers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    phone TEXT,
    address TEXT,
    type TEXT DEFAULT 'REGULAR',
    cnic TEXT
);

-- 5. Sales Table
CREATE TABLE IF NOT EXISTS sales (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount REAL,
    amount_paid REAL DEFAULT 0.0,
    balance_due REAL DEFAULT 0.0,
    payment_mode TEXT,
    customer_id INTEGER,
    user_id INTEGER,
    FOREIGN KEY(customer_id) REFERENCES customers(id)
);

-- 6. Sale Items Table
CREATE TABLE IF NOT EXISTS sale_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sale_id INTEGER,
    product_id INTEGER,
    batch_id INTEGER,
    quantity INTEGER,
    unit_price REAL,
    sub_total REAL,
    bonus_qty INTEGER DEFAULT 0,
    discount_percent REAL DEFAULT 0.0,
    returned_qty INTEGER DEFAULT 0,
    FOREIGN KEY(sale_id) REFERENCES sales(id),
    FOREIGN KEY(batch_id) REFERENCES batches(id)
);

-- 7. Purchase History
CREATE TABLE IF NOT EXISTS purchase_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    dealer_id INTEGER,
    product_id INTEGER,
    product_name TEXT,
    batch_no TEXT,
    dealer_invoice_no TEXT,
    initial_boxes_purchased INTEGER,
    cost_price REAL,
    trade_price REAL,
    purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. Payments Ledger (Khata)
CREATE TABLE IF NOT EXISTS payments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    entity_id INTEGER,
    entity_type TEXT,
    amount REAL,
    payment_mode TEXT,
    description TEXT,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 9. Stock Adjustments Audit
CREATE TABLE IF NOT EXISTS stock_adjustments_audit (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    batch_id INTEGER,
    old_qty INTEGER,
    new_qty INTEGER,
    reason TEXT,
    adjustment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id INTEGER NOT NULL,
    FOREIGN KEY(batch_id) REFERENCES batches(id)
);

-- 10. Sale Returns
CREATE TABLE IF NOT EXISTS sale_returns (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    sale_id INTEGER,
    sale_item_id INTEGER,
    batch_id INTEGER,
    returned_qty INTEGER,
    refund_amount REAL,
    return_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    refund_method TEXT,
    reason TEXT
);

-- 11. Users Table (Security)
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    role TEXT DEFAULT 'SALESMAN',
    full_name TEXT,
    is_active INTEGER DEFAULT 1
);

INSERT OR IGNORE INTO customers (id, name, phone, address, type, cnic)
VALUES (1, 'Counter Sale (Walk-in)', '', '', 'REGULAR', '');

CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_batches_number ON batches(batch_no);
CREATE INDEX IF NOT EXISTS idx_sales_date ON sales(sale_date);