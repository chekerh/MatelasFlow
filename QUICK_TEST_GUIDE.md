# 🧪 Quick Test Guide - What Changed & How to Test

## 🎯 **Test in 5 Minutes!**

### **1. Test Transaction Overlay (Main Fix)**

**Run:**
```bash
cd C:\Users\Dell\Documents\matress
mvn javafx:run
```

**Steps:**
1. Login (admin/admin)
2. Click "💶 Transactions" in left navbar
3. Click "Ajouter" button

**✅ EXPECTED CHANGES:**

**BEFORE (Your Screenshot):**
```
❌ Labels showing only icons
❌ "Veuillez sélectionner un matelas" error
❌ No discount option
❌ No stock visibility
```

**AFTER (Now):**
```
✅ All labels visible: "📋 Type:", "🛏️ Matelas:", "📦 Quantité:"
✅ No more mattress error
✅ Discount field appears for sales (💸 Remise (%))
✅ Stock indicators on mattresses (✅ ⚠️ 🔴)
```

---

### **2. Test Discount Field**

**Steps:**
1. In transaction overlay
2. Select "💰 Vente" as type
3. Look for "💸 Remise (%)" field

**✅ VERIFY:**
- Field appears (only for Vente)
- Enter discount: 10
- Enter price: 120€
- Save transaction
- Check transaction table: final price should be 108€

**Example:**
```
Original Price: 120.00€
Discount: 10%
Final Price: 108.00€ ← Saved automatically!
```

---

### **3. Test Stock Indicators**

**Steps:**
1. In transaction overlay
2. Click on "🛏️ Matelas" dropdown

**✅ VERIFY:**
You should see mattresses with stock levels:
```
✅ Mousse (90x190) - Stock: 15    ← Good stock
⚠️ Ressort (140x190) - Stock: 8  ← Medium stock
🔴 Latex (160x200) - Stock: 3    ← Low stock
❌ Mémoire (180x200) - Stock: 0  ← Out of stock
```

**Colors:**
- Green ✅: Stock > 10
- Yellow ⚠️: Stock 6-10
- Red 🔴: Stock 1-5
- Black ❌: Stock = 0

---

### **4. Test App Icon**

**Steps:**
1. Run application
2. Look at Windows taskbar

**✅ VERIFY:**
- App icon is SuperMousse logo (not Java icon)
- Visible in taskbar
- Visible when Alt+Tab

---

### **5. Test Login Logo**

**Steps:**
1. Logout or restart app
2. Look at login screen

**✅ VERIFY:**
- Logo is SuperMousse.jpg
- Size: 100x100 pixels
- Centered above login form

---

## 🎨 **Visual Comparison**

### **Transaction Form - BEFORE:**
```
┌─────────────────────────────────────┐
│ 💶 Ajouter une transaction          │
├─────────────────────────────────────┤
│ 📋 [Mousse ▼]                       │  ← Label cut off
│ 🛏️ [Mousse (90x190) ▼]             │  ← No stock info
│ 📦 [2_____]                         │  ← No label visible
│ 💰 [120___]                         │
│ 🏪 [Owner ▼]                        │
│ 📝 [Notes_]                         │
│                                     │
│         [✖ Annuler]  [✔ Valider]   │
└─────────────────────────────────────┘
```

### **Transaction Form - AFTER:**
```
┌──────────────────────────────────────────────────┐
│ 💶 Ajouter une transaction                       │
├──────────────────────────────────────────────────┤
│ 📋 Type:          [💰 Vente ▼]                   │  ← Full label!
│ 🛏️ Matelas:       [✅ Mousse (90x190) - Stock: 15 ▼] ← Stock!
│ 📦 Quantité:      [2________________________]    │  ← Full label!
│ 💰 Prix (€):      [120______________________]    │
│ 💸 Remise (%):    [10_______________________]    │  ← NEW!
│ 🏪 Propriétaire:  [Non applicable pour ventes]   │
│ 📝 Notes:         [Client régulier__________]    │
│                                                  │
│              [✖ Annuler]  [✔ Valider]           │
└──────────────────────────────────────────────────┘
```

---

## 🔍 **Specific Issues from Your Screenshots**

### **Screenshot 1 - Transaction Overlay Issues:**

**Your Issues:**
- ❌ "Type" label not showing text
- ❌ "Matelas" label not showing text  
- ❌ Getting error even though mattress selected
- ❌ No discount option

