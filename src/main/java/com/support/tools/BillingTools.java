package com.support.tools;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Mock billing and finance backend for AutoPrime dealership.
 *
 * Contains a simulated customer database with realistic records.
 * In production these functions would query a real database or billing API.
 */
public class BillingTools {

    // -----------------------------------------------------------------------
    // Tool definitions (sent to Claude as the tools list)
    // -----------------------------------------------------------------------

    public static final List<Map<String, Object>> DEFINITIONS = List.of(

        Map.of(
            "name", "get_financing_plan",
            "description", "Returns the customer's current auto loan details: vehicle, monthly payment, APR, term, and remaining balance.",
            "input_schema", Map.of(
                "type", "object",
                "properties", Map.of(
                    "customer_id", Map.of("type", "string", "description", "The customer's account ID (e.g. CUST-101)")
                ),
                "required", List.of("customer_id")
            )
        ),

        Map.of(
            "name", "get_payment_history",
            "description", "Returns the customer's recent monthly loan payment records.",
            "input_schema", Map.of(
                "type", "object",
                "properties", Map.of(
                    "customer_id", Map.of("type", "string", "description", "The customer's account ID")
                ),
                "required", List.of("customer_id")
            )
        ),

        Map.of(
            "name", "get_billing_statement",
            "description", "Returns the customer's dealership invoices and service charges.",
            "input_schema", Map.of(
                "type", "object",
                "properties", Map.of(
                    "customer_id", Map.of("type", "string", "description", "The customer's account ID")
                ),
                "required", List.of("customer_id")
            )
        ),

        Map.of(
            "name", "get_warranty_info",
            "description", "Returns warranty coverage details for the customer's vehicle.",
            "input_schema", Map.of(
                "type", "object",
                "properties", Map.of(
                    "customer_id", Map.of("type", "string", "description", "The customer's account ID"),
                    "vin", Map.of("type", "string", "description", "Vehicle Identification Number (optional)")
                ),
                "required", List.of("customer_id")
            )
        ),

        Map.of(
            "name", "open_service_claim",
            "description", "Opens a warranty service claim for a vehicle fault and returns a claim ID.",
            "input_schema", Map.of(
                "type", "object",
                "properties", Map.of(
                    "customer_id", Map.of("type", "string", "description", "The customer's account ID"),
                    "vin", Map.of("type", "string", "description", "Vehicle Identification Number"),
                    "fault_description", Map.of("type", "string", "description", "Description of the fault or issue")
                ),
                "required", List.of("customer_id", "vin", "fault_description")
            )
        ),

        Map.of(
            "name", "open_refund_request",
            "description", "Opens a refund or vehicle return request and returns a case ID.",
            "input_schema", Map.of(
                "type", "object",
                "properties", Map.of(
                    "customer_id", Map.of("type", "string", "description", "The customer's account ID"),
                    "charge_id", Map.of("type", "string", "description", "The invoice or charge ID to refund"),
                    "reason", Map.of("type", "string", "description", "Reason for the refund request")
                ),
                "required", List.of("customer_id", "charge_id", "reason")
            )
        )
    );

    // -----------------------------------------------------------------------
    // Mock customer database
    // -----------------------------------------------------------------------

    private record Customer(
        String id, String name, String email, String phone,
        String vehicle, String vin, double purchasePrice,
        double downPayment, double loanAmount, double apr,
        int termMonths, int paymentsMade, double monthlyPayment,
        LocalDate purchaseDate, String lender
    ) {
        double remainingBalance() {
            double balance = loanAmount;
            double monthlyRate = apr / 100 / 12;
            for (int i = 0; i < paymentsMade; i++) {
                balance = balance * (1 + monthlyRate) - monthlyPayment;
            }
            return Math.max(balance, 0);
        }
        LocalDate nextPaymentDue() {
            return purchaseDate.plusMonths(paymentsMade + 1);
        }
    }

