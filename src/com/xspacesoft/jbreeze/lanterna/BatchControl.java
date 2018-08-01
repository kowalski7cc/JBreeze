package com.xspacesoft.jbreeze.lanterna;

import com.xspacesoft.jbreeze.api.Daikin;
import com.xspacesoft.jbreeze.api.DaikinStatus;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class BatchControl implements AutoCloseable, Closeable {

    private List<Daikin> daikinUnits;
    private ExecutorService executorService;

    public BatchControl(List<Daikin> daikinUnits) {
        Objects.requireNonNull(daikinUnits, "Units list can't be null");
        this.daikinUnits = daikinUnits;
        executorService =
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public Map<Daikin, DaikinStatus> getAllStatus() {
        return daikinUnits.stream()
                .collect(Collectors.toMap(u -> u, u -> executorService.submit(() -> u.getStatus())))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(k -> k.getKey(), j -> {
                    try {
                        return j.getValue().get();
                    } catch (Exception e) {
                        return null;
                    }
                }));
    }

    public void updateAllStatus(DaikinStatus status) {
        daikinUnits.stream()
                .map(k -> executorService.submit(() -> k.setStatus(status)))
                .map(k-> {
                    try {
                        return k.get();
                    } catch (InterruptedException e) {
                        return null;
                    } catch (ExecutionException e) {
                        return null;
                    }
                });
    }

    @Override
    public void close() {
        executorService.shutdown();
    }
}
