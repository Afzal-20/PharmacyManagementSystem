# PharmDesk
### Wholesale Edition · Desktop · Java 25 + JavaFX + SQLite

A complete, offline pharmacy management desktop application built for wholesale pharmaceutical distributors. Covers the full business cycle — purchasing, selling, inventory, credit ledgers, returns, thermal invoice printing, PDF archiving, expiry management, and automatic backups — with role-based access control and no external server dependencies.

---

## Screenshots

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
- Thermal receipt printed on 80mm roll via ESC/POS 
- PDF invoice auto-archived to `C:\ProgramData\PharmDesk\Invoices\` on every checkout

### Stock & Inventory
- Batch-level inventory — each purchase creates a distinct lot with its own expiry date and price
- Duplicate batch detection: re-purchasing the same batch number merges stock rather than duplicating
- Trade price auto-calculated from cost price + margin percentage
- Admin stock adjustment with audit trail — every manual change permanently logged
- Edit product master data (name, generic, manufacturer, pack size, min stock level)

### Expiry Management
- Dedicated screen showing all batches expiring within 90 days plus already-expired stock
- Colour-coded rows: dark red = expired · light red = ≤30 days · orange = 31–60 days · yellow = 61–90 days
- Summary cards showing counts per urgency level at a glance
- Admin write-off: zeros a batch's stock in one click, permanently logged to the audit trail
- Hard block on selling expired stock at the POS cannot be overridden

### Backup & Restore
- **Automatic backup on every close ,** JVM shutdown hook fires whenever the app exits
- **Manual backup** from the Dashboard or the Backup & Restore screen at any time
- **All backups kept ,**  no rotation, no auto-deletion, full history preserved
- **Restore from list ,**  pick any backup from the screen and restore in one click
- **Restore from file ,**  browse to any `.db` file on disk (USB, network share, etc.)
- **Pre-restore safety copy ,** before any restore the current database is saved automatically
- Backup filename format: `pharmacy_backup_YYYY-MM-DD_HH-mm-ss.db`
- All backups stored in `C:\ProgramData\PharmDesk\backups\`

### Sales History & Returns
- Date-filtered invoice browser with item-level drill-down
- Process item-level returns: cash refund or Khata credit, quantity validated against original sale
- Refund correctly backs out the original per-item discount
- Return receipt PDF archived to `C:\ProgramData\PharmDesk\Returns\`
- Reprint any historical invoice at any time

### Khata Ledger System
- Separate ledgers for customers (receivables) and dealers (payables)
- Dynamic balance calculated live from the payments ledger, no stale stored balance
- Record cash payments against customers or dealers directly from the ledger view
- Full debit/credit history with dates and descriptions

### Dealer & Customer Management
- Register and edit dealers (company name, contact, drug license number)
- Dealer deletion blocked if payment history exists, preserves ledger integrity
- Register and edit customers (name, phone, CNIC)
- Walk-in customer placeholder (id = 1) is system-protected

### Dashboard
- Monthly sales total
- Low stock alert count (against per-product minimum stock levels)
- Expiry alert count (batches expiring within 6 months)
- Daily cash closing summary: total billed, net cash, Khata billed, refunds issued
- Quick-action buttons: New Sale, Add Stock, Expiry Alerts, Backup & Restore

### Security
- BCrypt password hashing (jBCrypt) , no plaintext, no MD5, no SHA
- Two roles: **ADMIN** and **SALESMAN**
- RBAC enforced at both sidebar and controller level
- Brute force protection: 5 failed login attempts triggers a 5-second lockout with live countdown
- All SQL uses `PreparedStatement` , no string-concatenated queries anywhere
- SQLite foreign keys and WAL journal mode enabled on every connection

### Item Ledger
- Select any product to see its full purchase and sales history side by side

### User Management *(Admin only)*
- Create ADMIN or SALESMAN accounts
- Enable / disable accounts (primary admin is permanent and cannot be deactivated)

---

## Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Java 25 |
| UI Framework | JavaFX 25 + FXML |
| Database | SQLite 3.42 (embedded, zero-install) |
| Build Tool | Apache Maven 3 (maven-shade-plugin fat jar) |
| PDF Generation | iTextPDF 5.5.13 |
| Password Hashing | jBCrypt 0.4 |
| Logging | SLF4J SimpleLogger → `C:\ProgramData\PharmDesk\pharmdesk.log` |
| Thermal Printing | ESC/POS over USB (80mm roll) |
| Architecture | MVC — Controller / DAO Interface / Model |

---

## Prerequisites

- **Java 25 JDK** — [Adoptium Temurin 25](https://adoptium.net) recommended
- **JavaFX SDK 25+** — download separately from [openjfx.io](https://openjfx.io)
- **Apache Maven 3.8+** (included via `mvnw` wrapper — no separate install needed)

No database server. No internet connection required. No installation wizard for development.

---

## Development Setup

```bash
# 1. Clone
git clone https://github.com/Afzal-20/PharmacyManagementSystem.git
cd PharmacyManagementSystem

