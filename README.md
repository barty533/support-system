# AutoPrime — Conversational AI Support System

A multi-agent conversational support system for a car dealership, built in Java using the Anthropic Claude API.

---

## Architecture

```
User
 │
 ▼
Orchestrator (Router)
 │  Classifies each message into: TECHNICAL | BILLING | OTHER
 │
 ├─► Agent A – Technical Specialist
 │       Retrieves relevant sections from local documentation files (RAG).
 │       Answers strictly based on retrieved content — no outside knowledge.
 │
 ├─► Agent B – Finance & Billing Specialist
 │       Responds exclusively through tool-calling.
 │       All answers are derived from backend function results.
 │
 └─► Fallback
         Gracefully handles unrelated or out-of-scope requests.
```

Full conversation history is maintained across all turns and agent switches.

---

## Prerequisites

- Java 17 or higher
- Maven 3.8 or higher
- An Anthropic API key — obtain one at [console.anthropic.com](https://console.anthropic.com)

---

## Setting Up Your API Key

The application reads the Anthropic API key from the `ANTHROPIC_API_KEY` environment variable.

### macOS / Linux

**Session only** (key is lost when the terminal is closed):
```bash
export ANTHROPIC_API_KEY=sk-ant-your-key-here
```

**Permanent** (persists across all future sessions):
```bash
# For Zsh (default on macOS Catalina and later):
echo 'export ANTHROPIC_API_KEY=sk-ant-your-key-here' >> ~/.zshrc
source ~/.zshrc

# For Bash:
echo 'export ANTHROPIC_API_KEY=sk-ant-your-key-here' >> ~/.bashrc
source ~/.bashrc
```

Verify the key is set:
```bash
echo $ANTHROPIC_API_KEY
```

### Windows

**Session only — Command Prompt:**
```cmd
set ANTHROPIC_API_KEY=sk-ant-your-key-here
```

**Session only — PowerShell:**
```powershell
$env:ANTHROPIC_API_KEY="sk-ant-your-key-here"
```

**Permanent — via System Settings (recommended):**
1. Open the Start menu and search for **Environment Variables**.
2. Click **Edit the system environment variables**.
3. In the System Properties window click **Environment Variables**.
4. Under **User variables** click **New**.
5. Set the variable name to `ANTHROPIC_API_KEY` and the value to your key.
6. Click OK on all windows. Restart any open terminals for the change to take effect.

Verify the key is set (Command Prompt):
```cmd
echo %ANTHROPIC_API_KEY%
```

---

## Getting Started

**1. Clone the repository**
```bash
git clone https://github.com/barty533/support-system
cd support-agents
```

**2. Set your API key** — see instructions above

**3. Build**
```bash
mvn clean package
```

**4. Run**

The jar must be executed from the `support-agents` directory so it can locate the `docs/` folder.
```bash
java -jar target/support-agents-1.0.0-jar-with-dependencies.jar
```

Type a message and press Enter. Type `exit` to end the session.

---

## Agent A — Technical Specialist (RAG)

Answers questions using only content retrieved from the following documentation files:

| File | Contents |
|------|---------|
| `vehicle-inventory.md` | New and pre-owned listings, specs, prices, colours, and stock IDs |
| `vehicle-diagnostics.md` | Warning lights, OBD-II fault codes, freeze frame data, scan procedure |
| `engine-transmission-electrical.md` | Engine faults, transmission diagnosis, battery, alternator, fuses, ECU modules |
| `maintenance-schedule.md` | Service intervals, oil change procedure, tyre rotation, brake inspection |

If the answer is not present in the documentation, Agent A will say so and direct the customer to call the service team.

---

## Agent B — Finance & Billing Specialist (Tool-Calling)

Responds exclusively by invoking backend functions. Available tools:

| Tool | Purpose |
|------|---------|
| `get_financing_plan` | Retrieve loan details: vehicle, APR, monthly payment, remaining balance |
| `get_payment_history` | Retrieve recent monthly payment records |
| `get_billing_statement` | Retrieve dealership invoices and service charges |
| `get_warranty_info` | Retrieve warranty coverage details for the customer's vehicle |
| `open_service_claim` | Open a warranty repair or service claim |
| `open_refund_request` | Open a refund or return case |

Agent B never answers from its own knowledge — every response is grounded in tool output.

Available customer accounts for testing:

| Customer ID | Name |
|---|---|
| CUST-101 | James Hartley |
| CUST-102 | Maria Santos |
| CUST-103 | Derek Okafor |
| CUST-104 | Sophie Brennan |
| CUST-105 | Carlos Rivera |
| CUST-106 | Priya Nair |
| CUST-107 | Tom Whitfield |
| CUST-108 | Aisha Kamara |

---

## Routing Logic

| User message | Intent | Handler |
|---|---|---|
| "What SUVs do you have?" | TECHNICAL | Agent A |
| "Do you have any Hondas?" | TECHNICAL | Agent A |
| "My check engine light is flashing" | TECHNICAL | Agent A |
| "When should I change my oil?" | TECHNICAL | Agent A |
| "What are my monthly payments?" | BILLING | Agent B |
| "Show my billing statement" | BILLING | Agent B |
| "What does my warranty cover?" | BILLING | Agent B |
| "I want to open a warranty claim" | BILLING | Agent B |
| Unrelated topics | OTHER | Fallback |

---

## Example Conversations

### Technical — inventory

```
You: What trucks do you have in stock?

[Orchestrator] Routing intent: TECHNICAL
[AgentA] Retrieved 4 chunk(s): vehicle-inventory.md

Support: We currently have the following new trucks in stock:
  - Ford F-150 XLT 2025 — Oxford White | 3.5L EcoBoost V6 | Towing: 13,000 lbs | $43,500
  - Ford F-150 Lariat 2025 — Antimatter Blue | 3.5L EcoBoost V6 | $54,200
  - Toyota Tacoma TRD Off-Road 2025 — Army Green | 2.4L Turbo | Towing: 6,500 lbs | $41,300
  - Toyota Tacoma SR5 2025 — Super White | $36,900
  - Chevrolet Silverado LT 2025 — Black | 2.7L Turbo | Towing: 11,500 lbs | $46,700
```

---

### Technical — diagnostics

```
You: My check engine light is flashing, what does that mean?

[Orchestrator] Routing intent: TECHNICAL

Support: A flashing check engine light indicates an active engine misfire,
which risks serious damage to the catalytic converter. Stop driving immediately
and have the vehicle towed to a service centre.
Please call our service team at (555) 123-4567.
```

---

### Billing — warranty enquiry with name resolution

```
You: What does my warranty cover?

Support: I'd be happy to help. Could you please provide your full name or customer ID?

You: James Hartley

[Orchestrator] Routing intent: BILLING
[AgentB] Calling tool: get_warranty_info

Support: Your 2024 Toyota RAV4 AWD has the following active warranties:
  Basic (Bumper-to-Bumper): 3 years / 36,000 miles
  Powertrain: 5 years / 60,000 miles
  Roadside Assistance: 2 years / unlimited miles
```

---

### Agent switching

```
You: My battery warning light came on — what should I do?

[Orchestrator] Routing intent: TECHNICAL

Support: The battery warning light means the charging system is not maintaining
voltage. This is most likely a failing alternator or broken drive belt.
Drive directly to a service centre — the vehicle may stall once the battery drains.

---

You: Can you show me my recent charges?

[Orchestrator] Routing intent: BILLING
[AgentB] Calling tool: get_billing_statement

Support: Here are your recent dealership charges for James Hartley:
  INV-A101 | Vehicle purchase deposit     | $5,000.00 | Paid
  INV-A102 | Documentation fee            |   $299.00 | Paid
  INV-A103 | Dealer preparation fee       |   $249.00 | Paid
  INV-A104 | 5,000-mile service           |   $129.00 | Paid
  INV-A105 | Cabin air filter replacement |    $45.00 | Paid
```

---

### Unrelated topic

```
You: Can you recommend a good restaurant nearby?

[Orchestrator] Routing intent: OTHER

Support: I'm sorry, but I cannot assist with that request.
Please contact our support team at support@autoprime.com for further help.
```

---

## Project Structure

```
support-agents/
├── docs/
│   ├── vehicle-inventory.md
│   ├── vehicle-diagnostics.md
│   ├── engine-transmission-electrical.md
│   └── maintenance-schedule.md
├── src/main/java/com/support/
│   ├── Main.java                        # Entry point and conversation loop
│   ├── Orchestrator.java                # Intent classification and routing
│   ├── agents/
│   │   ├── AgentA.java                  # Technical Specialist (RAG)
│   │   └── AgentB.java                  # Finance & Billing Specialist (tool-calling)
│   ├── model/
│   │   ├── Config.java                  # Centralised configuration constants
│   │   ├── Message.java                 # Typed message record
│   │   ├── ClaudeClient.java            # Anthropic API HTTP client with retry logic
│   │   └── ConversationHistory.java     # Multi-turn message store
│   ├── rag/
│   │   └── DocumentStore.java           # Document loader and TF-IDF retrieval (cached IDF)
│   └── tools/
│       └── BillingTools.java            # Tool definitions and mock backend functions
└── pom.xml
```

---

## Design Notes

- **Agent A** answers exclusively from retrieved documentation. If no relevant chunk is found, it declines rather than guessing.
- **Agent B** answers exclusively from tool output using the correct Anthropic `tool_result` content block format. Its system prompt explicitly forbids answering from internal knowledge.
- **Manual orchestration** — routing, the RAG pipeline, and the tool-calling loop are all implemented directly in Java without any agentic libraries.
- **Cached IDF** — document IDF scores are computed once at startup and cached, avoiding redundant recalculation on every query.
- **Typed messages** — conversation messages use a `Message` record instead of raw `Map<String, String>`, improving type safety and readability.
- **Centralised config** — model name, token limits, retry settings, and RAG parameters are all defined in `Config.java`.
- **Retry logic** — the API client retries automatically on 429 (rate limit) and 5xx (server error) responses using exponential backoff.
- **Context-aware routing** — the router receives the last two conversation turns so follow-up replies such as a customer providing their name are classified correctly.
- **Context preservation** — full conversation history is included in every agent call, enabling coherent multi-turn dialogue across agent switches.
