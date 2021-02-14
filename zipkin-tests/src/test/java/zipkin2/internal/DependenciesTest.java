/*
 * Copyright 2015-2020 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package zipkin2.internal;

import java.nio.ByteBuffer;
import org.junit.Test;
import zipkin2.DependencyLink;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public final class DependenciesTest {

    @Test
    public void dependenciesRoundTrip() {
        DependencyLink ab = DependencyLink.newBuilder().parent("a").child("b").callCount(2L).build();
        DependencyLink cd = DependencyLink.newBuilder().parent("c").child("d").errorCount(2L).build();
        Dependencies dependencies = Dependencies.create(1L, 2L, asList(ab, cd));
        ByteBuffer bytes = dependencies.toThrift();
        assertThat(Dependencies.fromThrift(bytes)).isEqualTo(dependencies);
    }

        @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    @org.openjdk.jmh.annotations.BenchmarkMode(org.openjdk.jmh.annotations.Mode.Throughput)
    @org.openjdk.jmh.annotations.Warmup(iterations = 10, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Measurement(iterations = 30, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.OutputTimeUnit(java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Fork(value = 1 )
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_dependenciesRoundTrip() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::dependenciesRoundTrip, this.description("dependenciesRoundTrip"));
        }

        private DependenciesTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new DependenciesTest();
        }

        @java.lang.Override
        public DependenciesTest implementation() {
            return this.implementation;
        }
    }
}
