package com.demo.upimesh.controller;

import com.demo.upimesh.service.MeshSimulationService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class MeshSimulationController {
    private final MeshSimulationService simulation;

    public MeshSimulationController(MeshSimulationService simulation) {
        this.simulation = simulation;
    }

    @GetMapping("/mesh-simulator")
    public String page() { return "mesh-simulator"; }

    @RestController
    @RequestMapping("/api/simulation")
    public static class Api {
        private final MeshSimulationService simulation;
        public Api(MeshSimulationService simulation) { this.simulation = simulation; }

        @GetMapping("/state")
        public Map<String,Object> state() { return simulation.snapshot(); }

        @PostMapping("/reset")
        public Map<String,Object> reset() { return simulation.reset(); }

        @PostMapping("/configure")
        public Map<String,Object> configure(@RequestBody Map<String,Object> body) {
            return simulation.configure(body);
        }

        @PostMapping("/partition")
        public Map<String,Object> partition(@RequestBody Map<String,Object> body) {
            return simulation.setPartition(Boolean.parseBoolean(String.valueOf(body.get("enabled"))));
        }

        @PostMapping("/send")
        public Map<String,Object> send(@RequestBody Map<String,Object> body) {
            return simulation.send(
                String.valueOf(body.getOrDefault("from", "Alice")),
                String.valueOf(body.getOrDefault("to", "Bob")),
                Double.parseDouble(String.valueOf(body.getOrDefault("amount", "100")))
            );
        }

        @PostMapping("/attack")
        public Map<String,Object> attack(@RequestBody Map<String,Object> body) {
            return simulation.attack(String.valueOf(body.get("type")));
        }
    }
}
