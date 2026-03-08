# Pharmacy Management System
### Wholesale Edition · Desktop · Java 21 + JavaFX + SQLite

A complete, offline pharmacy management desktop application built for wholesale pharmaceutical distributors and retail pharmacies. Covers the full business cycle — purchasing, selling, inventory, credit ledgers, returns, and PDF invoicing — with role-based access control and no external dependencies.

---

## Screenshots

> *Login → Dashboard → POS → Sales History*
>
> *(Add screenshots to `/docs/screenshots/` and update paths below)*

| Login | Dashboard | Point of Sale |
|-------|-----------|---------------|
| ![login](docs/screenshots/login.png) | ![dashboard](docs/screenshots/dashboard.png) | ![pos](docs/screenshots/pos.png) |

---

## Features

### Point of Sale
- Fuzzy product search (Levenshtein distance) — tolerates typos on both brand name and generic name
- Per-item discount percentage and bonus quantity
- Walk-in counter sales (cash, full payment enforced) and named credit customer sales (Khata)
- Live balance-due / change-due calculation as amount is typed
- Thermal PDF invoice (80mm roll) auto-generated and saved to Desktop on every checkout

### Stock & Inventory
- Batch-level inventory — each purchase creates a distinct lot with its own expiry date and price
- Duplicate batch detection: re-purchasing the same batch number merges stock rather than duplicating
- Trade price auto-calculated from cost price + margin percentage
- Admin stock adjustment with mandatory reason — every change written to a permanent audit log
- Edit product master data (name, generic name, manufacturer, pack size, min stock level)

### Sales History & Returns
- Date-filtered invoice browser with item-level drill-down
- Process item-level returns: cash refund or Khata credit, quantity validated against original sale
- Refund maths correctly backs out the original per-item discount
- Return receipt PDF auto-generated in a `Returns/` folder
- Reprint any historical invoice at any time

### Khata Ledger System
- Separate ledgers for customers (receivables) and dealers (payables)
- Dynamic balance calculated live from the payments ledger — no stale stored balance
- Record cash payments against customers or dealers directly from the ledger view
- Full debit/credit history with dates and descriptions

### Dealer & Customer Management
- Register and edit dealers (company name, contact, drug license number)
- Dealer deletion blocked if payment history exists — preserves ledger integrity
- Register and edit customers (name, phone, CNIC)
- Walk-in customer placeholder (id = 1) is system-protected and cannot be edited or deleted

### Dashboard
- Monthly sales total
- Low stock alert count (against per-product minimum stock levels)
- Expiry alert count (batches expiring within 6 months)
- Daily cash closing summary: total billed, net cash in drawer, Khata billed, refunds issued

### Security
- BCrypt password hashing (jBCrypt) — no plaintext, no MD5, no SHA
- Two roles: **ADMIN** and **SALESMAN**
- RBAC enforced at both sidebar level and controller level
- All SQL uses `PreparedStatement` — no string-concatenated queries anywhere
- SQLite foreign keys and WAL journal mode enabled on every connection

### Item Ledger
- Select any product to see its full purchase history and sales history side by side

### User Management *(Admin only)*
- Create ADMIN or SALESMAN accounts
- Enable / disable accounts (primary admin account is permanent)

---

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 21 |
| UI Framework | JavaFX 21 + FXML |
| Database | SQLite 3.42 (embedded, zero-install) |
| Build Tool | Apache Maven 3 |
| PDF Generation | iTextPDF 5.5.13 |
| Password Hashing | jBCrypt 0.4 |
| Architecture | MVC — Controller / DAO Interface / Model |

---

## Prerequisites

