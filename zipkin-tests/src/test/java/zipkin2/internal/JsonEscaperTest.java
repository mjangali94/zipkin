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

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static zipkin2.internal.JsonEscaper.jsonEscape;
import static zipkin2.internal.JsonEscaper.jsonEscapedSizeInBytes;

public class JsonEscaperTest {

    @Test
    public void testJsonEscapedSizeInBytes() {
        assertThat(jsonEscapedSizeInBytes(new String(new char[] { 0, 'a', 1 }))).isEqualTo(13);
        assertThat(jsonEscapedSizeInBytes(new String(new char[] { '"', '\\', '\t', '\b' }))).isEqualTo(8);
        assertThat(jsonEscapedSizeInBytes(new String(new char[] { '\n', '\r', '\f' }))).isEqualTo(6);
        assertThat(jsonEscapedSizeInBytes("\u2028 and \u2029")).isEqualTo(17);
        assertThat(jsonEscapedSizeInBytes("\"foo")).isEqualTo(5);
    }

    @Test
    public void testJsonEscape() {
        assertThat(jsonEscape(new String(new char[] { 0, 'a', 1 })).toString()).isEqualTo("\\u0000a\\u0001");
        assertThat(jsonEscape(new String(new char[] { '"', '\\', '\t', '\b' })).toString()).isEqualTo("\\\"\\\\\\t\\b");
        assertThat(jsonEscape(new String(new char[] { '\n', '\r', '\f' })).toString()).isEqualTo("\\n\\r\\f");
        assertThat(jsonEscape("\u2028 and \u2029").toString()).isEqualTo("\\u2028 and \\u2029");
        assertThat(jsonEscape("\"foo").toString()).isEqualTo("\\\"foo");
    }

        @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    @org.openjdk.jmh.annotations.BenchmarkMode(org.openjdk.jmh.annotations.Mode.Throughput)
    @org.openjdk.jmh.annotations.Warmup(iterations = 10, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Measurement(iterations = 30, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.OutputTimeUnit(java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Fork(value = 1 )
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testJsonEscapedSizeInBytes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testJsonEscapedSizeInBytes, this.description("testJsonEscapedSizeInBytes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testJsonEscape() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testJsonEscape, this.description("testJsonEscape"));
        }

        private JsonEscaperTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new JsonEscaperTest();
        }

        @java.lang.Override
        public JsonEscaperTest implementation() {
            return this.implementation;
        }
    }
}