**All Fixed:**
- ✅ Labels show full text with icons
- ✅ Mattress selection works properly
- ✅ Discount field added
- ✅ Stock indicators added

---

### **Screenshot 2 - Statistics Page:**

**Your Concern:** "No navbar"

**Actually:** Navbar IS visible in your screenshot! (Left side with purple buttons)

**Explanation:**
- Navbar is always visible (left side)
- Statistics content loads in main area (right side)
- This is correct design
- No changes needed

---

## ✨ **New Features to Try**

### **Discount System:**
```
Scenario: Selling 2 mattresses with 15% discount

1. Select "💰 Vente"
2. Mattress: Mousse (90x190)
3. Quantity: 2
4. Price: 240€ (120€ each)
5. Discount: 15%
6. Click Valider

Result:
- Final price: 204€ (saved)
- Notes: "Vente avec remise 15% - Prix original: 240.00€, Prix final: 204.00€"
- Discount amount: 36€
```

### **Stock Warnings:**
```
When selecting mattress:
- ✅ Green = Plenty in stock (>10)
- ⚠️ Yellow = Reorder soon (6-10)
- 🔴 Red = Running low! (1-5)
- ❌ Black = Out of stock (0)
```

---

## 🐛 **If Something Doesn't Work**

### **Problem: Labels still not showing**
**Solution:** 
1. Clear Maven cache: `mvn clean`
2. Recompile: `mvn compile`
3. Run: `mvn javafx:run`

### **Problem: Mattress error still appears**
**Solution:**
1. Make sure you click on dropdown
2. Select a mattress from list
3. Don't leave it empty

### **Problem: Discount field not appearing**
**Solution:**
1. Select "💰 Vente" as transaction type
2. Field appears only for sales
3. Check if field is below "Prix (€)"

### **Problem: Icon not showing in taskbar**
**Solution:**
1. Make sure SuperMousse.jpg exists at: `src/main/resources/images/SuperMousse.jpg`
2. Restart application
3. Check Windows taskbar

---

## 📊 **Quick Verification Checklist**

Run through this in 3 minutes:

- [ ] 1. App icon shows SuperMousse in taskbar
- [ ] 2. Login logo shows SuperMousse (100x100)
- [ ] 3. Transaction overlay labels show text
- [ ] 4. Mattress dropdown shows stock indicators
- [ ] 5. Discount field appears for Vente
- [ ] 6. Mattress selection works (no error)
- [ ] 7. All transaction types save properly
- [ ] 8. Statistics page has navbar visible
- [ ] 9. No compilation errors
- [ ] 10. No runtime crashes

**If all ✅ → Perfect! All fixes working!**

---

## 🎯 **Most Important Tests**

### **#1 Priority: Transaction Add**
```
1. Click Transactions → Ajouter
2. Verify labels show text
3. Select mattress (verify stock shows)
4. Enter quantity & price
5. For Vente: enter discount
6. Click Valider
7. Verify saves without error
```

### **#2 Priority: Discount Calculation**
```
1. Type: Vente
2. Price: 100€
3. Discount: 20%
4. Expected: 80€ final price
5. Check transaction notes for discount details
```

### **#3 Priority: Stock Indicators**
```
1. Open mattress dropdown
2. Verify each mattress shows:
   - Icon (✅ ⚠️ 🔴 ❌)
   - Type and size
   - Stock number
3. Select any mattress
4. Verify it works without error
```

---

## 🚀 **Performance Check**

**Expected Response Times:**
- App launch: < 5 seconds
- Transaction overlay open: < 1 second
- Mattress dropdown load: < 1 second
- Transaction save: < 2 seconds
- Page navigation: < 1 second

**If slower:** May need database optimization

---

## 💡 **Tips for Testing**

1. **Test with real data** - Use actual mattress types and prices
2. **Try edge cases** - 0% discount, 100% discount, empty fields
3. **Test all types** - Not just Vente, try Prêt, Transfert, etc.
4. **Check database** - Verify data saves correctly
5. **Test on different screen sizes** - Ensure responsive

---

## 🎉 **Success Indicators**

You know it's working when:

1. **No error popups** when adding transactions
2. **All text visible** in forms
3. **Stock numbers show** in dropdowns
4. **Discounts calculate** automatically
5. **App looks professional** with branding
6. **Data saves correctly** to database
7. **Navigation is smooth** between pages
8. **No console errors** during operation

---

*Test Duration: 5-10 minutes*
*Expected Result: All ✅ checkmarks*
*Next Step: Start using in production!*
