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
package zipkin2.storage;

import java.util.List;
import org.junit.Test;
import zipkin2.Span;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class GroupByTraceIdTest {

    Span oneOne = Span.newBuilder().traceId(1, 1).id(1).build();

    Span twoOne = Span.newBuilder().traceId(2, 1).id(1).build();

    Span zeroOne = Span.newBuilder().traceId(0, 1).id(1).build();

    @Test
    public void map_groupsEverythingWhenNotStrict() {
        List<Span> spans = asList(oneOne, twoOne, zeroOne);
        assertThat(GroupByTraceId.create(false).map(spans)).containsExactly(spans);
    }

    @Test
    public void map_groupsByTraceIdHighWheStrict() {
        List<Span> spans = asList(oneOne, twoOne, zeroOne);
        assertThat(GroupByTraceId.create(true).map(spans)).containsExactly(asList(oneOne), asList(twoOne), asList(zeroOne));
    }

    @Test
    public void map_modifiable() {
        List<Span> spans = asList(oneOne, twoOne, zeroOne);
        List<List<Span>> modifiable = GroupByTraceId.create(true).map(spans);
        // This transform is used in cassandra v1 and filters when traces match on lower 64bits, but not
        // the higher ones.
        assertThat(StrictTraceId.filterTraces(asList(twoOne.traceId())).map(modifiable)).containsExactly(asList(twoOne));
    }

        @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    @org.openjdk.jmh.annotations.BenchmarkMode(org.openjdk.jmh.annotations.Mode.Throughput)
    @org.openjdk.jmh.annotations.Warmup(iterations = 10, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Measurement(iterations = 30, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.OutputTimeUnit(java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Fork(value = 1 )
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_map_groupsEverythingWhenNotStrict() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::map_groupsEverythingWhenNotStrict, this.description("map_groupsEverythingWhenNotStrict"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_map_groupsByTraceIdHighWheStrict() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::map_groupsByTraceIdHighWheStrict, this.description("map_groupsByTraceIdHighWheStrict"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_map_modifiable() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::map_modifiable, this.description("map_modifiable"));
        }

        private GroupByTraceIdTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new GroupByTraceIdTest();
        }

        @java.lang.Override
        public GroupByTraceIdTest implementation() {
            return this.implementation;
        }
    }
}
