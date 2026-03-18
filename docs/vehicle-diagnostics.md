# Vehicle Diagnostics Guide

## Warning Lights Reference

### Check Engine Light (CEL)
- Solid yellow/orange: non-critical fault detected; safe to drive but requires diagnosis soon.
- Flashing/blinking: active misfire detected; risk of catalytic converter damage. Stop driving immediately and call for service.

Common OBD-II codes associated with CEL:
- P0300 – Random/multiple cylinder misfire detected
- P0420 – Catalyst system efficiency below threshold (Bank 1)
- P0171 – System too lean (Bank 1); check for vacuum leaks or dirty MAF sensor
- P0174 – System too lean (Bank 2)
- P0128 – Coolant temperature below thermostat regulating temperature

### Oil Pressure Warning Light (red oil can icon)
- Stop the engine immediately. Driving with low oil pressure causes severe engine damage within minutes.
- Check oil level on the dipstick. If oil level is correct, do not restart — the pump or sensor may be faulty.
- Top up oil only if level is low; then recheck the light.

### Battery / Charging Warning Light (red battery icon)
- Indicates the charging system is not maintaining battery voltage.
- Likely causes: failing alternator, broken drive belt, corroded battery terminals.
- Drive directly to a service centre; the vehicle may stall when the battery drains.

### Temperature Warning Light (red thermometer icon)
- Engine is overheating. Pull over safely and switch off the engine immediately.
- Do NOT open the bonnet or radiator cap while the engine is hot.
- Allow 30 minutes to cool before inspecting coolant level.
- Causes: low coolant, faulty thermostat, blocked radiator, failing water pump.

### Tyre Pressure Monitoring System (TPMS) Light (horseshoe with exclamation mark)
- One or more tyres is significantly under-inflated (typically >25% below recommended PSI).
- Check all four tyres and the spare. Recommended PSI is printed on the sticker inside the driver's door jamb.
- If tyre pressures are correct and light persists, a TPMS sensor may be faulty.

### Brake Warning Light (red circle with exclamation)
- If parking brake is off: indicates low brake fluid, worn brake pads, or a hydraulic fault.
- Low brake fluid may signal a leak — do not drive; have the vehicle towed.
- If paired with ABS light: electronic brake system fault; consult a technician.

### ABS Warning Light
- Anti-lock braking system has a fault. Normal braking still works; ABS may not activate in emergency stops.
- Common causes: faulty wheel speed sensor, damaged ABS ring, low brake fluid.

---

## Diagnostic Scan Procedure

1. Connect an OBD-II scanner to the port located under the dashboard on the driver's side.
2. Turn the ignition to the ON position (engine off).
3. Select "Read Codes" on the scanner.
4. Record all fault codes (both active and pending).
5. Cross-reference codes against manufacturer TSBs (Technical Service Bulletins) before clearing.
6. Do not clear codes before diagnosis — cleared codes remove freeze-frame data needed for diagnosis.

---

## Freeze Frame Data

When a fault code is logged, the ECU stores a snapshot of sensor values at the moment of the fault. This data includes:
- Engine RPM
- Vehicle speed
- Coolant temperature
- Fuel trim values (short-term and long-term)
- Throttle position

Always retrieve freeze frame data before clearing codes to assist accurate diagnosis.
