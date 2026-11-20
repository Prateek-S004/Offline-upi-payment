# UPI Offline Mesh — Secure Offline Payment Simulation

A Spring Boot application demonstrating **offline payment instructions routed through a Bluetooth-style device mesh network**, with secure encryption, network simulation, duplicate prevention, and deferred settlement.

Imagine you're in a basement with no internet connectivity. You want to send your friend ₹500. Your phone creates an encrypted payment instruction and broadcasts it to nearby phones. The packet travels from device to device until it reaches a device with internet connectivity. That device uploads the packet to the backend, where it is decrypted, validated, and settled.

This repository implements the **backend payment-processing system and a software-based mesh network simulator**, allowing the complete workflow to be demonstrated on a single computer without physical Bluetooth hardware.

It also includes an interactive **Mesh Network Lab** with network controls, live topology visualization, performance metrics, and security attack simulations.

---

# Table of Contents

1. [What This Project Demonstrates](#1-what-this-project-demonstrates)
2. [Features](#2-features)
3. [How to Run](#3-how-to-run)
4. [Dashboard Overview](#4-dashboard-overview)
5. [Complete Demo Workflow](#5-complete-demo-workflow)
6. [Network Simulation](#6-network-simulation)
7. [Security Attack Simulator](#7-security-attack-simulator)
8. [Architecture](#8-architecture)
9. [Security and Payment Processing](#9-security-and-payment-processing)
10. [API Reference](#10-api-reference)
11. [Project Structure](#11-project-structure)
12. [Testing](#12-testing)
13. [Limitations and Future Production Improvements](#13-limitations-and-future-production-improvements)

---

# 1. What This Project Demonstrates

The project demonstrates four important capabilities.

### 1.1 Offline packet propagation

A payment instruction can be created without requiring the simulated sender device to have internet connectivity.

The encrypted packet propagates through nearby virtual devices using a gossip-based mesh simulation.

### 1.2 Secure communication through untrusted devices

Intermediate devices carry encrypted payment packets without needing access to the original payment information.

Hybrid encryption using RSA-OAEP and AES-256-GCM protects confidentiality and detects unauthorized modifications.

### 1.3 Duplicate payment prevention

The same payment packet can reach the backend multiple times through repeated delivery attempts.

The backend uses ciphertext hashing, atomic idempotency checks, and a database uniqueness constraint to prevent duplicate settlement.

### 1.4 Network failure and security simulation

The Mesh Lab demonstrates different network conditions and security scenarios:

- Limited device communication range.
- Network partition and reconnection.
- Packet loss and retransmission.
- Ciphertext tampering.
- Replay attacks.
- Expired payment instructions.
- Invalid payment amounts.

These features make it possible to demonstrate not only the successful payment flow but also how the system behaves when the network or payment instructions are compromised.

---

# 2. Features

## Payment Processing

- Create encrypted payment instructions.
- Simulate offline payment creation.
- Route packets through virtual devices.
- Upload packets through an internet-enabled bridge.
- Decrypt and validate received instructions.
- Perform transactional debit and credit.
- Maintain a transaction ledger.
- Prevent duplicate payment processing.

## Mesh Network Simulation

- Five virtual devices with different network positions.
- One internet-enabled bridge device.
- Device-to-device communication range.
- One-hop gossip-based packet propagation.
- Configurable packet loss.
- Configurable retransmission attempts.
- Network partition and recovery.
- Packet TTL management.
- Live network topology visualization.

## Security Features

- RSA-OAEP and AES-256-GCM hybrid encryption.
- Authentication-tag verification.
- SHA-256 ciphertext hashing.
- Atomic idempotency checks.
- Duplicate packet detection.
- Timestamp-based freshness validation.
- Invalid payment amount validation.
- Persistent RSA server keypair through a PKCS12 keystore.

## Interactive Security Attack Simulator

Four attack scenarios can be executed from the Mesh Lab:

| Attack | What it demonstrates |
|---|---|
| Ciphertext Tampering | Detecting modifications to encrypted packets |
| Replay Attack | Preventing repeated processing of the same payment |
| Expired Packet | Rejecting stale payment instructions |
| Invalid Amount | Rejecting invalid payment values during settlement |

Each scenario includes:

- Attacker's objective.
- Step-by-step attack execution.
- Explanation of what happens to the packet.
- Identification of the security mechanism involved.
- Actual backend response.

## Monitoring and Visualization

- Live network topology.
- Device connectivity visualization.
- Account balances.
- Transaction history.
- Network event logs.
- Mesh transfer metrics.
- Retry and packet-drop metrics.
- Idempotency cache information.

---

# 3. How to Run

## Prerequisites

- JDK 17 or newer.
- Maven or the included Maven wrapper.
- Internet connectivity for the first dependency download.

The application uses an H2 in-memory database, so a separate database installation is not required.

## Run on Windows

Open a terminal in the project directory:

```cmd
mvnw.cmd spring-boot:run
```

If using PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

## Run on Linux or macOS

```bash
./mvnw spring-boot:run
```

## Open the dashboards

After Spring Boot starts successfully:

**Original Dashboard**

http://localhost:8080/

**UPI Mesh Network Lab**

http://localhost:8080/mesh-lab

The original dashboard remains available. The Mesh Lab provides additional network simulation and security demonstration capabilities.

## Stop the application

Press:

```text
Ctrl + C
```

in the terminal.

---

# 4. Dashboard Overview

The project has two dashboard interfaces.

## 4.1 Original Dashboard

The original dashboard provides the basic payment workflow.

It is used to:

- Select sender and receiver accounts.
- Enter payment details.
- Create encrypted payment packets.
- Inject packets into the mesh.
- Run gossip rounds.
- Flush packets through bridge devices.
- Observe balances and transaction history.

## 4.2 Mesh Network Lab

The Mesh Lab is an extended interactive environment for demonstrating network behavior and security.

It contains the following sections.

### A. Live Network Topology

Displays the five virtual devices and their connections.

The topology changes when communication range or network partition settings are modified.

### B. Network Controls

The following settings can be changed:

| Setting | Description |
|---|---|
| Communication Range | Controls which devices can communicate directly |
| Packet Loss | Probability of a transmission being dropped |
| Max Retries | Maximum simulated retransmission attempts |
| Network Partition | Separates devices into disconnected groups |

The user can also manually:

- Run one gossip round.
- Upload available packets to the bridge.
- Create or heal a network partition.

### C. Payment Injection

Allows the user to:

- Select a sender.
- Select a receiver.
- Enter a payment amount.
- Enter the sender PIN.
- Select the device where the packet should initially be injected.

The packet is encrypted and inserted into the simulated mesh.

### D. Account Balances

Displays the balances of the demo accounts.

After a successful settlement, the sender's balance decreases and the receiver's balance increases.

### E. Transaction History

Displays recent transactions with information such as:

- Transaction ID.
- Sender VPA.
- Receiver VPA.
- Amount.
- Transaction status.
- Rejection reason.
- Bridge node.
- Hop count.

### F. Network Event Stream

Displays events generated during mesh simulation, including packet transfers and network-related activity.

### G. Security Attack Simulator

Allows the user to execute four security attack demonstrations and understand how the backend handles each scenario.

---

# 5. Complete Demo Workflow

The following sequence demonstrates the complete payment lifecycle.

## Step 1 — Create a Payment

Open the Mesh Lab.

Select:

- Sender: Alice.
- Receiver: Bob.
- Amount: ₹500.
- PIN: 1111.
- Device: phone-alice.

Click **Encrypt & Inject into Mesh**.

The backend:

1. Creates a PaymentInstruction.
2. Generates a unique nonce.
3. Adds a timestamp.
4. Encrypts the payment instruction.
5. Wraps the encrypted data inside a MeshPacket.
6. Assigns the packet a TTL.
7. Injects the packet into the selected virtual device.

The sender's device now holds the encrypted packet.

## Step 2 — Run Gossip Rounds

Click **Run One Gossip Round**.

The MeshSimulatorService checks which devices can communicate with each other.

Packets are propagated through reachable one-hop connections.

Each forwarding operation decreases the packet TTL.

Repeat gossip rounds to allow packets to move through the network.

## Step 3 — Upload Through the Bridge

The virtual bridge device is the only default device with internet connectivity.

Click **Bridge Uploads to Real Backend**.

The bridge uploads packets to:

```text
/api/bridge/ingest
```

The backend:

1. Calculates the SHA-256 hash of the ciphertext.
2. Checks whether the packet has already been processed.
3. Attempts to claim the packet hash in the idempotency cache.
4. Decrypts the packet.
5. Verifies timestamp freshness.
6. Passes the instruction to the settlement service.
7. Performs the debit and credit transactionally.

## Step 4 — Verify Settlement

Check the account balances.

For a successful ₹500 payment:

- Alice's balance decreases by ₹500.
- Bob's balance increases by ₹500.

A new transaction also appears in the transaction history.

## Step 5 — Demonstrate Network Failure

Enable the network partition.

Create another payment and run gossip rounds.

Observe how disconnected groups prevent packets from crossing the partition.

Click **Heal Partition**.

Run gossip rounds again and observe packet propagation after connectivity is restored.

## Step 6 — Demonstrate Security Attacks

Open the Security Attack Simulator.

Run all four scenarios:

1. Ciphertext Tampering.
2. Replay Attack.
3. Expired Packet.
4. Invalid Amount.

The simulator displays the attack stages and the actual backend response.

---

# 6. Network Simulation

The mesh simulation models device-to-device communication without using physical Bluetooth or Wi-Fi Direct hardware.

## 6.1 Virtual Devices

The default simulation contains five devices:

| Device | Type | Internet |
|---|---|---|
| phone-alice | User device | No |
| phone-stranger1 | Intermediate device | No |
| phone-stranger2 | Intermediate device | No |
| phone-stranger3 | Intermediate device | No |
| phone-bridge | Bridge device | Yes |

Each device has a position in the simulated topology.

The MeshSimulatorService calculates which devices are within communication range.

## 6.2 Communication Range

The communication range determines whether two devices can exchange packets directly.

Reducing the range can disconnect devices.

Increasing the range can create additional communication links.

This allows different network topologies to be demonstrated without changing the Java implementation.

## 6.3 Gossip-Based Propagation

The simulator uses one-hop gossip propagation.

A device holding a packet attempts to forward it to reachable neighboring devices.

A packet can move through multiple devices over multiple gossip rounds.

The packet TTL decreases during forwarding to prevent unlimited propagation.

## 6.4 Network Partition

Network partition divides the default devices into two groups:

- Group A: phone-alice, phone-stranger1, phone-stranger2.
- Group B: phone-stranger3, phone-bridge.

Communication across the partition is disabled.

When the partition is healed, normal range-based communication resumes.

This demonstrates how packets can remain queued during network isolation and propagate after connectivity returns.

## 6.5 Packet Loss and Retries

The simulator supports configurable packet loss.

For example:

| Packet Loss | Expected Behavior |
|---|---|
| 0% | No simulated packet loss |
| 30% | Some transmissions may fail |
| 50% | More retransmissions may be required |
| 80% | Delivery becomes less reliable |

The maximum retry setting controls how many retransmission attempts are permitted.

Because packet loss is probabilistic, repeated runs may produce different results.

The dashboard also displays retry and dropped-transmission metrics.

---

# 7. Security Attack Simulator

The Security Attack Simulator demonstrates how different attacks occur and how the backend responds.

These are executable software simulations, not merely static illustrations.

The simulator sends requests to the actual Spring Boot attack endpoints, which use the existing encryption and payment-ingestion services.

## 7.1 Ciphertext Tampering Attack

### Attacker's Objective

Modify an encrypted payment packet while it is travelling through the mesh network.

### Attack Execution

1. A legitimate payment instruction is created.
2. The instruction is encrypted using AES-256-GCM.
3. An attacker obtains a copy of the encrypted packet.
4. The simulator modifies one bit in the final byte of the encoded packet.
5. The modified packet reaches the bridge.
6. The bridge attempts decryption and authentication.

### How the System Prevents It

AES-GCM provides authenticated encryption.

The authentication tag is verified during decryption.

If the encrypted packet or authentication tag is modified, verification fails.

The packet is rejected instead of being processed as a valid payment.

### What the Demo Shows

- Legitimate payment creation.
- Attacker interception.
- Packet modification.
- Bridge verification.
- Actual backend response.

**Important:** The current tampering simulation modifies the final byte, which belongs to the GCM authentication tag in the project's encoded packet format. It demonstrates authentication failure rather than a successful alteration of the readable payment amount.

---

## 7.2 Replay Attack

### Attacker's Objective

Capture a valid payment packet and submit the same packet repeatedly.

The goal is to make the receiver receive multiple credits for one payment instruction.

### Attack Execution

1. Alice creates a legitimate payment for Bob.
2. The encrypted packet reaches the bridge.
3. The bridge processes the first delivery.
4. An attacker resends the exact same packet.
5. The bridge receives the packet again.
6. The backend checks its ciphertext hash.

### How the System Prevents It

The backend uses SHA-256 hashing and idempotency checks.

The first valid delivery can proceed to settlement.

When the same packet arrives again, the backend identifies it as a duplicate.

The duplicate is rejected instead of being settled again.

### What the Demo Shows

- First packet delivery.
- First settlement response.
- Attacker resending the packet.
- Duplicate detection.
- Second delivery response.

---

## 7.3 Expired Packet Attack

### Attacker's Objective

Submit an old payment instruction after its permitted validity period.

### Attack Execution

1. The simulator creates an encrypted instruction with an old timestamp.
2. The packet is submitted to the bridge.
3. The bridge decrypts the instruction.
4. The backend reads the signedAt timestamp.
5. The timestamp is compared with the configured freshness window.

### How the System Prevents It

The BridgeIngestionService checks whether the instruction timestamp is within the permitted maximum age.

An expired instruction is rejected before settlement.

### What the Demo Shows

- Creation of an old instruction.
- Submission to the bridge.
- Timestamp extraction.
- Freshness validation.
- Rejection response.

---

## 7.4 Invalid Amount Attack

### Attacker's Objective

Attempt to create a payment using an invalid negative amount.

### Attack Execution

1. The simulator creates a payment instruction containing -₹1.
2. The instruction is encrypted normally.
3. The bridge receives the encrypted packet.
4. The bridge decrypts the instruction successfully.
5. The settlement service validates the payment amount.

### How the System Prevents It

The SettlementService checks the amount and rejects values less than or equal to zero.

It also performs other payment validations, including:

- Insufficient balance.
- Maximum transaction amount.
- Daily transaction limit.

### What the Demo Shows

- Invalid payment construction.
- Encryption.
- Successful decryption.
- Settlement validation.
- Rejection response.

This scenario demonstrates an important security distinction:

**Encryption protects payment data, but business validation determines whether a payment instruction is valid.**

---

# 8. Architecture

The project follows a layered Spring Boot architecture.

```text
                  SENDER DEVICE
                       |
                       |
              PaymentInstruction
                       |
                       v
                Hybrid Encryption
                 RSA-OAEP + AES-GCM
                       |
                       v
                   MeshPacket
                       |
                       v
              MeshSimulatorService
                       |
             One-hop Gossip Protocol
                       |
          +------------+------------+
          |            |            |
          v            v            v
       Device 1     Device 2      Device 3
          |            |            |
          +------------+------------+
                       |
                       v
                 Bridge Device
                       |
                       | Internet available
                       v
              /api/bridge/ingest
                       |
                       v
             BridgeIngestionService
                       |
                       v
             Ciphertext SHA-256 Hash
                       |
                       v
              Duplicate Detection
                       |
                       v
             HybridCryptoService
                       |
                       v
             Freshness Validation
                       |
                       v
               SettlementService
                       |
                       v
              Transactional Database
                       |
              +--------+--------+
              |                 |
              v                 v
         Debit Sender      Credit Receiver
                       |
                       v
                Transaction Ledger
```

## Main Components

### Model Layer

Contains the domain objects:

- Account.
- Transaction.
- MeshPacket.
- PaymentInstruction.
- VirtualDevice.

### Cryptography Layer

Responsible for:

- RSA key management.
- Hybrid encryption.
- AES-GCM authentication.
- Ciphertext hashing.

### Service Layer

Contains:

- DemoService.
- MeshSimulatorService.
- BridgeIngestionService.
- IdempotencyService.
- SettlementService.

### Controller Layer

Exposes REST APIs and dashboard routes.

The MeshLabController provides the additional interactive network and security simulation endpoints.

---

# 9. Security and Payment Processing

## 9.1 Hybrid Encryption

The project uses a hybrid encryption scheme.

RSA is used to encrypt the AES session key, while AES-GCM encrypts the actual payment instruction.

The process is:

1. Generate a fresh AES-256 session key.
2. Generate a 12-byte initialization vector.
3. Encrypt the payment instruction using AES-GCM.
4. Encrypt the AES session key using RSA-OAEP.
5. Combine the encrypted session key, IV, and encrypted payload.

The resulting packet contains:

```text
[ RSA-encrypted AES key ]
[ 12-byte GCM IV ]
[ AES-GCM ciphertext + authentication tag ]
```

Intermediate devices only need to forward the encrypted packet.

The server uses its RSA private key to recover the AES key and decrypt the payment instruction.

## 9.2 Persistent Server Key Management

The server RSA keypair is loaded from a PKCS12 keystore.

This avoids generating a completely new keypair every time the application restarts.

The keystore location and password are configured through application properties and environment variables.

The private key must remain protected and must never be distributed to intermediate devices.

## 9.3 Idempotency

The system calculates a SHA-256 hash of the encrypted packet.

The IdempotencyService uses ConcurrentHashMap atomic operations to claim packet hashes.

This prevents multiple threads in the same application instance from processing the same packet concurrently.

The BridgeIngestionService also checks the transaction database for previously processed packet hashes.

The Transaction entity has a unique packet-hash constraint as an additional protection.

The in-memory cache is application-instance-local. A distributed production deployment would require a shared idempotency store such as Redis.

## 9.4 Freshness Validation

The payment instruction contains a signedAt timestamp.

The backend verifies that the timestamp falls within the configured freshness window.

This prevents old payment instructions from being accepted indefinitely.

## 9.5 Transactional Settlement

The SettlementService performs account debit, account credit, and transaction recording inside a database transaction.

The Account entity uses optimistic locking through `@Version`.

This helps prevent conflicting concurrent account updates.

The settlement layer also checks payment limits and account balances.

---

# 10. API Reference

## Original Dashboard and Payment APIs

| Method | Endpoint | Description |
|---|---|---|
| GET | `/` | Original dashboard |
| GET | `/api/server-key` | Server RSA public key |
| GET | `/api/accounts` | Account balances |
| GET | `/api/transactions` | Recent transactions |
| GET | `/api/mesh/state` | Virtual device and mesh state |
| POST | `/api/demo/send` | Create and inject an encrypted payment |
| POST | `/api/mesh/gossip` | Execute one gossip round |
| POST | `/api/mesh/flush` | Upload packets held by bridge devices |
| POST | `/api/mesh/reset` | Reset mesh queues and idempotency cache |
| POST | `/api/bridge/ingest` | Bridge packet ingestion endpoint |

## Mesh Lab APIs

| Method | Endpoint | Description |
|---|---|---|
| GET | `/mesh-lab` | Interactive Mesh Lab dashboard |
| GET | `/api/simulator/network` | Network topology, settings and metrics |
| POST | `/api/simulator/network/configure` | Configure range, loss and retries |
| POST | `/api/simulator/network/partition` | Create or heal a network partition |

## Security Attack APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/simulator/attack/tamper` | Execute ciphertext tampering scenario |
| POST | `/api/simulator/attack/replay` | Execute replay scenario |
| POST | `/api/simulator/attack/expired` | Execute expired packet scenario |
| POST | `/api/simulator/attack/invalid` | Execute invalid amount scenario |

The attack endpoints create encrypted payment instructions and submit them to the actual bridge ingestion logic.

The frontend provides the step-by-step explanation and displays the backend response.

---

# 11. Project Structure

```text
upi-offline-mesh/
│
├── pom.xml
├── mvnw
├── mvnw.cmd
├── README.md
│
└── src/
    │
    ├── main/
    │   │
    │   ├── java/com/demo/upimesh/
    │   │   │
    │   │   ├── UpiMeshApplication.java
    │   │   │
    │   │   ├── model/
    │   │   │   ├── Account.java
    │   │   │   ├── AccountRepository.java
    │   │   │   ├── Transaction.java
    │   │   │   ├── TransactionRepository.java
    │   │   │   ├── MeshPacket.java
    │   │   │   ├── PaymentInstruction.java
    │   │   │   └── VirtualDevice.java
    │   │   │
    │   │   ├── crypto/
    │   │   │   ├── ServerKeyHolder.java
    │   │   │   └── HybridCryptoService.java
    │   │   │
    │   │   ├── service/
    │   │   │   ├── DemoService.java
    │   │   │   ├── MeshSimulatorService.java
    │   │   │   ├── BridgeIngestionService.java
    │   │   │   ├── IdempotencyService.java
    │   │   │   └── SettlementService.java
    │   │   │
    │   │   ├── controller/
    │   │   │   ├── ApiController.java
    │   │   │   ├── DashboardController.java
    │   │   │   └── MeshLabController.java
    │   │   │
    │   │   └── config/
    │   │       └── AppConfig.java
    │   │
    │   └── resources/
    │       ├── application.properties
    │       ├── keystore/
    │       │   └── upi-server.p12
    │       └── templates/
    │           ├── dashboard.html
    │           └── mesh-lab.html
    │
    └── test/
        └── java/com/demo/upimesh/
            └── IdempotencyConcurrencyTest.java
```

The exact test files may vary depending on the latest project version.

---

# 12. Testing

Run the complete test suite:

### Windows

```cmd
mvnw.cmd test
```

### Linux/macOS

```bash
./mvnw test
```

## Recommended Manual Tests

| Test | Procedure | Expected Result |
|---|---|---|
| Normal payment | Create payment, gossip and flush | Payment settles |
| Communication range | Change range slider | Topology changes |
| Network partition | Enable partition | Cross-group links disappear |
| Network recovery | Heal partition | Connectivity returns |
| Packet loss | Increase packet loss | More transmissions may fail |
| Retries | Increase retry count | Additional retry attempts are allowed |
| Ciphertext tampering | Run tamper attack | Authentication/decryption fails |
| Replay | Run replay attack | Second delivery is detected as duplicate |
| Expired packet | Run expired attack | Old instruction is rejected |
| Invalid amount | Run invalid amount attack | Settlement rejects negative amount |

## Concurrency Test

The idempotency concurrency test is particularly important.

It verifies that multiple threads delivering the same packet simultaneously do not cause multiple successful settlements.

Run:

```cmd
mvnw.cmd test -Dtest=IdempotencyConcurrencyTest
```

The expected result is that one delivery succeeds and the others are identified as duplicates.

---

# 13. Limitations and Future Production Improvements

This project is a software simulation and backend demonstration. It is not a real offline UPI implementation.

The distinction is important.

| Current Implementation | Production Requirement |
|---|---|
| H2 in-memory database | PostgreSQL or MySQL |
| ConcurrentHashMap idempotency | Distributed Redis-based idempotency |
| PKCS12 server keystore | Hardware-backed key management or HSM |
| Server-side payment creation | Payment creation on Android devices |
| Software mesh simulation | Actual BLE GATT or Wi-Fi Direct |
| One default bridge device | Multiple authenticated bridge devices |
| Basic bridge ingestion | Mutual TLS or signed bridge credentials |
| Demo accounts | Real account and identity verification |
| Simulated payment settlement | Integration with a regulated banking/payment system |
| No production-grade bridge authorization | Bridge authentication and authorization |
| No distributed rate limiting | Per-device and per-user rate limiting |

## Important Conceptual Limitations

### 1. Offline Balance Verification

A receiver cannot independently verify the sender's actual available bank balance while the entire network is offline.

A payment instruction created offline is effectively a deferred payment request until the backend processes it.

If the sender has insufficient funds when settlement occurs, the transaction can be rejected.

### 2. Offline Double Spending

A malicious sender could create multiple payment instructions while disconnected.

For example, a sender with ₹500 might create:

- ₹500 payment to Bob.
- ₹500 payment to Carol.

Both instructions could travel through different network paths.

The backend can reject the second settlement when it processes the instructions, but it cannot guarantee that the receiver will know the payment is valid before reconnection.

### 3. Physical Mesh Networking

The current simulator does not use real Bluetooth communication.

A production implementation would require actual device discovery, peer communication, packet storage, retransmission, and background execution support.

### 4. Privacy and Bridge Trust

Intermediate devices carry encrypted packets, but metadata such as packet size, transmission timing, and device activity may still be observable.

A production implementation would need additional privacy and bridge authentication mechanisms.

---

# Conclusion

UPI Offline Mesh demonstrates how encrypted payment instructions can be propagated through an intermittently connected device network and processed later by a backend.

The project combines:

- Spring Boot backend development.
- Hybrid cryptography.
- Device-to-device mesh simulation.
- Network partition and packet-loss handling.
- Idempotent payment processing.
- Transactional database operations.
- Security attack simulations.
- Interactive network visualization.

The key concept is **mesh-routed deferred settlement**.

Rather than claiming to implement real-time offline UPI, this project demonstrates the engineering challenges involved in transporting and securely processing payment instructions when internet connectivity is unavailable.

The combination of network simulation, payment processing, and executable security demonstrations makes it possible to explore both successful payment delivery and failure scenarios in a controlled environment.