- **Java 21** JDK (with JavaFX modules, e.g. [Liberica JDK Full](https://bell-sw.com/pages/downloads/) or [Azul Zulu FX](https://www.azul.com/downloads/?package=jdk-fx))
- **Apache Maven 3.8+**

No database server. No internet. No installation wizard.

---

## Getting Started

```bash
# 1. Clone the repository
git clone https://github.com/your-username/PharmacyManagementSystem.git
cd PharmacyManagementSystem

# 2. Build
mvn clean package

# 3. Run
mvn javafx:run
```

On first launch the application:
1. Creates the SQLite database (`wholesale_pharmacy.db`) in the working directory
2. Runs `schema.sql` to build all tables and indexes
3. Seeds two default accounts:

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | ADMIN |
| `salesman` | `sales123` | SALESMAN |

> **Change both passwords immediately after first login.**

---

## Configuration

Create a `config.properties` file in the same directory you run the application from. All keys are optional — defaults are used if the file is absent.

```properties
# Printed on every invoice
pharmacy.name=AL-SHIFA PHARMACY
pharmacy.address=123 Main Bazaar, Mardan
pharmacy.phone=0937-123456

# SQLite database filename (relative to working directory)
db.name=wholesale_pharmacy.db

# Open generated PDFs in default viewer automatically
pdf.auto_open=true
```

---

## Project Structure

```
src/
├── main/
│   ├── java/com/my/pharmacy/
│   │   ├── App.java                    # Entry point
│   │   ├── config/
│   │   │   ├── DatabaseConnection.java # SQLite connection factory
│   │   │   └── DatabaseSetup.java      # Schema init on first run
│   │   ├── controller/                 # JavaFX controllers (UI logic)
│   │   ├── dao/                        # DAO interfaces + implementations (all SQL)
│   │   ├── model/                      # POJOs (Product, Batch, Sale, Customer, etc.)
│   │   └── util/
│   │       ├── CalculationEngine.java  # All financial arithmetic
│   │       ├── InvoiceGenerator.java   # PDF thermal receipt generation
│   │       ├── FuzzySearchUtil.java    # Levenshtein-based product search
│   │       ├── ConfigUtil.java         # config.properties reader
│   │       └── UserSession.java        # Singleton session store
│   └── resources/
│       ├── database/schema.sql         # Full DB schema
│       ├── fxml/                       # All FXML view files
│       └── styles/                     # CSS stylesheets
```

---

## Database Schema (Summary)

```
products          — Medicine master (name, generic, manufacturer, pack size, min stock)
batches           — Stock lots (batch no, expiry, qty, cost, trade price) → products
customers         — Buyers (id=1 is the system walk-in placeholder)
dealers           — Suppliers (company, contact, drug license)
sales             — Invoice headers → customers
sale_items        — Invoice lines (qty, price, discount, returned qty) → sales, batches
purchase_history  — Immutable purchase log
payments          — Unified Khata ledger (entity_type = CUSTOMER | DEALER)
sale_returns      — Immutable return log
stock_adjustments_audit — Admin stock edit audit trail
users             — Accounts (BCrypt password, role, is_active)
```

---

## Role Permissions

| Feature | ADMIN | SALESMAN |
|---------|:-----:|:--------:|
| Point of Sale | ✅ | ✅ |
| View Inventory | ✅ | ✅ |
| View Sales History | ✅ | ✅ |
| Reprint Invoice | ✅ | ✅ |
| View Khata Ledgers | ✅ | ✅ |
| Stock Purchase Entry | ✅ | ❌ |
| Adjust Stock | ✅ | ❌ |
| Edit Product | ✅ | ❌ |
| Process Sales Return | ✅ | ❌ |
| Record Khata Payment | ✅ | ❌ |
| Edit Dealer / Customer | ✅ | ❌ |
| Delete Dealer | ✅ | ❌ |
| User Management | ✅ | ❌ |

---

## Invoice Output

Invoices are generated as PDF files targeting **80mm thermal roll paper** using iTextPDF. They are saved to the user's Desktop automatically:

- **New sale:** `~/Desktop/Invoice_N.pdf`
- **Reprint:** `~/Desktop/REPRINT_Invoice_N.pdf`
- **Return receipt:** `Returns/Return_Inv_N_timestamp.pdf` (in the application directory)

The invoice header (shop name, address, phone) is read from `config.properties` at print time.

---

## Default Credentials

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | ADMIN |
| `salesman` | `sales123` | SALESMAN |

These are created on first launch only (when the users table is empty). Passwords are BCrypt-hashed before storage — the plaintext values above are only needed for first login.

---

## Known Limitations

- **Single-user only.** Concurrent access to the same `.db` file from multiple machines is not supported.
- **No network features.** No cloud sync, no remote access, no multi-branch support.
- **No automated backup.** Manually copy `wholesale_pharmacy.db` to a safe location regularly.
- **No reporting module.** Monthly P&L, tax summaries, etc. are not included in this version.
- **Price changes require a new batch.** Editing an existing batch price is intentionally blocked to preserve historical invoice accuracy.

---

## License

This project is released for personal and commercial use. See [LICENSE](LICENSE) for details.
