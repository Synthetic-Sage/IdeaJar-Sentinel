# Technical Report: Input Closure Bug Fix (Redmi Note 9 Pro Max)

**Status:** FIXED
**Component:** `CaptureActivity.kt`
**Device Spec:** Redmi Note 9 Pro Max (Android 12)

## Problem Description
The `CaptureActivity` (translucent modal) would close unexpectedly when the user pressed the "Enter" key on the "Signal Data" field. Or, in later iterations, the "Notes Panel" would disappear (crash/restart).

## Root Cause Analysis
1.  **IME Action Misinterpretation:** The manufacturer's default keyboard treated the Enter key as an "Action Done" signal rather than a raw key code, triggering the system to close the IME and potentially the Activity (if `imeAction` wasn't strictly handled).
2.  **Event Propagation (Ghost Clicks):** Even when the Enter key was trapped, interacting with the keyboard or tapping near the edge seemingly propagated click events to the underlying background scrim `Box`, which had a `.clickable { activity.finish() }` modifier.
3.  **Text Manipulation Crash:** Switching to `TextFieldValue` for cursor control introduced a regression where `replaceRange` could throw an exception if the selection state was unstable during the key event, causing the Activity to crash (appearing as "disappearing").

## Implemented Solution

### 1. "Nuclear" Key Trap
We bypassed the IME's default behavior by intercepting the hardware key event directly:
```kotlin
.onPreviewKeyEvent {
    if (it.key == Key.Enter && it.type == KeyEventType.KeyDown) {
        // Manually handle New Line
        true // Consume event
    } else false
}
```

### 2. Interaction Shield
We hardened the UI container (`Card`) to explicitly consume all touch interactions, preventing any "fall-through" events to the background:
```kotlin
.clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null
) {} // Consumes click
```

### 3. Crash Guard
Wrapped the manual text manipulation login in a `try-catch` block. If the complex `TextFieldValue` manipulation fails (e.g., due to index out of bounds), it falls back to a safe string append operation.

### 4. Manifest Configuration
Enforced `android:windowSoftInputMode="stateVisible|adjustResize"` in `AndroidManifest.xml` to ensure the layout resizes correctly with the keyboard, preventing layout-shift-induced closures.

## Recommendation for Future Devs
- **Avoid Background Click Listeners in Modals with Input:** On sensitive devices, it is safer to rely solely on an explicit "Close" button rather than a background click listener, as keyboard interactions can trigger false positives.
- **Use `TextFieldValue`:** For any input requiring text manipulation (like inserting newlines manually), avoid simple `String` state and use `TextFieldValue` to maintain cursor position integrity.