# 2. Run (development)
.\mvnw.cmd javafx:run          # Windows
./mvnw javafx:run              # Linux / macOS

# 3. Build fat jar
.\mvnw.cmd package
# Output: target/PharmDesk.jar
```

> **Windows note:** Make sure `JAVA_HOME` points to JDK 25 before running mvnw.

On first launch the application:
1. Creates `C:\ProgramData\PharmDesk\` and all required subdirectories
2. Copies default `config.properties` if none exists
3. Creates the SQLite database and runs `schema.sql`
4. Seeds two default accounts:

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | ADMIN |
| `salesman` | `sales123` | SALESMAN |

> **Change both passwords immediately after first login.**

---

## Configuration

The live config file is located at `C:\ProgramData\PharmDesk\config.properties`. Edit it with any text editor — no restart required for invoice header changes.

```properties
# Printed on every invoice header
pharmacy.name=
pharmacy.address=
pharmacy.phone=

# SQLite database filename (inside C:\ProgramData\PharmDesk\)
db.name=wholesale_pharmacy.db

# USB thermal printer name (as shown in Windows Devices & Printers)
printer.name=
```

All keys are written automatically with defaults on first run. `ConfigUtil` is the single source of truth — add new keys there only.

---

## Data Directory

All runtime data lives in `C:\ProgramData\PharmDesk\` — writable by all Windows users without UAC elevation.

```
C:\ProgramData\PharmDesk\
    wholesale_pharmacy.db       ← live database
    config.properties           ← editable configuration
    pharmdesk.log               ← application log (SLF4J)
    Invoices\                   ← PDF archive of every sale invoice
    Returns\                    ← PDF archive of every return receipt
    backups\                    ← automatic + manual database backups
```

---

## Project Structure

```
src/main/java/com/my/pharmacy/
├── App.java                        # Entry point + backup shutdown hook
├── config/
│   ├── DatabaseConnection.java     # SQLite connection factory (WAL + FK)
│   └── DatabaseSetup.java          # Schema init on first run
├── controller/                     # 21 JavaFX controllers (MVC)
│   ├── LoginController.java        # Auth + brute force lockout
│   ├── MainController.java         # Root layout + toast container
│   ├── POSController.java          # Point of Sale
│   ├── SalesHistoryController.java # Invoice browser + returns
│   ├── InventoryController.java    # Batch inventory view
│   ├── PurchaseController.java     # Stock purchase entry
│   ├── ExpiryController.java       # Expiry management + write-off
│   ├── BackupController.java       # Backup & restore
│   ├── KhataController.java        # Customer + dealer ledgers
│   ├── DashboardController.java    # Home dashboard
│   └── ...                         # Customer, Dealer, User, etc.
├── dao/                            # DAO interfaces + SQL implementations
├── model/                          # POJOs (Product, Batch, Sale, Customer …)
├── service/
│   └── CartService.java            # Cart logic extracted from POSController
└── util/
    ├── AppPaths.java               # Single source of truth for all file paths
    ├── BackupService.java          # Backup / restore logic
    ├── CalculationEngine.java      # All financial arithmetic
    ├── ConfigUtil.java             # config.properties reader/writer + defaults
    ├── DialogUtil.java             # Styled confirmation dialogs
    ├── FuzzySearchUtil.java        # Levenshtein product search
    ├── InvoiceGenerator.java       # PDF invoice generation (iTextPDF)
    ├── NotificationService.java    # Non-blocking toast notifications
    ├── ThermalPrinter.java         # ESC/POS thermal printer engine
    ├── UserSession.java            # Singleton session store
    └── Validator.java              # Chainable input validation utility

