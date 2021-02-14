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
package zipkin2.v1;

import org.junit.Test;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.Span.Kind;
import static org.assertj.core.api.Assertions.assertThat;
import static zipkin2.TestObjects.BACKEND;
import static zipkin2.TestObjects.FRONTEND;

public class V1SpanConverterTest {

    Endpoint kafka = Endpoint.newBuilder().serviceName("kafka").build();

    V1SpanConverter v1SpanConverter = new V1SpanConverter();

    @Test
    public void convert_ma() {
        V1Span v1 = V1Span.newBuilder().traceId(1L).id(2L).addAnnotation(1472470996199000L, "mr", BACKEND).addBinaryAnnotation("ma", kafka).build();
        Span v2 = Span.newBuilder().traceId("1").id("2").kind(Kind.CONSUMER).timestamp(1472470996199000L).localEndpoint(BACKEND).remoteEndpoint(kafka).build();
        assertThat(v1SpanConverter.convert(v1)).containsExactly(v2);
    }

    @Test
    public void convert_sa() {
        V1Span v1 = V1Span.newBuilder().traceId(1L).id(2L).addAnnotation(1472470996199000L, "cs", FRONTEND).addBinaryAnnotation("sa", BACKEND).build();
        Span v2 = Span.newBuilder().traceId("1").id("2").kind(Kind.CLIENT).timestamp(1472470996199000L).localEndpoint(FRONTEND).remoteEndpoint(BACKEND).build();
        assertThat(v1SpanConverter.convert(v1)).containsExactly(v2);
    }

    @Test
    public void convert_ca() {
        V1Span v1 = V1Span.newBuilder().traceId(1L).id(2L).addAnnotation(1472470996199000L, "sr", BACKEND).addBinaryAnnotation("ca", FRONTEND).build();
        Span v2 = Span.newBuilder().traceId("1").id("2").kind(Kind.SERVER).timestamp(1472470996199000L).localEndpoint(BACKEND).remoteEndpoint(FRONTEND).shared(true).build();
        assertThat(v1SpanConverter.convert(v1)).containsExactly(v2);
    }

    // Following 3 tests show leniency for old versions of zipkin-ruby which serialized address binary
    // annotations as "1" instead of true
    @Test
    public void convert_ma_incorrect_value() {
        V1Span v1 = V1Span.newBuilder().traceId(1L).id(2L).addAnnotation(1472470996199000L, "mr", BACKEND).addBinaryAnnotation("ma", "1", kafka).build();
        Span v2 = Span.newBuilder().traceId("1").id("2").kind(Kind.CONSUMER).timestamp(1472470996199000L).localEndpoint(BACKEND).remoteEndpoint(kafka).build();
        assertThat(v1SpanConverter.convert(v1)).containsExactly(v2);
    }

    @Test
    public void convert_sa_incorrect_value() {
        V1Span v1 = V1Span.newBuilder().traceId(1L).id(2L).addAnnotation(1472470996199000L, "cs", FRONTEND).addBinaryAnnotation("sa", "1", BACKEND).build();
        Span v2 = Span.newBuilder().traceId("1").id("2").kind(Kind.CLIENT).timestamp(1472470996199000L).localEndpoint(FRONTEND).remoteEndpoint(BACKEND).build();
        assertThat(v1SpanConverter.convert(v1)).containsExactly(v2);
    }

    @Test
    public void convert_ca_incorrect_value() {
        V1Span v1 = V1Span.newBuilder().traceId(1L).id(2L).addAnnotation(1472470996199000L, "sr", BACKEND).addBinaryAnnotation("ca", "1", FRONTEND).build();
        Span v2 = Span.newBuilder().traceId("1").id("2").kind(Kind.SERVER).timestamp(1472470996199000L).localEndpoint(BACKEND).remoteEndpoint(FRONTEND).shared(true).build();
        assertThat(v1SpanConverter.convert(v1)).containsExactly(v2);
    }

        @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    @org.openjdk.jmh.annotations.BenchmarkMode(org.openjdk.jmh.annotations.Mode.Throughput)
    @org.openjdk.jmh.annotations.Warmup(iterations = 10, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Measurement(iterations = 30, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.OutputTimeUnit(java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Fork(value = 1 )
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_convert_ma() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::convert_ma, this.description("convert_ma"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_convert_sa() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::convert_sa, this.description("convert_sa"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_convert_ca() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::convert_ca, this.description("convert_ca"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_convert_ma_incorrect_value() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::convert_ma_incorrect_value, this.description("convert_ma_incorrect_value"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_convert_sa_incorrect_value() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::convert_sa_incorrect_value, this.description("convert_sa_incorrect_value"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_convert_ca_incorrect_value() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::convert_ca_incorrect_value, this.description("convert_ca_incorrect_value"));
        }

        private V1SpanConverterTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new V1SpanConverterTest();
        }

        @java.lang.Override
        public V1SpanConverterTest implementation() {
            return this.implementation;
        }
    }
}
