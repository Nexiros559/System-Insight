package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MetricSerializationTest {

    // Jackson est l'outil standard pour convertir Objet <-> JSON
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldSerializeSystemMetricToJson() throws Exception {
        // 1. GIVEN - On prépare des données contrôlées (pas de random ici !)
        // On utilise les constructeurs complets pour maîtriser les valeurs
        long fixedTimestamp = 1700000000000L;

        CpuMetric cpu = new CpuMetric(65.5, 0.45, fixedTimestamp, false, false);
        RamMetric ram = new RamMetric(16000000000L, 8000000000L, 8000000000L); // 16Go total, 8Go used
        DiskMetric disk = new DiskMetric(512000000000L, 100000000000L, 412000000000L); // 512Go total

        SystemMetric metric = new SystemMetric(fixedTimestamp, cpu, disk, ram);

        // 2. WHEN - On transforme en texte (JSON)
        String jsonResult = mapper.writeValueAsString(metric);

        // 3. THEN - On vérifie que les clés critiques pour le frontend sont là
        // On ne vérifie pas tout le string exact (car l'ordre des champs peut varier),
        // mais on vérifie la présence des données vitales.

        // Vérif Structure
        assertTrue(jsonResult.contains("\"cpu\":"), "Le JSON doit contenir l'objet 'cpu'");
        assertTrue(jsonResult.contains("\"ram\":"), "Le JSON doit contenir l'objet 'ram'");

        // Vérif Valeurs CPU
        assertTrue(jsonResult.contains("65.5"), "La température doit être présente");
        assertTrue(jsonResult.contains("0.45"), "La charge CPU doit être présente");

        // Vérif Valeurs RAM (Jackson convertit les long en nombres JSON)
        assertTrue(jsonResult.contains("16000000000"), "Le total RAM doit être présent");
    }

    @Test
    void shouldDeserializeJsonToSystemMetric() throws Exception {
        // 1. GIVEN - Un JSON brut qui simule ce que Kafka reçoit
        String jsonInput = """
            {
                "timestamp": 1700000000000,
                "cpu": {
                    "temperature": 82.0,
                    "cpuLoad": 0.99,
                    "timestamp": 1700000000000,
                    "isOverheating": true,
                    "isOverloaded": true
                },
                "ram": {
                    "total": 8000,
                    "used": 4000,
                    "available": 4000
                },
                "disk": {
                    "totalSpace": 500000,
                    "usedSpace": 250000,
                    "freeSpace": 250000
                }
            }
        """;

        // 2. WHEN - On demande à Jackson de le transformer en Objet Java
        SystemMetric result = mapper.readValue(jsonInput, SystemMetric.class);

        // 3. THEN - On vérifie que Java a bien tout compris
        assertEquals(82.0, result.cpu().temperature(), 0.01);
        assertEquals(0.99, result.cpu().cpuLoad(), 0.01);
        assertTrue(result.cpu().isOverheating());
        assertEquals(4000, result.ram().used());
    }
}