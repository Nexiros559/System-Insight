package org.example;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.write.Point;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ConsumerJavaTest {

    @Test
    void shouldConvertAndWriteMetricToInflux() {
        // --- 1. PRÉPARATION DES MOCKS (Le décor de cinéma) ---

        // On crée des faux pour InfluxDB
        InfluxDBClient mockInfluxClient = mock(InfluxDBClient.class);
        WriteApiBlocking mockWriteApi = mock(WriteApiBlocking.class);

        // On dit au faux client Influx : "Si on te demande l'API d'écriture, donne ce faux API"
        when(mockInfluxClient.getWriteApiBlocking()).thenReturn(mockWriteApi);

        // --- 2. LA MAGIE (Try-with-resources) ---
        // On ouvre une parenthèse temporelle où les "new" et les "static" sont détournés
        try (
                // A. Détourne le 'new KafkaConsumer'
                MockedConstruction<KafkaConsumer> mockedKafka = Mockito.mockConstruction(KafkaConsumer.class);

                // B. Détourne le 'InfluxDBClientFactory.create' (Statique)
                MockedStatic<InfluxDBClientFactory> mockedFactory = Mockito.mockStatic(InfluxDBClientFactory.class)
        ) {
            // On configure le détournement statique pour qu'il renvoie notre faux client
            mockedFactory.when(() -> InfluxDBClientFactory.create(anyString(), any(char[].class), anyString(), anyString()))
                    .thenReturn(mockInfluxClient);

            // --- 3. EXÉCUTION (Instanciation de TA classe) ---
            // C'est ici que ton constructeur s'exécute. 
            // Grâce aux mocks ci-dessus, il ne va PAS planter même sans Kafka réel.
            ConsumerJava myConsumer = new ConsumerJava();

            // On prépare une donnée de test (Record)
            long now = System.currentTimeMillis();
            CpuMetric cpu = new CpuMetric(55.0, 0.45, now, false, false);
            RamMetric ram = new RamMetric(16000L, 8000L, 8000L);
            DiskMetric disk = new DiskMetric(1000L, 500L, 500L);
            SystemMetric metric = new SystemMetric(now, cpu, disk, ram);

            // --- 4. APPEL DE LA MÉTHODE À TESTER ---
            // On teste directement la logique d'écriture (évite de lancer start() qui fait une boucle infinie)
            myConsumer.writeToInfluxDB(metric);

            // --- 5. VÉRIFICATION (Assert) ---
            // On vérifie que writeApi.writePoints a été appelé avec une liste contenant nos points
            verify(mockWriteApi).writePoints(argThat(points -> {
                // On vérifie qu'on a bien 3 points (CPU, RAM, DISK)
                if (((List) points).size() != 3) return false;

                // On vérifie un point au hasard, par exemple le CPU
                Point cpuPoint = (Point) ((List) points).get(0);
                // Le toLineProtocol() génère la ligne de texte pour Influx, on vérifie qu'elle contient la température
                return cpuPoint.toLineProtocol().contains("temperature=55.0");
            }));
        }
    }
}