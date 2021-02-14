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

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import zipkin2.Span;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static zipkin2.TestObjects.CLIENT_SPAN;
import static zipkin2.TestObjects.FRONTEND;
import static zipkin2.TestObjects.TODAY;
import static zipkin2.TestObjects.TRACE;
import static zipkin2.TestObjects.newTrace;
import static zipkin2.storage.ITSpanStore.requestBuilder;

public class StrictTraceIdTest {

    @Test
    public void filterTraces_skipsOnNoClash() {
        Span oneOne = Span.newBuilder().traceId(1, 1).id(1).build();
        Span oneTwo = Span.newBuilder().traceId(1, 2).id(1).build();
        List<List<Span>> traces = asList(asList(oneOne), asList(oneTwo));
        assertThat(StrictTraceId.filterTraces(requestBuilder().spanName("11").build()).map(traces)).isSameAs(traces);
    }

    @Test
    public void filterTraces_onSpanName() {
        assertThat(StrictTraceId.filterTraces(requestBuilder().spanName("11").build()).map(traces())).flatExtracting(l -> l).isEmpty();
        assertThat(StrictTraceId.filterTraces(requestBuilder().spanName("1").build()).map(traces())).containsExactly(traces().get(0));
    }

    @Test
    public void filterTraces_onTag() {
        assertThat(StrictTraceId.filterTraces(requestBuilder().parseAnnotationQuery("foo=0").build()).map(traces())).flatExtracting(l -> l).isEmpty();
        assertThat(StrictTraceId.filterTraces(requestBuilder().parseAnnotationQuery("foo=1").build()).map(traces())).containsExactly(traces().get(0));
    }

    @Test
    public void filterSpans() {
        ArrayList<Span> trace = new ArrayList<>(TRACE);
        assertThat(StrictTraceId.filterSpans(CLIENT_SPAN.traceId()).map(trace)).isEqualTo(TRACE);
        trace.set(1, CLIENT_SPAN.toBuilder().traceId(CLIENT_SPAN.traceId().substring(16)).build());
        assertThat(StrictTraceId.filterSpans(CLIENT_SPAN.traceId()).map(trace)).doesNotContain(CLIENT_SPAN);
    }

    List<List<Span>> traces() {
        // 64-bit trace ID
        Span span1 = Span.newBuilder().traceId(CLIENT_SPAN.traceId().substring(16)).id("1").name("1").putTag("foo", "1").timestamp(TODAY * 1000L).localEndpoint(FRONTEND).build();
        // 128-bit trace ID prefixed by above
        Span span2 = span1.toBuilder().traceId(CLIENT_SPAN.traceId()).name("2").putTag("foo", "2").build();
        // Different 128-bit trace ID prefixed by above
        Span span3 = span1.toBuilder().traceId("1" + span1.traceId()).name("3").putTag("foo", "3").build();
        return new ArrayList<>(asList(asList(span1), asList(span2), asList(span3)));
    }

    @Test
    public void hasClashOnLowerTraceId() {
        Span oneOne = Span.newBuilder().traceId(1, 1).id(1).build();
        Span twoOne = Span.newBuilder().traceId(2, 1).id(1).build();
        Span zeroOne = Span.newBuilder().traceId(0, 1).id(1).build();
        Span oneTwo = Span.newBuilder().traceId(1, 2).id(1).build();
        assertThat(StrictTraceId.hasClashOnLowerTraceId(asList(asList(oneOne), asList(oneTwo)))).isFalse();
        assertThat(StrictTraceId.hasClashOnLowerTraceId(asList(asList(oneOne), asList(twoOne)))).isTrue();
        assertThat(StrictTraceId.hasClashOnLowerTraceId(asList(asList(oneOne), asList(zeroOne)))).isTrue();
    }

        @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    @org.openjdk.jmh.annotations.BenchmarkMode(org.openjdk.jmh.annotations.Mode.Throughput)
    @org.openjdk.jmh.annotations.Warmup(iterations = 10, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Measurement(iterations = 30, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.OutputTimeUnit(java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Fork(value = 1 )
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterTraces_skipsOnNoClash() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterTraces_skipsOnNoClash, this.description("filterTraces_skipsOnNoClash"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterTraces_onSpanName() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterTraces_onSpanName, this.description("filterTraces_onSpanName"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterTraces_onTag() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterTraces_onTag, this.description("filterTraces_onTag"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_filterSpans() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::filterSpans, this.description("filterSpans"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_hasClashOnLowerTraceId() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::hasClashOnLowerTraceId, this.description("hasClashOnLowerTraceId"));
        }

        private StrictTraceIdTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new StrictTraceIdTest();
        }

        @java.lang.Override
        public StrictTraceIdTest implementation() {
            return this.implementation;
        }
    }
}
