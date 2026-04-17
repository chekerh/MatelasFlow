# 📦 Pack/Bundle Solution Proposals

## Overview
You want to sell packs (bundles) like: **190/160 SOFT + 190/90 SOFT = 700 DINARS**

Here are three solution approaches, from simplest to most robust:

---

## 🟢 Option 1: Simple Pack UI (Recommended for Quick Start)

### How it works:
- Add a **toggle/radio button** in the transaction overlay: "Article individuel" vs "Pack"
- When "Pack" is selected:
  - Replace mattress dropdown with a **pack selection dropdown**
  - Packs are stored as **simple JSON configuration** or in-memory data
  - Pack definition includes: name, price, and list of mattresses with quantities
- When a pack is sold:
  - Creates **multiple separate transactions** (one per mattress in the pack)
  - Each transaction references the same pack sale in notes
  - Total price is split proportionally or recorded in first transaction

### Database Changes:
- ✅ **NO database schema changes needed**
- Packs stored as configuration (JSON file or hardcoded constants)

### Code Structure:
```java
// Pack.java (simple model class)
class Pack {
    String name;
    double price;
    List<PackItem> items; // PackItem(mattressId, quantity)
}

// PackService.java
class PackService {
    static List<Pack> getAllPacks();
    static void sellPack(Pack pack, int quantity);
}
```

### Pros:
- ✅ **Fastest to implement** (1-2 hours)
- ✅ No database migration needed
- ✅ Easy to add/remove packs (just edit config)
- ✅ Simple to understand

### Cons:
- ❌ Packs not stored in database (lost on restart if not persisted to file)
- ❌ Can't easily track pack sales statistics
- ❌ Pack changes require code/config updates

---

## 🟡 Option 2: Pack Entity in Database (Recommended for Production)

### How it works:
- Create **3 new database tables**:
  1. `packs` - Stores pack definitions (id, name, price, active)
  2. `pack_items` - Links mattresses to packs (pack_id, mattress_id, quantity)
  3. Optional: Add `pack_id` to `transaction` table to link sales to packs
- When pack is sold:
  - Creates one "Pack Sale" transaction record
  - Plus individual transactions for each mattress in the pack
  - Or creates multiple transactions linked by a pack_sale_id

### Database Schema:
```sql
CREATE TABLE packs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    description TEXT,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE pack_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pack_id INT NOT NULL,
    mattress_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    FOREIGN KEY (pack_id) REFERENCES packs(id) ON DELETE CASCADE,
    FOREIGN KEY (mattress_id) REFERENCES mattress(id) ON DELETE RESTRICT
);

-- Optionally add to transaction table:
ALTER TABLE transaction ADD COLUMN pack_id INT NULL;
ALTER TABLE transaction ADD FOREIGN KEY (pack_id) REFERENCES packs(id);
```

### Code Structure:
```java
// Pack.java, PackDAO.java (full CRUD operations)
// PackItem.java, PackItemDAO.java
// PackService.java with pack management UI
```

### Pros:
- ✅ **Most robust and scalable**
- ✅ Packs persisted in database
- ✅ Can track pack sales history
- ✅ Can enable/disable packs dynamically
- ✅ Can build pack management UI later

### Cons:
- ❌ **More complex** (4-6 hours implementation)
- ❌ Requires database migration
- ❌ More tables to maintain

---

## 🔴 Option 3: Hybrid Approach (Best of Both Worlds)

### How it works:
- Use **Option 1** for the UI/UX (simple pack selection)
- Store pack definitions in a **JSON configuration file** (`packs.json`)
- When pack is sold, create **multiple transactions** with pack reference in notes
- Later, can migrate to **Option 2** (database) without changing UI

### Implementation:
1. Create `packs.json` file:
```json
{
  "packs": [
    {
      "name": "Pack Complet 190/160 + 190/90",
      "price": 700.00,
      "items": [
        {"mattressType": "SOFT", "size": "190/160", "quantity": 1},
        {"mattressType": "SOFT", "size": "190/90", "quantity": 1}
      ]
    }
  ]
}
```

2. Create PackConfigReader to load packs from JSON
3. UI: Add pack selector similar to Option 1
4. On sale: Create multiple transactions linked by pack name in notes

### Pros:
- ✅ Easy to implement (2-3 hours)
- ✅ Packs persist (in JSON file)
- ✅ Can migrate to database later
- ✅ Easy to edit packs (just edit JSON)

### Cons:
- ❌ Pack matching by name/type (less precise than ID)
- ❌ JSON file can be corrupted
- ❌ Less robust than database option

---

## 🎯 My Recommendation

For **quick implementation**: **Option 3 (Hybrid)**

For **production/long-term**: **Option 2 (Database)**

---

## 📋 Implementation Steps (For Option 3 - Hybrid)

1. ✅ Create `packs.json` configuration file
2. ✅ Create `Pack.java` model class
3. ✅ Create `PackConfigReader.java` to load packs
4. ✅ Update `TransactionOverlay.fxml` - Add pack/individual toggle
5. ✅ Update `TransactionOverlayController.java` - Add pack selection logic
6. ✅ Update `handleOk()` method - Handle pack sales (create multiple transactions)
7. ✅ Test with your example pack: 190/160 SOFT + 190/90 SOFT = 700 DT

---

## ❓ Which option do you prefer?

Please let me know:
1. **Option 1** - Quickest, no database changes
2. **Option 2** - Most robust, requires database migration  
3. **Option 3** - Balanced approach (my recommendation)

Or I can proceed with **Option 3** if you'd like me to start implementing!

