# 🌌 IdeaJar: The Gravity-Based Note Archive

> *"Order from Chaos."*

**IdeaJar** is not a normal to-do list. It is an offline-first, physics-based memory vault designed for "Lone Wolf" thinkers. Notes are not static text rows; they are physical entities that orbit, drift, and collide in a simulated zero-gravity environment.

### 🚀 Latest Release: v1.2 "Event Horizon"
* **Android 15 Ready:** Full support for `FOREGROUND_SERVICE_SPECIAL_USE`.
* **Mission Control:** Backup and Restore your entire universe (JSON Export).
* **Neon Visuals:** High-contrast HUD for deep space visibility.

---

## ⚡ Core Features

### ⚛️ Physics Simulation
* **Zero-G Environment:** Notes float and react to simulated gravity.
* **Black Hole Categorization:** Drag notes into massive "Category Stars" to organize them.
* **Interactive Chaos:** Fling, drag, and collide thoughts to spark new ideas.

### 🛡️ The Sentinel Overlay (Foreground Service)
* **Always Ready:** A persistent "Shake Detection" service runs in the background.
* **Quick Capture:** Shake your phone anytime to spawn a Quick Note without opening the app.
* **Offline First:** No cloud, no ads, no tracking. Your data stays on your device.

### 🔧 Mission Control (Settings)
* **Data Sovereignty:** Export your database to JSON. Restore it on any device.
* **Privacy:** Built-in "Incognito Mode" (App does not use Internet permissions except for local loopback).

---

## 🛠️ Technical Stack

* **Language:** Kotlin (100%)
* **UI Framework:** Jetpack Compose (Modern Reactive UI)
* **Architecture:** MVVM (Model-View-ViewModel)
* **Database:** Room (SQLite) with Coroutines
* **Physics Engine:** Custom 2D Vector Physics (Velocity, Friction, Collision)
* **Background:** Android Foreground Services (`dataSync` / `specialUse`)

---

## 📥 Installation (Side-Load)

Since this app is a "Rogue Tool" (not on Play Store), you must install it manually.

1.  Download the latest `.apk` from the **[Releases]** tab.
2.  **Android 14/15 Users:** If "Play Protect" blocks the install:
    * *Option A:* Turn on **Airplane Mode** ✈️ before installing.
    * *Option B:* Click "More Details" -> "Install Anyway."
3.  **Permissions:** Grant "Display Over Other Apps" when prompted (required for the Quick Capture overlay).

---

## 👨‍💻 Developer Log
Maintained by **Synthetic Sage**.
* **v1.0:** Initial Launch (The Silent Sentinel).
* **v1.1:** Stability Patch (Crash Fixes).
* **v1.2:** Event Horizon (Data Backup & A15 Support).

[Read Full Development Report](DEV_REPORT.md)