    private static final Map<String, Customer> CUSTOMERS = Map.of(
        "CUST-101", new Customer(
            "CUST-101", "James Hartley",    "j.hartley@email.com",    "(555) 201-4430",
            "2024 Toyota RAV4 XLE AWD",     "2T3BFREV4PW123456",
            34500, 5000, 29500, 6.5, 60, 8,  576.82,
            LocalDate.now().minusMonths(8),  "Chase Auto Finance"
        ),
        "CUST-102", new Customer(
            "CUST-102", "Maria Santos",     "m.santos@email.com",     "(555) 309-8821",
            "2025 Honda Accord Sport",      "1HGCV2F34SA201873",
            32400, 3000, 29400, 7.2, 72, 3,  451.14,
            LocalDate.now().minusMonths(3),  "Ally Financial"
        ),
        "CUST-103", new Customer(
            "CUST-103", "Derek Okafor",     "d.okafor@email.com",     "(555) 412-6654",
            "2025 Ford F-150 Lariat",       "1FTFW1E50PFA98231",
            54200, 8000, 46200, 5.9, 84, 12, 661.43,
            LocalDate.now().minusMonths(12), "TD Auto Finance"
        ),
        "CUST-104", new Customer(
            "CUST-104", "Sophie Brennan",   "s.brennan@email.com",    "(555) 518-3390",
            "2025 Hyundai IONIQ 6 SE",      "KMHLS4AE2PA045612",
            38615, 6000, 32615, 4.9, 60, 5,  615.07,
            LocalDate.now().minusMonths(5),  "Chase Auto Finance"
        ),
        "CUST-105", new Customer(
            "CUST-105", "Carlos Rivera",    "c.rivera@email.com",     "(555) 627-7712",
            "2024 Kia Telluride EX",        "5XYP5DHC4PG312087",
            44800, 7500, 37300, 6.9, 72, 15, 573.28,
            LocalDate.now().minusMonths(15), "Ally Financial"
        ),
        "CUST-106", new Customer(
            "CUST-106", "Priya Nair",       "p.nair@email.com",       "(555) 733-4401",
            "2023 Honda CR-V Sport",        "7FARW2H82PE011234",
            33700, 4500, 29200, 7.8, 60, 18, 589.41,
            LocalDate.now().minusMonths(18), "TD Auto Finance"
        ),
        "CUST-107", new Customer(
            "CUST-107", "Tom Whitfield",    "t.whitfield@email.com",  "(555) 844-9023",
            "2025 Toyota Camry XSE",        "4T1BZ1HK2PU801345",
            31200, 2500, 28700, 8.1, 60, 2,  584.92,
            LocalDate.now().minusMonths(2),  "Chase Auto Finance"
        ),
        "CUST-108", new Customer(
            "CUST-108", "Aisha Kamara",     "a.kamara@email.com",     "(555) 952-1188",
            "2022 Toyota RAV4 XLE (CPO)",   "2T3P1RFV4NW213908",
            29200, 4000, 25200, 9.5, 48, 22, 632.17,
            LocalDate.now().minusMonths(22), "Ally Financial"
        )
    );

    private static final Customer DEFAULT_CUSTOMER = CUSTOMERS.get("CUST-101");

    private static Customer resolve(String customerId) {
        return CUSTOMERS.getOrDefault(customerId.toUpperCase(), DEFAULT_CUSTOMER);
    }

    // -----------------------------------------------------------------------
    // Tool implementations
    // -----------------------------------------------------------------------

    public static String getFinancingPlan(String customerId) {
        Customer c = resolve(customerId);
        return String.format("""
            Customer ID    : %s
            Name           : %s
            Vehicle        : %s
            VIN            : %s
            Purchase Price : $%.2f
            Down Payment   : $%.2f
            Loan Amount    : $%.2f
            APR            : %.1f%%
            Term           : %d months
            Monthly Payment: $%.2f
            Payments Made  : %d of %d
            Remaining Balance: $%.2f
            Next Payment Due : %s
            Lender         : %s
            """,
            c.id(), c.name(), c.vehicle(), c.vin(),
            c.purchasePrice(), c.downPayment(), c.loanAmount(),
            c.apr(), c.termMonths(), c.monthlyPayment(),
            c.paymentsMade(), c.termMonths(),
            c.remainingBalance(), c.nextPaymentDue(), c.lender());
    }

    public static String getPaymentHistory(String customerId) {
        Customer c = resolve(customerId);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Payment history for %s (%s):\n", c.name(), c.vehicle()));
        int count = Math.min(c.paymentsMade(), 5);
        for (int i = 0; i < count; i++) {
            LocalDate date = c.nextPaymentDue().minusMonths(count - i);
            String payId = String.format("PAY-%04d", 8000 + c.paymentsMade() - i);
            sb.append(String.format("  %s | %s | $%.2f | Paid\n", payId, date, c.monthlyPayment()));
        }
        return sb.toString();
    }

