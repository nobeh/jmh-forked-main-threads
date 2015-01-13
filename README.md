This is a sample project to demonstrate if the application in `ForkedMain` cannot completely shut down the running threads, then JMH is not able to finalize and complete the benchmark.

To run the sample, you need to have Java 8. Simply run:

```bash
$ ./run.sh
```

Then you can use `jps` and `jstack -l` on `ForkedMain` to observe that there are still running threads inside it and JMH is still waiting on them.

To start with, look at `ApplicationBenchmark`. Change how the application is shut down through the chain in `#teardown()` and it is observable that JMH completes the benchmark.

