package com.demo.upimesh.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MeshSimulationService {
    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final List<Packet> packets = new ArrayList<>();
    private final List<Event> events = new ArrayList<>();
    private final AtomicLong packetIds = new AtomicLong();
    private double communicationRange = 180;
    private double packetLossPercent = 10;
    private int maxRetries = 2;
    private int packetTtlSeconds = 120;
    private Packet lastAcceptedPacket;
    private long successful, rejected, duplicate, forwarded, retries;

    public MeshSimulationService() { reset(); }

    public synchronized Map<String, Object> snapshot() {
        return Map.of(
            "nodes", nodes.values().stream().map(Node::view).toList(),
            "packets", packets.stream().map(Packet::view).toList(),
            "events", events.stream().limit(100).map(Event::view).toList(),
            "settings", Map.of("communicationRange", communicationRange,
                "packetLossPercent", packetLossPercent, "maxRetries", maxRetries,
                "packetTtlSeconds", packetTtlSeconds),
            "metrics", Map.of("successful", successful, "rejected", rejected,
                "duplicates", duplicate, "forwarded", forwarded, "retries", retries,
                "totalPackets", packets.size())
        );
    }

    public synchronized Map<String, Object> reset() {
        nodes.clear(); packets.clear(); events.clear(); lastAcceptedPacket = null;
        successful = rejected = duplicate = forwarded = retries = 0;
        packetIds.set(0);
        nodes.put("Alice", new Node("Alice", 60, 100, "A"));
        nodes.put("Bob", new Node("Bob", 220, 100, "A"));
        nodes.put("Charlie", new Node("Charlie", 380, 100, "A"));
        nodes.put("Dave", new Node("Dave", 540, 100, "A"));
        event("SYSTEM", "Simulation reset. Four virtual devices are online.");
        return snapshot();
    }

    public synchronized Map<String, Object> configure(Map<String, Object> input) {
        if (input.containsKey("communicationRange"))
            communicationRange = clamp(number(input.get("communicationRange")), 40, 600);
        if (input.containsKey("packetLossPercent"))
            packetLossPercent = clamp(number(input.get("packetLossPercent")), 0, 100);
        if (input.containsKey("maxRetries"))
            maxRetries = (int) clamp(number(input.get("maxRetries")), 0, 10);
        if (input.containsKey("packetTtlSeconds"))
            packetTtlSeconds = (int) clamp(number(input.get("packetTtlSeconds")), 5, 3600);
        event("CONFIG", "Network settings updated.");
        return snapshot();
    }

    public synchronized Map<String, Object> setPartition(boolean enabled) {
        String group = enabled ? "B" : "A";
        nodes.get("Charlie").group = group;
        nodes.get("Dave").group = group;
        event("NETWORK", enabled
            ? "Network partition created: Alice/Bob are isolated from Charlie/Dave."
            : "Network partition healed: all devices are in the same network.");
        return snapshot();
    }

    public synchronized Map<String, Object> send(String from, String to, double amount) {
        if (!nodes.containsKey(from) || !nodes.containsKey(to) || from.equals(to))
            return result("INVALID", "Choose two different known devices.");
        if (amount <= 0 || amount > 100000)
            return result("REJECTED", "Payment amount must be between 0.01 and 100000.");
        Packet packet = new Packet("PKT-" + packetIds.incrementAndGet(), from, to, amount,
                Instant.now(), packetTtlSeconds, "NORMAL");
        packets.add(0, packet);
        route(packet, false);
        return snapshot();
    }

    public synchronized Map<String, Object> attack(String type) {
        String normalized = type == null ? "" : type.toUpperCase(Locale.ROOT);
        Packet packet;
        switch (normalized) {
            case "REPLAY" -> {
                if (lastAcceptedPacket == null)
                    return result("NOT_READY", "Send one successful payment before trying replay.");
                packet = lastAcceptedPacket.copy("PKT-" + packetIds.incrementAndGet(), "REPLAY");
                packets.add(0, packet);
                duplicate++;
                packet.status = "REJECTED";
                packet.reason = "duplicate_packet";
                event("SECURITY", "Replay attack rejected: packet hash was already processed.");
            }
            case "TAMPER" -> {
                packet = new Packet("PKT-" + packetIds.incrementAndGet(), "Alice", "Bob",
                        250, Instant.now(), packetTtlSeconds, "TAMPER");
                packets.add(0, packet);
                packet.status = "REJECTED"; packet.reason = "ciphertext_integrity_check_failed";
                rejected++;
                event("SECURITY", "Tampered payload rejected: integrity validation failed.");
            }
            case "EXPIRED" -> {
                packet = new Packet("PKT-" + packetIds.incrementAndGet(), "Alice", "Bob",
                        250, Instant.now().minusSeconds(packetTtlSeconds + 30L),
                        packetTtlSeconds, "EXPIRED");
                packets.add(0, packet);
                packet.status = "REJECTED"; packet.reason = "packet_expired";
                rejected++;
                event("SECURITY", "Expired packet rejected by TTL validation.");
            }
            case "INVALID" -> {
                packet = new Packet("PKT-" + packetIds.incrementAndGet(), "Alice", "Bob",
                        -50, Instant.now(), packetTtlSeconds, "INVALID");
                packets.add(0, packet);
                packet.status = "REJECTED"; packet.reason = "invalid_payment_instruction";
                rejected++;
                event("SECURITY", "Invalid payment instruction rejected.");
            }
            default -> { return result("INVALID", "Unknown attack type."); }
        }
        return snapshot();
    }

    private void route(Packet packet, boolean ignored) {
        if (packet.createdAt.plusSeconds(packet.ttlSeconds).isBefore(Instant.now())) {
            reject(packet, "packet_expired"); return;
        }
        List<String> path = findPath(packet.from, packet.to);
        if (path.isEmpty()) {
            packet.status = "QUEUED";
            packet.reason = "no_route_network_partition_or_out_of_range";
            event("NETWORK", packet.id + " queued: no route to destination.");
            return;
        }
        packet.route = path;
        for (int i = 1; i < path.size(); i++) {
            boolean delivered = false;
            for (int attempt = 0; attempt <= maxRetries; attempt++) {
                if (ThreadLocalRandom.current().nextDouble(100) >= packetLossPercent) {
                    delivered = true;
                    if (attempt > 0) {
                        retries += attempt;
                        event("RETRY", packet.id + " reached " + path.get(i)
                                + " after " + attempt + " retry attempt(s).");
                    }
                    break;
                }
                if (attempt < maxRetries) event("RETRY", packet.id + " lost on link "
                        + path.get(i - 1) + " → " + path.get(i) + "; retrying.");
            }
            if (!delivered) {
                packet.status = "QUEUED";
                packet.reason = "packet_lost_retry_limit_reached";
                event("NETWORK", packet.id + " queued after retry limit.");
                return;
            }
            if (i < path.size() - 1) forwarded++;
        }
        packet.status = "SETTLED";
        packet.reason = "delivered_and_settled";
        packet.deliveredAt = Instant.now();
        successful++;
        lastAcceptedPacket = packet.copy(packet.id, packet.kind);
        event("SETTLEMENT", packet.id + " delivered and settled at " + packet.to
                + " via " + String.join(" → ", path) + ".");
    }

    private List<String> findPath(String start, String target) {
        Queue<String> queue = new ArrayDeque<>();
        Map<String, String> previous = new HashMap<>();
        Set<String> visited = new HashSet<>();
        queue.add(start); visited.add(start);
        while (!queue.isEmpty()) {
            String current = queue.remove();
            if (current.equals(target)) break;
            for (Node candidate : nodes.values()) {
                if (visited.contains(candidate.id)) continue;
                Node here = nodes.get(current);
                if (!here.group.equals(candidate.group)) continue;
                if (distance(here, candidate) <= communicationRange) {
                    visited.add(candidate.id); previous.put(candidate.id, current);
                    queue.add(candidate.id);
                }
            }
        }
        if (!visited.contains(target)) return List.of();
        LinkedList<String> path = new LinkedList<>();
        for (String at = target; at != null; at = previous.get(at)) path.addFirst(at);
        return path;
    }

    private double distance(Node a, Node b) {
        return Math.hypot(a.x - b.x, a.y - b.y);
    }

    private void reject(Packet packet, String reason) {
        packet.status = "REJECTED"; packet.reason = reason; rejected++;
        event("SECURITY", packet.id + " rejected: " + reason);
    }

    private Map<String, Object> result(String status, String message) {
        return Map.of("status", status, "message", message, "snapshot", snapshot());
    }
    private void event(String type, String message) {
        events.add(0, new Event(Instant.now(), type, message));
        if (events.size() > 200) events.remove(events.size() - 1);
    }
    private double number(Object value) {
        try { return Double.parseDouble(String.valueOf(value)); }
        catch (Exception e) { return 0; }
    }
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static class Node {
        String id; double x, y; String group;
        Node(String id, double x, double y, String group) {
            this.id=id; this.x=x; this.y=y; this.group=group;
        }
        Map<String,Object> view() {
            return Map.of("id", id, "x", x, "y", y, "group", group, "online", true);
        }
    }
    private static class Packet {
        String id, from, to, kind, status="CREATED", reason="";
        double amount; Instant createdAt, deliveredAt; int ttlSeconds;
        List<String> route = new ArrayList<>();
        Packet(String id, String from, String to, double amount, Instant createdAt,
               int ttlSeconds, String kind) {
            this.id=id; this.from=from; this.to=to; this.amount=amount;
            this.createdAt=createdAt; this.ttlSeconds=ttlSeconds; this.kind=kind;
        }
        Packet copy(String newId, String newKind) {
            Packet p = new Packet(newId, from, to, amount, createdAt, ttlSeconds, newKind);
            p.status=status; p.reason=reason; p.route=new ArrayList<>(route); return p;
        }
        Map<String,Object> view() {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("id",id); m.put("from",from); m.put("to",to); m.put("amount",amount);
            m.put("kind",kind); m.put("status",status); m.put("reason",reason);
            m.put("createdAt",createdAt.toString()); m.put("deliveredAt", deliveredAt == null ? null : deliveredAt.toString());
            m.put("route",route); return m;
        }
    }
    private record Event(Instant at, String type, String message) {
        Map<String,Object> view() { return Map.of("at",at.toString(),"type",type,"message",message); }
    }
}