    public static String getBillingStatement(String customerId) {
        Customer c = resolve(customerId);
        LocalDate pd = c.purchaseDate();
        return String.format("""
            Billing statement for %s (%s):
            INV-A%03d | %s | Vehicle purchase deposit     | $%.2f  | Paid
            INV-A%03d | %s | Documentation fee            |  $299.00 | Paid
            INV-A%03d | %s | Dealer preparation fee       |  $249.00 | Paid
            INV-A%03d | %s | 5,000-mile service           |  $129.00 | %s
            INV-A%03d | %s | Cabin air filter replacement |   $45.00 | %s
            """,
            c.name(), c.vehicle(),
            c.id().hashCode() % 900 + 100, pd,              c.downPayment(),
            c.id().hashCode() % 900 + 101, pd,
            c.id().hashCode() % 900 + 102, pd,
            c.id().hashCode() % 900 + 103, pd.plusMonths(4), c.paymentsMade() >= 4 ? "Paid" : "Pending",
            c.id().hashCode() % 900 + 104, pd.plusMonths(8), c.paymentsMade() >= 8 ? "Paid" : "Pending");
    }

    public static String getWarrantyInfo(String customerId, String vin) {
        Customer c = resolve(customerId);
        String vehicleVin = (vin != null && !vin.isBlank()) ? vin : c.vin();
        LocalDate pd = c.purchaseDate();
        boolean isCPO = c.vehicle().contains("CPO");
        return String.format("""
            Warranty information for %s
            Vehicle : %s
            VIN     : %s

            Active warranties:
            1. %s
               Coverage : 3 years / 36,000 miles
               Expires  : %s (or at 36,000 miles)
               Covers   : Defects in materials and workmanship, electrical components,
                          A/C, audio, suspension, and steering.
               Excludes : Wear items (brakes, tyres, wiper blades, filters), accident damage.

            2. Powertrain Warranty
               Coverage : 5 years / 60,000 miles
               Expires  : %s (or at 60,000 miles)
               Covers   : Engine, transmission, and drivetrain.

            3. Roadside Assistance
               Coverage : 2 years / unlimited miles
               Expires  : %s
               Includes : Towing, fuel delivery, flat tyre, lockout service.
            %s
            """,
            c.name(), c.vehicle(), vehicleVin,
            isCPO ? "CPO Limited Warranty" : "Basic (Bumper-to-Bumper) Warranty",
            pd.plusYears(3), pd.plusYears(5), pd.plusYears(2),
            isCPO ? "  (CPO warranty replaces basic warranty for this vehicle)" : "");
    }

    public static String openServiceClaim(String customerId, String vin, String fault) {
        Customer c = resolve(customerId);
        String claimId = "CLM-" + (Math.abs(customerId.hashCode()) % 90000 + 10000);
        return String.format("""
            Warranty service claim opened successfully.
            Claim ID   : %s
            Customer   : %s (%s)
            Vehicle    : %s
            VIN        : %s
            Fault      : %s
            Status     : Pending technician review
            Next step  : Bring the vehicle to our service centre and quote claim ID %s.
            Response time: within 1 business day.
            """, claimId, c.name(), c.id(), c.vehicle(),
            (vin != null && !vin.isBlank()) ? vin : c.vin(),
            fault, claimId);
    }

    public static String openRefundRequest(String customerId, String chargeId, String reason) {
        Customer c = resolve(customerId);
        String caseId = "REF-" + (Math.abs(customerId.hashCode()) % 90000 + 10000);
        return String.format("""
            Refund request opened successfully.
            Case ID    : %s
            Customer   : %s (%s)
            Charge ID  : %s
            Reason     : %s
            Status     : Under review
            Processing : 5–10 business days
            A confirmation will be sent to %s.
            """, caseId, c.name(), c.id(), chargeId, reason, c.email());
    }

    // -----------------------------------------------------------------------
    // Dispatcher
    // -----------------------------------------------------------------------


    /** Null-safe text extraction from a JsonNode field. */
    private static String getText(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.path(field);
        return (value == null || value.isMissingNode() || value.isNull())
            ? defaultValue : value.asText();
    }

    public static String dispatch(String toolName, JsonNode input) {
        return switch (toolName) {
            case "get_financing_plan" ->
                getFinancingPlan(getText(input, "customer_id", "CUST-101"));
            case "get_payment_history" ->
                getPaymentHistory(getText(input, "customer_id", "CUST-101"));
            case "get_billing_statement" ->
                getBillingStatement(getText(input, "customer_id", "CUST-101"));
            case "get_warranty_info" ->
                getWarrantyInfo(
                    getText(input, "customer_id", "CUST-101"),
                    getText(input, "vin", "")
                );
            case "open_service_claim" ->
                openServiceClaim(
                    getText(input, "customer_id", "CUST-101"),
                    getText(input, "vin", ""),
                    getText(input, "fault_description", "not provided")
                );
            case "open_refund_request" ->
                openRefundRequest(
                    getText(input, "customer_id", "CUST-101"),
                    getText(input, "charge_id", "unknown"),
                    getText(input, "reason", "not provided")
                );
            default -> "Unknown tool: " + toolName;
        };
    }
}
