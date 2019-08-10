package com.github.ddth.akka.qnd;

import com.github.ddth.akka.utils.AkkaUtils;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class QndGenerateId {
    public static void main(String[] args) {
        final int NUM_RUNS = 10_000_000;
        long t1, t2;

        BloomFilter<String> bloomUUID = BloomFilter
                .create(Funnels.stringFunnel(StandardCharsets.UTF_8), 2 * NUM_RUNS, 0.0001);
        t1 = System.currentTimeMillis();
        for (int i = 0; i < NUM_RUNS; i++) {
            String id = UUID.randomUUID().toString();
            if (bloomUUID.mightContain(id)) {
                System.err.print("Already existed: " + id);
            }
            bloomUUID.put(id);
        }
        t2 = System.currentTimeMillis();
        System.out.println("UUID: " + (t2 - t1) + "ms -> " + (1000L * NUM_RUNS / (t2 - t1) + " id/sec"));

        BloomFilter<String> bloomID = BloomFilter
                .create(Funnels.stringFunnel(StandardCharsets.UTF_8), 2 * NUM_RUNS, 0.0001);
        t1 = System.currentTimeMillis();
        for (int i = 0; i < NUM_RUNS; i++) {
            String id = AkkaUtils.nextId();
            if (bloomID.mightContain(id)) {
                System.err.print("Already existed: " + id);
            }
            bloomID.put(id);
        }
        t2 = System.currentTimeMillis();
        System.out.println("ID  : " + (t2 - t1) + "ms -> " + (1000L * NUM_RUNS / (t2 - t1) + " id/sec"));
    }
}
