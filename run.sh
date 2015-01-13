#!/bin/bash

# Make sure Java 8 is available.

mvn clean install

java -Xms4096m -Xmx8192m -jar target/benchmarks.jar org.openjdk.jmh.bugreport.benchmark.ApplicationBenchmark