src/main/resources/
├── database/schema.sql             # Full DB schema + indexes
├── fxml/                           # 20 FXML view files
├── styles/style.css                # Global JavaFX stylesheet
├── images/                         # Logo assets
└── simplelogger.properties         # SLF4J log config
```

---

## Backup System

| Event | What happens |
|-------|-------------|
| App closes (any reason) | Automatic timestamped backup via JVM shutdown hook |
| Admin clicks Backup Now | Immediate manual backup |
| Admin restores a backup | Current DB saved as pre-restore copy first, then restore applied |

Backup files are never deleted automatically. To free space, remove old files from `backups\` manually.

> **Tip:** Periodically copy the `backups\` folder to an external drive for off-site protection.

---

## Database Schema

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
stock_adjustments_audit — Manual stock edit + write-off audit trail
users                   — Accounts (BCrypt hash, role, is_active)
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

## Invoice & Receipt Output

| Document | Location |
|----------|----------|
| Sale invoice (thermal) | Printed on 80mm roll via ESC/POS |
| Sale invoice (PDF) | `C:\ProgramData\PharmDesk\Invoices\Invoice_N.pdf` |
| Reprint (PDF) | `C:\ProgramData\PharmDesk\Invoices\REPRINT_Invoice_N.pdf` |
| Return receipt (PDF) | `C:\ProgramData\PharmDesk\Returns\Return_Inv_N_timestamp.pdf` |

The invoice header (shop name, address, phone) is read from `config.properties` at print time.

---

## Logging

Application events are written to `C:\ProgramData\PharmDesk\pharmdesk.log` via SLF4J SimpleLogger.

```
2026-03-11 06:30:00 [INFO] App - PharmDesk starting up
2026-03-11 06:30:01 [INFO] LoginController - Login successful: user=admin role=ADMIN
2026-03-11 06:30:05 [INFO] POSController - Sale saved with id=1
2026-03-11 06:30:05 [INFO] InvoiceGenerator - PDF saved: Invoices\Invoice_1.pdf
```

DAO-level logging is set to WARN to suppress SQL noise. Application level is INFO.

---

## Default Credentials

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | ADMIN |
| `salesman` | `sales123` | SALESMAN |

Seeded on first launch only (when the users table is empty). Passwords are BCrypt-hashed — the values above are only needed for the very first login.

---

## Known Limitations

- **Single-user only.** Concurrent access from multiple machines is not supported.
- **Windows target.** Data paths use `C:\ProgramData\PharmDesk\`; non-Windows systems fall back to `~/PharmDesk/` for development only.
- **Backups are local.** Copy `backups\` to external storage regularly.
- **App restart required after restore.** The SQLite connection is not reloaded at runtime.
- **No reporting module.** Monthly P&L, tax summaries, etc. are not included in this version.
- **Price changes require a new batch.** Editing existing batch prices is intentionally blocked to preserve historical invoice accuracy.

---

## License

This project is released for personal and commercial use. See [LICENSE](LICENSE) for details.