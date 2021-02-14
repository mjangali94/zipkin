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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import zipkin2.Endpoint;
import zipkin2.Span;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static zipkin2.TestObjects.CLIENT_SPAN;
import static zipkin2.TestObjects.TODAY;

public class V2SpanWriterTest {

    V2SpanWriter writer = new V2SpanWriter();

    // bigger than needed to test sizeInBytes
    byte[] bytes = new byte[2048];

    WriteBuffer buf = WriteBuffer.wrap(bytes);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void sizeInBytes() {
        writer.write(CLIENT_SPAN, buf);
        assertThat(writer.sizeInBytes(CLIENT_SPAN)).isEqualTo(buf.pos());
    }

    @Test
    public void writes128BitTraceId() {
        writer.write(CLIENT_SPAN, buf);
        assertThat(new String(bytes, UTF_8)).startsWith("{\"traceId\":\"" + CLIENT_SPAN.traceId() + "\"");
    }

    @Test
    public void writesAnnotationWithoutEndpoint() {
        writer.write(CLIENT_SPAN, buf);
        assertThat(new String(bytes, UTF_8)).contains("{\"timestamp\":" + (TODAY + 100) * 1000L + ",\"value\":\"foo\"}");
    }

    @Test
    public void omitsEmptySpanName() {
        Span span = Span.newBuilder().traceId("7180c278b62e8f6a216a2aea45d08fc9").parentId("6b221d5bc9e6496c").id("5b4185666d50f68b").build();
        writer.write(span, buf);
        assertThat(new String(bytes, UTF_8)).doesNotContain("name");
    }

    @Test
    public void omitsEmptyServiceName() {
        Span span = CLIENT_SPAN.toBuilder().localEndpoint(Endpoint.newBuilder().ip("127.0.0.1").build()).build();
        writer.write(span, buf);
        assertThat(new String(bytes, UTF_8)).contains("\"localEndpoint\":{\"ipv4\":\"127.0.0.1\"}");
    }

    @Test
    public void tagsAreAMap() {
        writer.write(CLIENT_SPAN, buf);
        assertThat(new String(bytes, UTF_8)).contains("\"tags\":{\"clnt/finagle.version\":\"6.45.0\",\"http.path\":\"/api\"}");
    }

        @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    @org.openjdk.jmh.annotations.BenchmarkMode(org.openjdk.jmh.annotations.Mode.Throughput)
    @org.openjdk.jmh.annotations.Warmup(iterations = 10, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Measurement(iterations = 30, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.OutputTimeUnit(java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Fork(value = 1 )
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_sizeInBytes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::sizeInBytes, this.description("sizeInBytes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writes128BitTraceId() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writes128BitTraceId, this.description("writes128BitTraceId"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writesAnnotationWithoutEndpoint() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writesAnnotationWithoutEndpoint, this.description("writesAnnotationWithoutEndpoint"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_omitsEmptySpanName() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::omitsEmptySpanName, this.description("omitsEmptySpanName"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_omitsEmptyServiceName() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::omitsEmptyServiceName, this.description("omitsEmptyServiceName"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tagsAreAMap() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tagsAreAMap, this.description("tagsAreAMap"));
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().thrown, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private V2SpanWriterTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new V2SpanWriterTest();
        }

        @java.lang.Override
        public V2SpanWriterTest implementation() {
            return this.implementation;
        }
    }
}
