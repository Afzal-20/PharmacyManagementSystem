# Pharmacy Management System
### Wholesale Edition · Desktop · Java 21 + JavaFX + SQLite

A complete, offline pharmacy management desktop application built for wholesale pharmaceutical distributors and retail pharmacies. Covers the full business cycle — purchasing, selling, inventory, credit ledgers, returns, PDF invoicing, expiry management, and automatic backups — with role-based access control and no external dependencies.

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

### Expiry Management
- Dedicated screen showing all batches expiring within the next 90 days plus already-expired stock
- Colour-coded rows: dark red = expired, light red = ≤30 days, orange = 31–60 days, yellow = 61–90 days
- Summary cards showing counts for each urgency level at a glance
- Admin write-off: zeros a batch's stock in one click, permanently logged to the audit trail
- Quick-access Expiry Alerts button on the Dashboard

### Backup & Restore
- **Automatic backup on every close** — a JVM shutdown hook fires whenever the app exits, creating a timestamped `.db` copy in `./backups/`
- **Manual backup** from the Dashboard (admin) or the dedicated Backup & Restore screen at any time
- **All backups kept** — no rotation, no deletion, full history preserved
- **Restore from list** — pick any backup from the screen and restore in one click
- **Restore from file** — browse to any `.db` file on disk (USB, network share, etc.)
- **Pre-restore safety copy** — before any restore, the current database is automatically saved, so a mistaken restore can itself be undone
- Backup filename format: `pharmacy_backup_YYYY-MM-DD_HH-mm-ss.db`

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
- Quick-action buttons: New Sale, Add Stock, Expiry Alerts, Backup & Restore

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
│   │   ├── App.java                    # Entry point + backup shutdown hook
│   │   ├── config/
│   │   │   ├── DatabaseConnection.java # SQLite connection factory
│   │   │   └── DatabaseSetup.java      # Schema init on first run
│   │   ├── controller/
│   │   │   ├── BackupController.java   # Backup & Restore screen
│   │   │   ├── ExpiryController.java   # Expiry Management screen
│   │   │   └── ...                     # All other controllers
│   │   ├── dao/                        # DAO interfaces + implementations (all SQL)
│   │   ├── model/                      # POJOs (Product, Batch, Sale, Customer, etc.)
│   │   └── util/
│   │       ├── BackupService.java      # Backup / restore logic
│   │       ├── CalculationEngine.java  # All financial arithmetic
│   │       ├── InvoiceGenerator.java   # PDF thermal receipt generation
│   │       ├── FuzzySearchUtil.java    # Levenshtein-based product search
│   │       ├── ConfigUtil.java         # config.properties reader/writer
│   │       └── UserSession.java        # Singleton session store
│   └── resources/
│       ├── database/schema.sql         # Full DB schema
│       ├── fxml/
│       │   ├── BackupView.fxml         # Backup & Restore screen
│       │   ├── ExpiryView.fxml         # Expiry Management screen
│       │   └── ...                     # All other FXML views
│       └── styles/                     # CSS stylesheets
```

---

## Backup System

Backups are stored in `./backups/` relative to the application's working directory.

| Event | What happens |
|-------|-------------|
| App closes (any reason) | Automatic timestamped backup via JVM shutdown hook |
| Admin clicks Backup Now | Immediate manual backup |
| Admin restores a backup | Current DB saved as pre-restore copy first, then restore applied |

Backup files are never deleted automatically. To free space, remove old files from `./backups/` manually.

> **Tip:** Periodically copy the `./backups/` folder to an external drive or cloud storage for off-site protection.

---

## Database Schema (Summary)

```
products                — Medicine master (name, generic, manufacturer, pack size, min stock)
batches                 — Stock lots (batch no, expiry, qty, cost, trade price) → products
customers               — Buyers (id=1 is the system walk-in placeholder)
dealers                 — Suppliers (company, contact, drug license)
sales                   — Invoice headers → customers
sale_items              — Invoice lines (qty, price, discount, returned qty) → sales, batches
purchase_history        — Immutable purchase log
payments                — Unified Khata ledger (entity_type = CUSTOMER | DEALER)
sale_returns            — Immutable return log
stock_adjustments_audit — Admin stock edit + write-off audit trail
users                   — Accounts (BCrypt password, role, is_active)
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
| View Expiry Alerts | ✅ | ✅ |
| Stock Purchase Entry | ✅ | ❌ |
| Adjust Stock | ✅ | ❌ |
| Write Off Expired Batch | ✅ | ❌ |
| Edit Product | ✅ | ❌ |
| Process Sales Return | ✅ | ❌ |
| Record Khata Payment | ✅ | ❌ |
| Edit Dealer / Customer | ✅ | ❌ |
| Delete Dealer | ✅ | ❌ |
| Manual Backup | ✅ | ❌ |
| Restore Database | ✅ | ❌ |
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
- **Backups are local.** The system backs up automatically, but files remain on the same machine. Copy `./backups/` to external storage regularly.
- **No reporting module.** Monthly P&L, tax summaries, etc. are not included in this version.
- **Price changes require a new batch.** Editing an existing batch price is intentionally blocked to preserve historical invoice accuracy.
- **App restart required after restore.** The SQLite connection is not reloaded at runtime; a fresh start is needed after any restore operation.

---

## License

This project is released for personal and commercial use. See [LICENSE](LICENSE) for details.
