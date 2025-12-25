# 📓 IdeaJar Development Log & Technical Report

**Project Status:** Active (Release Candidate)
**Lead Developer:** Synthetic Sage
**Target Device:** Redmi Note 9 Pro Max (Primary Testbench)

---

## 🟢 v1.2: "Event Horizon" Update (Technical Report)
**Date:** 2025-12-25
**Version:** 1.2
**Focus:** Android 15 Compliance, Data Sovereignty, & Visual Overhaul

### 1. Android 15 Service Compatibility
* **Problem:** The app crashed immediately upon launching the background service on Android 14/15 devices.
* **Root Cause:** Android 14+ enforces strict `foregroundServiceType` declarations. The OS kills any service that doesn't declare its type. `shortService` was insufficient for a permanent overlay.
* **Solution:**
    * Updated `AndroidManifest.xml` to use `android:foregroundServiceType="specialUse"`.
    * Added the required `android.permission.FOREGROUND_SERVICE_SPECIAL_USE`.
    * Defined the strict `<property>` tag to describe the overlay function to the OS.

### 2. Data Safety Architecture ("Mission Control")
* **Problem:** User data was trapped in the app's internal database. Reinstalling the app (or clearing storage) resulted in total data loss.
* **Solution:** Implemented a full JSON Export/Import system.
    * **Export:** Serializes the Room Database (`Note` and `Category` entities) into a JSON file using Gson.
    * **Import:** "Destructive Load" strategy—wipes the current database and repopulates it from the JSON file to ensure ID consistency.
    * **UI:** Added "Save Universe" and "Load Universe" buttons in Settings.

### 3. Visual & UX Polish ("Neon Mode")
* **Problem:**
    * **Visibility:** Gray note bubbles were hard to see against the black space background.
    * **Confusion:** Users mistook the "Center Focus" icon for a Camera button.
    * **Navigation:** Users felt "trapped" in the overlay without a clear Exit button.
* **Solution:**
    * **Neon Visuals:** Notes now default to a **Cyan (Holo Blue)** color with a white border for high contrast.
    * **Recalibrate Button:** Replaced the icon with `Icons.Default.Refresh` and moved it to the Top-Right (HUD style).
    * **Home Button:** Added a permanent "Back" arrow in the Top-Left of the overlay.

---

## 🟡 Legacy Fix: Input Closure Bug (v1.0 Patch)

**Status:** FIXED
**Component:** `CaptureActivity.kt`
**Device Spec:** Redmi Note 9 Pro Max (Android 12)

### Problem Description
The `CaptureActivity` (translucent modal) would close unexpectedly when the user pressed the "Enter" key on the "Signal Data" field.

### Root Cause Analysis
1.  **IME Action Misinterpretation:** The manufacturer's default keyboard treated the Enter key as an "Action Done" signal rather than a raw key code, triggering the system to close the IME and potentially the Activity.
2.  **Event Propagation (Ghost Clicks):** Keyboard interactions seemingly propagated click events to the underlying background scrim `Box`, which had a `.clickable { activity.finish() }` modifier.

### Implemented Solution
1.  **"Nuclear" Key Trap:** We bypassed the IME's default behavior by intercepting the hardware key event directly (`onPreviewKeyEvent`) to manually insert newlines.
2.  **Interaction Shield:** We hardened the UI container (`Card`) to explicitly consume all touch interactions (`MutableInteractionSource`), preventing any "fall-through" events to the background.
3.  **Crash Guard:** Wrapped manual text manipulation in a `try-catch` block to prevent `TextFieldValue` index out-of-bounds crashes.

---

## 📝 Recommendations for Future Iterations
* **Database Migration:** For v1.3, implement `AutoMigration` in Room to handle schema changes without wiping user data.
* **Gesture Conflict:** The "Long Press" to drag Black Holes (Categories) can sometimes conflict with panning. Consider adding a "Lock/Unlock" toggle for categories.
