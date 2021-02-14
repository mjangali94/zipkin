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
package zipkin2;

import org.junit.Test;
import zipkin2.codec.SpanBytesDecoder;
import zipkin2.codec.SpanBytesEncoder;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static zipkin2.TestObjects.FRONTEND;

public class SpanBytesDecoderDetectorTest {

    Span span1 = Span.newBuilder().traceId("a").id("b").name("get").timestamp(10).duration(30).kind(Span.Kind.SERVER).shared(true).putTag("http.method", "GET").localEndpoint(FRONTEND).build();

    Span span2 = Span.newBuilder().traceId("a").parentId("b").id("c").name("get").timestamp(15).duration(10).localEndpoint(FRONTEND).build();

    @Test
    public void decoderForMessage_json_v1() {
        byte[] message = SpanBytesEncoder.JSON_V1.encode(span1);
        assertThat(SpanBytesDecoderDetector.decoderForMessage(message)).isEqualTo(SpanBytesDecoder.JSON_V1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decoderForMessage_json_v1_list() {
        byte[] message = SpanBytesEncoder.JSON_V1.encodeList(asList(span1, span2));
        SpanBytesDecoderDetector.decoderForMessage(message);
    }

    @Test
    public void decoderForListMessage_json_v1() {
        byte[] message = SpanBytesEncoder.JSON_V1.encodeList(asList(span1, span2));
        assertThat(SpanBytesDecoderDetector.decoderForListMessage(message)).isEqualTo(SpanBytesDecoder.JSON_V1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decoderForListMessage_json_v1_singleItem() {
        byte[] message = SpanBytesEncoder.JSON_V1.encode(span1);
        SpanBytesDecoderDetector.decoderForListMessage(message);
    }

    /**
     * Single-element reads were for legacy non-list encoding. Don't add new code that does this
     */
    @Test(expected = UnsupportedOperationException.class)
    public void decoderForMessage_json_v2() {
        byte[] message = SpanBytesEncoder.JSON_V2.encode(span1);
        assertThat(SpanBytesDecoderDetector.decoderForMessage(message)).isEqualTo(SpanBytesDecoder.JSON_V2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decoderForMessage_json_v2_list() {
        byte[] message = SpanBytesEncoder.JSON_V2.encodeList(asList(span1, span2));
        SpanBytesDecoderDetector.decoderForMessage(message);
    }

    @Test
    public void decoderForListMessage_json_v2() {
        byte[] message = SpanBytesEncoder.JSON_V2.encodeList(asList(span1, span2));
        assertThat(SpanBytesDecoderDetector.decoderForListMessage(message)).isEqualTo(SpanBytesDecoder.JSON_V2);
    }

    @Test
    public void decoderForListMessage_json_v2_partial_localEndpoint() {
        Span span = Span.newBuilder().traceId("a").id("b").localEndpoint(Endpoint.newBuilder().serviceName("foo").build()).build();
        byte[] message = SpanBytesEncoder.JSON_V2.encodeList(asList(span));
        assertThat(SpanBytesDecoderDetector.decoderForListMessage(message)).isEqualTo(SpanBytesDecoder.JSON_V2);
    }

    @Test
    public void decoderForListMessage_json_v2_partial_remoteEndpoint() {
        Span span = Span.newBuilder().traceId("a").id("b").kind(Span.Kind.CLIENT).remoteEndpoint(Endpoint.newBuilder().serviceName("foo").build()).build();
        byte[] message = SpanBytesEncoder.JSON_V2.encodeList(asList(span));
        assertThat(SpanBytesDecoderDetector.decoderForListMessage(message)).isEqualTo(SpanBytesDecoder.JSON_V2);
    }

    @Test
    public void decoderForListMessage_json_v2_partial_tag() {
        Span span = Span.newBuilder().traceId("a").id("b").putTag("foo", "bar").build();
        byte[] message = SpanBytesEncoder.JSON_V2.encodeList(asList(span));
        assertThat(SpanBytesDecoderDetector.decoderForListMessage(message)).isEqualTo(SpanBytesDecoder.JSON_V2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decoderForListMessage_json_v2_singleItem() {
        byte[] message = SpanBytesEncoder.JSON_V2.encode(span1);
        SpanBytesDecoderDetector.decoderForListMessage(message);
    }

    @Test
    public void decoderForMessage_thrift() {
        byte[] message = SpanBytesEncoder.THRIFT.encode(span1);
        assertThat(SpanBytesDecoderDetector.decoderForMessage(message)).isEqualTo(SpanBytesDecoder.THRIFT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decoderForMessage_thrift_list() {
        byte[] message = SpanBytesEncoder.THRIFT.encodeList(asList(span1, span2));
        SpanBytesDecoderDetector.decoderForMessage(message);
    }

    @Test
    public void decoderForListMessage_thrift() {
        byte[] message = SpanBytesEncoder.THRIFT.encodeList(asList(span1, span2));
        assertThat(SpanBytesDecoderDetector.decoderForListMessage(message)).isEqualTo(SpanBytesDecoder.THRIFT);
    }

    /**
     * We encoded incorrectly for years, so we have to read this data eventhough it is wrong.
     *
     * <p>See openzipkin/zipkin-reporter-java#133
     */
    @Test
    public void decoderForListMessage_thrift_incorrectFirstByte() {
        byte[] message = SpanBytesEncoder.THRIFT.encodeList(asList(span1, span2));
        // We made a typo.. it should have been 12
        message[0] = 11;
        assertThat(SpanBytesDecoderDetector.decoderForListMessage(message)).isEqualTo(SpanBytesDecoder.THRIFT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decoderForListMessage_thrift_singleItem() {
        byte[] message = SpanBytesEncoder.THRIFT.encode(span1);
        SpanBytesDecoderDetector.decoderForListMessage(message);
    }

    /**
     * Single-element reads were for legacy non-list encoding. Don't add new code that does this
     */
    @Test(expected = UnsupportedOperationException.class)
    public void decoderForMessage_proto3() {
        byte[] message = SpanBytesEncoder.PROTO3.encode(span1);
        assertThat(SpanBytesDecoderDetector.decoderForMessage(message)).isEqualTo(SpanBytesDecoder.PROTO3);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void decoderForMessage_proto3_list() {
        byte[] message = SpanBytesEncoder.PROTO3.encodeList(asList(span1, span2));
        SpanBytesDecoderDetector.decoderForMessage(message);
    }

    @Test
    public void decoderForListMessage_proto3() {
        byte[] message = SpanBytesEncoder.PROTO3.encodeList(asList(span1, span2));
        assertThat(SpanBytesDecoderDetector.decoderForListMessage(message)).isEqualTo(SpanBytesDecoder.PROTO3);
    }

    /**
     * There is no difference between a list of size one and a single element in proto3
     */
    @Test
    public void decoderForListMessage_proto3_singleItem() {
        byte[] message = SpanBytesEncoder.PROTO3.encode(span1);
        assertThat(SpanBytesDecoderDetector.decoderForListMessage(message)).isEqualTo(SpanBytesDecoder.PROTO3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decoderForMessage_unknown() {
        SpanBytesDecoderDetector.decoderForMessage(new byte[] { 'h' });
    }

    @Test(expected = IllegalArgumentException.class)
    public void decoderForListMessage_unknown() {
        SpanBytesDecoderDetector.decoderForListMessage(new byte[] { 'h' });
    }

        @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    @org.openjdk.jmh.annotations.BenchmarkMode(org.openjdk.jmh.annotations.Mode.Throughput)
    @org.openjdk.jmh.annotations.Warmup(iterations = 10, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Measurement(iterations = 30, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.OutputTimeUnit(java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Fork(value = 1 )
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_decoderForMessage_json_v1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::decoderForMessage_json_v1, this.description("decoderForMessage_json_v1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_decoderForMessage_json_v1_list() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::decoderForMessage_json_v1_list, this.description("decoderForMessage_json_v1_list"), java.lang.IllegalArgumentException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_decoderForListMessage_json_v1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::decoderForListMessage_json_v1, this.description("decoderForListMessage_json_v1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_decoderForListMessage_json_v1_singleItem() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::decoderForListMessage_json_v1_singleItem, this.description("decoderForListMessage_json_v1_singleItem"), java.lang.IllegalArgumentException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_decoderForMessage_json_v2() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::decoderForMessage_json_v2, this.description("decoderForMessage_json_v2"), java.lang.UnsupportedOperationException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_decoderForMessage_json_v2_list() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::decoderForMessage_json_v2_list, this.description("decoderForMessage_json_v2_list"), java.lang.IllegalArgumentException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_decoderForListMessage_json_v2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::decoderForListMessage_json_v2, this.description("decoderForListMessage_json_v2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_decoderForListMessage_json_v2_partial_localEndpoint() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::decoderForListMessage_json_v2_partial_localEndpoint, this.description("decoderForListMessage_json_v2_partial_localEndpoint"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_decoderForListMessage_json_v2_partial_remoteEndpoint() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::decoderForListMessage_json_v2_partial_remoteEndpoint, this.description("decoderForListMessage_json_v2_partial_remoteEndpoint"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_decoderForListMessage_json_v2_partial_tag() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::decoderForListMessage_json_v2_partial_tag, this.description("decoderForListMessage_json_v2_partial_tag"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_decoderForListMessage_json_v2_singleItem() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::decoderForListMessage_json_v2_singleItem, this.description("decoderForListMessage_json_v2_singleItem"), java.lang.IllegalArgumentException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_decoderForMessage_thrift() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::decoderForMessage_thrift, this.description("decoderForMessage_thrift"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_decoderForMessage_thrift_list() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::decoderForMessage_thrift_list, this.description("decoderForMessage_thrift_list"), java.lang.IllegalArgumentException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_decoderForListMessage_thrift() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::decoderForListMessage_thrift, this.description("decoderForListMessage_thrift"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_decoderForListMessage_thrift_incorrectFirstByte() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::decoderForListMessage_thrift_incorrectFirstByte, this.description("decoderForListMessage_thrift_incorrectFirstByte"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_decoderForListMessage_thrift_singleItem() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::decoderForListMessage_thrift_singleItem, this.description("decoderForListMessage_thrift_singleItem"), java.lang.IllegalArgumentException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_decoderForMessage_proto3() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::decoderForMessage_proto3, this.description("decoderForMessage_proto3"), java.lang.UnsupportedOperationException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_decoderForMessage_proto3_list() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::decoderForMessage_proto3_list, this.description("decoderForMessage_proto3_list"), java.lang.UnsupportedOperationException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_decoderForListMessage_proto3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::decoderForListMessage_proto3, this.description("decoderForListMessage_proto3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_decoderForListMessage_proto3_singleItem() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::decoderForListMessage_proto3_singleItem, this.description("decoderForListMessage_proto3_singleItem"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_decoderForMessage_unknown() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::decoderForMessage_unknown, this.description("decoderForMessage_unknown"), java.lang.IllegalArgumentException.class);
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_decoderForListMessage_unknown() throws java.lang.Throwable {
            this.createImplementation();
            this.runExceptionBenchmark(this.implementation()::decoderForListMessage_unknown, this.description("decoderForListMessage_unknown"), java.lang.IllegalArgumentException.class);
        }

        private SpanBytesDecoderDetectorTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SpanBytesDecoderDetectorTest();
        }

        @java.lang.Override
        public SpanBytesDecoderDetectorTest implementation() {
            return this.implementation;
        }
    }
}
