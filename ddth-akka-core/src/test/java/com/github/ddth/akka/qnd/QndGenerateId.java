package com.github.ddth.akka.qnd;

import java.util.UUID;

import com.github.ddth.akka.AkkaUtils;

public class QndGenerateId {

    public static void main(String[] args) {
        final int NUM_RUNS = 10_000_000;

        long t1 = System.currentTimeMillis();
        for (int i = 0; i < NUM_RUNS; i++) {
            String id = UUID.randomUUID().toString();
        }
        long t2 = System.currentTimeMillis();
        for (int i = 0; i < NUM_RUNS; i++) {
            String id = AkkaUtils.nextId();
        }
        long t3 = System.currentTimeMillis();

        System.out.println(
                "UUID: " + (t2 - t1) + "ms -> " + (1000L * NUM_RUNS / (t2 - t1) + " id/sec"));
        System.out.println(
                "ID  : " + (t3 - t2) + "ms -> " + (1000L * NUM_RUNS / (t3 - t2) + " id/sec"));
    }
}
