# Engine and Transmission Guide

## Engine Fault Diagnosis

### Engine Misfires
Symptoms: rough idle, loss of power, fuel smell from exhaust, flashing CEL.

Causes and checks:
- Spark plugs: inspect for fouling, wear, or incorrect gap. Replace every 30,000–100,000 miles depending on type (copper vs iridium).
- Ignition coils: test resistance with a multimeter; swap coils between cylinders to confirm fault migration.
- Fuel injectors: listen for clicking at idle; a silent injector is likely clogged or failed.
- Compression test: cylinder compression below 100 PSI or more than 15% variance between cylinders indicates internal engine wear.

### Engine Knocking / Pinging
- Detonation knock: caused by using fuel with too-low octane rating. Switch to the manufacturer-recommended octane.
- Rod knock: deep, rhythmic knock that worsens under load. Indicates worn or damaged connecting rod bearings. Requires immediate engine disassembly.
- Piston slap: hollow, louder when cold, quietens when warm. Caused by worn piston skirts or excessive cylinder wall wear.

### Oil Consumption
- Normal consumption: up to 1 quart per 2,000 miles is considered acceptable on many modern engines.
- Excessive consumption (>1 qt/1,000 mi): check for valve stem seal leaks (blue smoke on startup), piston ring wear (blue smoke under acceleration), or external leaks.
- Check PCV (Positive Crankcase Ventilation) valve — a stuck-closed PCV valve increases crankcase pressure and forces oil past seals.

### Overheating
1. Check coolant level in the reservoir and radiator (when cold).
2. Inspect for coolant leaks: look for white stains around hose connections, radiator, and water pump.
3. Test thermostat: remove and place in boiling water — it should open at rated temperature (typically 88–95°C).
4. Check radiator for blockages: flush with water if heavily scaled.
5. Pressure-test the cooling system to locate slow leaks.
6. Head gasket failure signs: white smoke from exhaust, milky oil (oil mixed with coolant), sweet smell from exhaust.

---

## Transmission Fault Diagnosis

### Automatic Transmission

Symptoms of faults:
- Slipping between gears: RPM rises without corresponding speed increase. Causes: low fluid, worn clutch packs, faulty solenoid.
- Delayed engagement: pause before drive or reverse engages. Low fluid or worn internal seals.
- Harsh/erratic shifting: may indicate a faulty throttle position sensor, solenoid, or dirty fluid.
- No movement in any gear: check fluid level first; if correct, internal mechanical failure likely.

Fluid check procedure:
1. Warm the engine to operating temperature.
2. With engine running, move selector through all positions and return to Park.
3. Remove the transmission dipstick, wipe clean, reinsert fully, and withdraw.
4. Fluid should be in the HOT range, pink/red in colour, and not smell burnt.
5. Dark brown or black fluid with a burnt smell indicates overdue service.

Transmission fluid service interval: every 30,000–60,000 miles (check vehicle manual).

### Manual Transmission

- Difficulty engaging gears: check clutch adjustment and hydraulic fluid level.
- Clutch slip: engine revs rise but speed does not increase. Clutch disc worn; replacement required.
- Clutch judder: vibration when releasing clutch. Oil contamination of clutch disc or worn pressure plate.
- Grinding when shifting: synchroniser rings worn. Indicates transmission overhaul needed.

### Transfer Case (4WD/AWD Vehicles)
- Grinding or popping when engaging 4WD: check transfer case fluid level and condition.
- 4WD will not engage or disengage: may be a faulty encoder motor, shift fork, or control module.
- Vibration at highway speeds in 4WD: do not use 4WD High on dry paved roads — drivetrain wind-up causes this.
# Electrical Systems Guide

## Battery

### Testing
- A fully charged 12V battery should read 12.6V or higher at rest (engine off, no loads).
- Under load (engine cranking), voltage should not drop below 9.6V.
- After charging, surface charge may temporarily read 12.8V+ — allow 30 minutes rest before testing.
- Use a digital multimeter or dedicated battery tester for accurate results.

### Charging
- Trickle charge (1–2A): safest method; maintains battery without risk of overcharging. Use for storage.
- Standard charge (10A): typical overnight charge for a fully discharged battery.
- Boost charge (40A+): fast charge for emergency starts only; reduces battery lifespan if used regularly.
- Do not charge a frozen battery — thaw it to room temperature first.

### Jump-Starting Procedure
1. Connect RED cable: dead battery (+) → good battery (+).
2. Connect BLACK cable: good battery (−) → unpainted metal on the dead vehicle (not the dead battery terminal).
3. Start the donor vehicle; run for 2 minutes.
4. Attempt to start the dead vehicle.
5. Disconnect in reverse order: BLACK from ground, BLACK from donor, RED from donor, RED from recipient.
6. Run the recovered vehicle for at least 30 minutes to allow the alternator to recharge the battery.

### Battery Lifespan
- Average lifespan: 3–5 years.
- Heat significantly shortens battery life (vehicles in hot climates may see 2–3 years).
- Signs of end-of-life: slow cranking, repeated need for jump-starts, swollen battery case.

---

## Alternator

### Function
The alternator charges the battery while the engine runs and powers all electrical loads.

### Output specification
- Typical output: 13.5V–14.8V at idle with engine running.
- Voltage below 13V with engine running indicates a charging fault.
- Voltage above 15V indicates a faulty voltage regulator — can damage electronics and boil the battery.

### Fault diagnosis
- Belt-driven alternator: check drive belt tension and condition first.
- Diode failure: causes AC ripple in the DC output; symptoms include dim flickering lights, battery drain.
- Brush wear: common on high-mileage alternators; brushes are often replaceable without replacing the whole unit.
- Test with a multimeter across battery terminals: 13.5–14.8V with engine running = healthy.

---

## Fuses and Relays

### Locating fuse boxes
Most vehicles have two fuse boxes:
1. Interior fusebox: under the dashboard on the driver's side (low-current circuits: radio, windows, interior lights).
2. Engine bay fusebox: near the battery (high-current circuits: starter, ignition, ABS, cooling fan).

### Identifying a blown fuse
- Visual inspection: the wire element inside the fuse will be broken or melted.
- Multimeter continuity test: set to continuity mode; probe both fuse terminals — a blown fuse gives no continuity.
- Always replace with a fuse of the same amperage rating. Never install a higher-rated fuse.

### Relays
- Relays are electrically-operated switches. A faulty relay can cause a component to not function at all or to run continuously.
- Swap a suspect relay with a known-good relay of the same part number to test.
- Clicking relays with no function: coil is working but contacts are burned — replace the relay.

---

## ECU / Control Modules

### Overview
Modern vehicles have multiple control modules:
- ECM (Engine Control Module): manages fuelling, ignition timing, emissions.
- TCM (Transmission Control Module): manages gear selection and shift timing.
- BCM (Body Control Module): manages lighting, windows, locks, alarms.
- ABS Module: manages anti-lock braking.

### Common issues
- Corroded connectors: clean with electrical contact cleaner; apply dielectric grease.
- Water ingress: modules exposed to water can fail; check floor carpets for dampness.
- Software faults: many issues are resolved with a software reflash/update from the manufacturer.

### When to replace vs. reprogram
- Always attempt a software update before replacing a module.
- Replacement modules usually require programming to the vehicle's VIN before they function.
- Some modules (e.g. BCM, immobiliser-linked ECM) require dealer-level programming tools.
