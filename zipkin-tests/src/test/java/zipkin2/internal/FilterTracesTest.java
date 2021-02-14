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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import zipkin2.Span;
import zipkin2.TestObjects;
import zipkin2.storage.QueryRequest;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static zipkin2.TestObjects.TODAY;

public class FilterTracesTest {

    QueryRequest request = QueryRequest.newBuilder().endTs(TODAY).lookback(1).limit(1).build();

    @Test
    public void returnsWhenValidlyMatches() {
        List<List<Span>> input = new ArrayList<>(asList(TestObjects.TRACE));
        assertThat(FilterTraces.create(request).map(input)).isEqualTo(input);
    }

    @Test
    public void doesntMutateInputWhenUnmatched() {
        List<List<Span>> input = Collections.unmodifiableList(asList(TestObjects.TRACE));
        assertThat(FilterTraces.create(request.toBuilder().endTs(1).build()).map(input)).isEmpty();
    }

        @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    @org.openjdk.jmh.annotations.BenchmarkMode(org.openjdk.jmh.annotations.Mode.Throughput)
    @org.openjdk.jmh.annotations.Warmup(iterations = 10, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Measurement(iterations = 30, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.OutputTimeUnit(java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Fork(value = 1 )
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_returnsWhenValidlyMatches() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::returnsWhenValidlyMatches, this.description("returnsWhenValidlyMatches"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doesntMutateInputWhenUnmatched() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doesntMutateInputWhenUnmatched, this.description("doesntMutateInputWhenUnmatched"));
        }

        private FilterTracesTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new FilterTracesTest();
        }

        @java.lang.Override
        public FilterTracesTest implementation() {
            return this.implementation;
        }
    }
}
