package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Assertions;
import org.example.SystemMetric;

import java.nio.file.Path;
import java.util.List;

public class JsonFIleManagerTest {

    @TempDir
    Path path;  // ✅ NON-STATIC = nouveau dossier pour chaque test

    Path completePath;
    String folder;
    JsonFileManager fileManagerTest;

    @BeforeEach
    void setUp(){
        completePath = path.resolve("MetricsTest.json");
        folder = completePath.toString();
        fileManagerTest = new JsonFileManager(folder);
    }

    @Test
    public void shouldBeEmptyAtStart(){
        List<SystemMetric> allMetrics = fileManagerTest.readAllMetrics();
        Assertions.assertTrue(allMetrics.isEmpty());
    }

    @Test
    public void shouldSaveAndReadOneMetric() {
        CpuMetric cpu = new CpuMetric(50.0, 0.4);
        RamMetric ram = new RamMetric(1000L, 500L);
        DiskMetric disk = new DiskMetric(10000L, 2000L);
        SystemMetric metric = new SystemMetric(System.currentTimeMillis(), cpu, disk, ram);

        this.fileManagerTest.saveMetric(metric);

        List<SystemMetric> allMetrics = fileManagerTest.readAllMetrics();
        Assertions.assertEquals(1, allMetrics.size());
    }

    @Test
    public void shouldSaveAndReadMultipleMetrics() {
        CpuMetric cpu1 = new CpuMetric(50.0, 0.4);
        RamMetric ram1 = new RamMetric(1000L, 500L);
        DiskMetric disk1 = new DiskMetric(10000L, 2000L);
        SystemMetric m1 = new SystemMetric(System.currentTimeMillis(), cpu1, disk1, ram1);
        this.fileManagerTest.saveMetric(m1);

        CpuMetric cpu2 = new CpuMetric(60.0, 0.6);
        RamMetric ram2 = new RamMetric(1000L, 200L);
        DiskMetric disk2 = new DiskMetric(10000L, 1000L);
        SystemMetric m2 = new SystemMetric(System.currentTimeMillis(), cpu2, disk2, ram2);
        this.fileManagerTest.saveMetric(m2);

        List<SystemMetric> allMetrics = fileManagerTest.readAllMetrics();
        Assertions.assertEquals(2, allMetrics.size());
    }
}

