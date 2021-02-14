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
package zipkin2.codec;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import zipkin2.Endpoint;
import zipkin2.Span;
import static org.assertj.core.api.Assertions.assertThat;
import static zipkin2.TestObjects.BACKEND;
import static zipkin2.TestObjects.TRACE;
import static zipkin2.codec.SpanBytesEncoderTest.ERROR_SPAN;
import static zipkin2.codec.SpanBytesEncoderTest.LOCAL_SPAN;
import static zipkin2.codec.SpanBytesEncoderTest.NO_ANNOTATIONS_ROOT_SERVER_SPAN;
import static zipkin2.codec.SpanBytesEncoderTest.SPAN;
import static zipkin2.codec.SpanBytesEncoderTest.UTF8_SPAN;
import static zipkin2.codec.SpanBytesEncoderTest.UTF_8;

public class SpanBytesDecoderTest {

    Span span = SPAN;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void niceErrorOnTruncatedSpans_PROTO3() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Truncated: length 66 > bytes available 8 reading List<Span> from proto3");
        byte[] encoded = SpanBytesEncoder.PROTO3.encodeList(TRACE);
        SpanBytesDecoder.PROTO3.decodeList(Arrays.copyOfRange(encoded, 0, 10));
    }

    @Test
    public void niceErrorOnTruncatedSpan_PROTO3() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Truncated: length 179 > bytes available 7 reading Span from proto3");
        byte[] encoded = SpanBytesEncoder.PROTO3.encode(SPAN);
        SpanBytesDecoder.PROTO3.decodeOne(Arrays.copyOfRange(encoded, 0, 10));
    }

    @Test
    public void emptyListOk_JSON_V1() {
        assertThat(SpanBytesDecoder.JSON_V1.decodeList(new byte[0])).isEmpty();
        assertThat(SpanBytesDecoder.JSON_V1.decodeList(new byte[] { '[', ']' })).isEmpty();
    }

    @Test
    public void emptyListOk_JSON_V2() {
        assertThat(SpanBytesDecoder.JSON_V2.decodeList(new byte[0])).isEmpty();
        assertThat(SpanBytesDecoder.JSON_V2.decodeList(new byte[] { '[', ']' })).isEmpty();
    }

    @Test
    public void emptyListOk_PROTO3() {
        assertThat(SpanBytesDecoder.PROTO3.decodeList(new byte[0])).isEmpty();
    }

    @Test
    public void spanRoundTrip_JSON_V2() {
        assertThat(SpanBytesDecoder.JSON_V2.decodeOne(SpanBytesEncoder.JSON_V2.encode(span))).isEqualTo(span);
    }

    @Test
    public void spanRoundTrip_PROTO3() {
        assertThat(SpanBytesDecoder.PROTO3.decodeOne(SpanBytesEncoder.PROTO3.encode(span))).isEqualTo(span);
    }

    @Test
    public void localSpanRoundTrip_JSON_V2() {
        assertThat(SpanBytesDecoder.JSON_V2.decodeOne(SpanBytesEncoder.JSON_V2.encode(LOCAL_SPAN))).isEqualTo(LOCAL_SPAN);
    }

    @Test
    public void localSpanRoundTrip_PROTO3() {
        assertThat(SpanBytesDecoder.PROTO3.decodeOne(SpanBytesEncoder.PROTO3.encode(LOCAL_SPAN))).isEqualTo(LOCAL_SPAN);
    }

    @Test
    public void errorSpanRoundTrip_JSON_V2() {
        assertThat(SpanBytesDecoder.JSON_V2.decodeOne(SpanBytesEncoder.JSON_V2.encode(ERROR_SPAN))).isEqualTo(ERROR_SPAN);
    }

    @Test
    public void errorSpanRoundTrip_PROTO3() {
        assertThat(SpanBytesDecoder.PROTO3.decodeOne(SpanBytesEncoder.PROTO3.encode(ERROR_SPAN))).isEqualTo(ERROR_SPAN);
    }

    @Test
    public void spanRoundTrip_64bitTraceId_JSON_V2() {
        span = span.toBuilder().traceId(span.traceId().substring(16)).build();
        assertThat(SpanBytesDecoder.JSON_V2.decodeOne(SpanBytesEncoder.JSON_V2.encode(span))).isEqualTo(span);
    }

    @Test
    public void spanRoundTrip_64bitTraceId_PROTO3() {
        span = span.toBuilder().traceId(span.traceId().substring(16)).build();
        assertThat(SpanBytesDecoder.PROTO3.decodeOne(SpanBytesEncoder.PROTO3.encode(span))).isEqualTo(span);
    }

    @Test
    public void spanRoundTrip_shared_JSON_V2() {
        span = span.toBuilder().kind(Span.Kind.SERVER).shared(true).build();
        assertThat(SpanBytesDecoder.JSON_V2.decodeOne(SpanBytesEncoder.JSON_V2.encode(span))).isEqualTo(span);
    }

    @Test
    public void spanRoundTrip_shared_PROTO3() {
        span = span.toBuilder().kind(Span.Kind.SERVER).shared(true).build();
        assertThat(SpanBytesDecoder.PROTO3.decodeOne(SpanBytesEncoder.PROTO3.encode(span))).isEqualTo(span);
    }

    /**
     * This isn't a test of what we "should" accept as a span, rather that characters that trip-up
     * json don't fail in codec.
     */
    @Test
    public void specialCharsInJson_JSON_V2() {
        assertThat(SpanBytesDecoder.JSON_V2.decodeOne(SpanBytesEncoder.JSON_V2.encode(UTF8_SPAN))).isEqualTo(UTF8_SPAN);
    }

    @Test
    public void specialCharsInJson_PROTO3() {
        assertThat(SpanBytesDecoder.PROTO3.decodeOne(SpanBytesEncoder.PROTO3.encode(UTF8_SPAN))).isEqualTo(UTF8_SPAN);
    }

    @Test
    public void falseOnEmpty_inputSpans_JSON_V2() {
        assertThat(SpanBytesDecoder.JSON_V2.decodeList(new byte[0], new ArrayList<>())).isFalse();
    }

    @Test
    public void falseOnEmpty_inputSpans_PROTO3() {
        assertThat(SpanBytesDecoder.PROTO3.decodeList(new byte[0], new ArrayList<>())).isFalse();
    }

    /**
     * Particulary, thrift can mistake malformed content as a huge list. Let's not blow up.
     */
    @Test
    public void niceErrorOnMalformed_inputSpans_JSON_V2() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Malformed reading List<Span> from ");
        SpanBytesDecoder.JSON_V2.decodeList(new byte[] { 'h', 'e', 'l', 'l', 'o' });
    }

    @Test
    public void niceErrorOnMalformed_inputSpans_PROTO3() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Truncated: length 101 > bytes available 3 reading List<Span> from proto3");
        SpanBytesDecoder.PROTO3.decodeList(new byte[] { 'h', 'e', 'l', 'l', 'o' });
    }

    @Test
    public void traceRoundTrip_JSON_V2() {
        byte[] message = SpanBytesEncoder.JSON_V2.encodeList(TRACE);
        assertThat(SpanBytesDecoder.JSON_V2.decodeList(message)).isEqualTo(TRACE);
    }

    @Test
    public void traceRoundTrip_PROTO3() {
        byte[] message = SpanBytesEncoder.PROTO3.encodeList(TRACE);
        assertThat(SpanBytesDecoder.PROTO3.decodeList(message)).isEqualTo(TRACE);
    }

    @Test
    public void traceRoundTrip_PROTO3_directBuffer() {
        byte[] message = SpanBytesEncoder.PROTO3.encodeList(TRACE);
        ByteBuffer buf = ByteBuffer.allocateDirect(message.length);
        buf.put(message);
        buf.flip();
        assertThat(SpanBytesDecoder.PROTO3.decodeList(buf)).isEqualTo(TRACE);
    }

    @Test
    public void traceRoundTrip_PROTO3_heapBuffer() {
        byte[] message = SpanBytesEncoder.PROTO3.encodeList(TRACE);
        ByteBuffer buf = ByteBuffer.wrap(message);
        assertThat(SpanBytesDecoder.PROTO3.decodeList(buf)).isEqualTo(TRACE);
    }

    @Test
    public void traceRoundTrip_PROTO3_heapBufferOffset() {
        byte[] message = SpanBytesEncoder.PROTO3.encodeList(TRACE);
        byte[] array = new byte[message.length + 4 + 5];
        System.arraycopy(message, 0, array, 4, message.length);
        ByteBuffer buf = ByteBuffer.wrap(array, 4, message.length);
        assertThat(SpanBytesDecoder.PROTO3.decodeList(buf)).isEqualTo(TRACE);
    }

    @Test
    public void spansRoundTrip_JSON_V2() {
        List<Span> tenClientSpans = Collections.nCopies(10, span);
        byte[] message = SpanBytesEncoder.JSON_V2.encodeList(tenClientSpans);
        assertThat(SpanBytesDecoder.JSON_V2.decodeList(message)).isEqualTo(tenClientSpans);
    }

    @Test
    public void spansRoundTrip_PROTO3() {
        List<Span> tenClientSpans = Collections.nCopies(10, span);
        byte[] message = SpanBytesEncoder.PROTO3.encodeList(tenClientSpans);
        assertThat(SpanBytesDecoder.PROTO3.decodeList(message)).isEqualTo(tenClientSpans);
    }

    @Test
    public void spanRoundTrip_noRemoteServiceName_JSON_V2() {
        span = span.toBuilder().remoteEndpoint(BACKEND.toBuilder().serviceName(null).build()).build();
        assertThat(SpanBytesDecoder.JSON_V2.decodeOne(SpanBytesEncoder.JSON_V2.encode(span))).isEqualTo(span);
    }

    @Test
    public void spanRoundTrip_noRemoteServiceName_PROTO3() {
        span = span.toBuilder().remoteEndpoint(BACKEND.toBuilder().serviceName(null).build()).build();
        assertThat(SpanBytesDecoder.PROTO3.decodeOne(SpanBytesEncoder.PROTO3.encode(span))).isEqualTo(span);
    }

    @Test
    public void spanRoundTrip_noAnnotations_rootServerSpan_JSON_V2() {
        span = NO_ANNOTATIONS_ROOT_SERVER_SPAN;
        assertThat(SpanBytesDecoder.JSON_V2.decodeOne(SpanBytesEncoder.JSON_V2.encode(span))).isEqualTo(span);
    }

    @Test
    public void spanRoundTrip_noAnnotations_rootServerSpan_PROTO3() {
        span = NO_ANNOTATIONS_ROOT_SERVER_SPAN;
        assertThat(SpanBytesDecoder.PROTO3.decodeOne(SpanBytesEncoder.PROTO3.encode(span))).isEqualTo(span);
    }

    @Test
    public void spanRoundTrip_noAnnotations_rootServerSpan_incomplete_JSON_V2() {
        span = NO_ANNOTATIONS_ROOT_SERVER_SPAN.toBuilder().duration(null).build();
        assertThat(SpanBytesDecoder.JSON_V2.decodeOne(SpanBytesEncoder.JSON_V2.encode(span))).isEqualTo(span);
    }

    @Test
    public void spanRoundTrip_noAnnotations_rootServerSpan_incomplete_PROTO3() {
        span = NO_ANNOTATIONS_ROOT_SERVER_SPAN.toBuilder().duration(null).build();
        assertThat(SpanBytesDecoder.PROTO3.decodeOne(SpanBytesEncoder.PROTO3.encode(span))).isEqualTo(span);
    }

    @Test
    public void spanRoundTrip_noAnnotations_rootServerSpan_shared_JSON_V2() {
        span = NO_ANNOTATIONS_ROOT_SERVER_SPAN.toBuilder().shared(true).build();
        assertThat(SpanBytesDecoder.JSON_V2.decodeOne(SpanBytesEncoder.JSON_V2.encode(span))).isEqualTo(span);
    }

    @Test
    public void spanRoundTrip_noAnnotations_rootServerSpan_shared_PROTO3() {
        span = NO_ANNOTATIONS_ROOT_SERVER_SPAN.toBuilder().shared(true).build();
        assertThat(SpanBytesDecoder.PROTO3.decodeOne(SpanBytesEncoder.PROTO3.encode(span))).isEqualTo(span);
    }

    @Test
    public void niceErrorOnUppercase_traceId_JSON_V2() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("48485A3953BB6124 should be lower-hex encoded with no prefix");
        String json = "{\n" + "  \"traceId\": \"48485A3953BB6124\",\n" + "  \"name\": \"get-traces\",\n" + "  \"id\": \"6b221d5bc9e6496c\"\n" + "}";
        SpanBytesDecoder.JSON_V2.decodeOne(json.getBytes(UTF_8));
    }

    @Test
    public void readsTraceIdHighFromTraceIdField() {
        byte[] with128BitTraceId = ("{\n" + "  \"traceId\": \"48485a3953bb61246b221d5bc9e6496c\",\n" + "  \"name\": \"get-traces\",\n" + "  \"id\": \"6b221d5bc9e6496c\"\n" + "}").getBytes(UTF_8);
        byte[] withLower64bitsTraceId = ("{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"name\": \"get-traces\",\n" + "  \"id\": \"6b221d5bc9e6496c\"\n" + "}").getBytes(UTF_8);
        assertThat(SpanBytesDecoder.JSON_V2.decodeOne(with128BitTraceId)).isEqualTo(SpanBytesDecoder.JSON_V2.decodeOne(withLower64bitsTraceId).toBuilder().traceId("48485a3953bb61246b221d5bc9e6496c").build());
    }

    @Test
    public void ignoresNull_topLevelFields() {
        String json = "{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"parentId\": null,\n" + "  \"id\": \"6b221d5bc9e6496c\",\n" + "  \"name\": null,\n" + "  \"timestamp\": null,\n" + "  \"duration\": null,\n" + "  \"localEndpoint\": null,\n" + "  \"remoteEndpoint\": null,\n" + "  \"annotations\": null,\n" + "  \"tags\": null,\n" + "  \"debug\": null,\n" + "  \"shared\": null\n" + "}";
        SpanBytesDecoder.JSON_V2.decodeOne(json.getBytes(UTF_8));
    }

    @Test
    public void ignoresNull_endpoint_topLevelFields() {
        String json = "{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"name\": \"get-traces\",\n" + "  \"id\": \"6b221d5bc9e6496c\",\n" + "  \"localEndpoint\": {\n" + "    \"serviceName\": null,\n" + "    \"ipv4\": \"127.0.0.1\",\n" + "    \"ipv6\": null,\n" + "    \"port\": null\n" + "  }\n" + "}";
        assertThat(SpanBytesDecoder.JSON_V2.decodeOne(json.getBytes(UTF_8)).localEndpoint()).isEqualTo(Endpoint.newBuilder().ip("127.0.0.1").build());
    }

    @Test
    public void skipsIncompleteEndpoint() {
        assertThat(SpanBytesDecoder.JSON_V2.decodeOne(("{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"id\": \"6b221d5bc9e6496c\",\n" + "  \"localEndpoint\": {\n" + "    \"serviceName\": null,\n" + "    \"ipv4\": null,\n" + "    \"ipv6\": null,\n" + "    \"port\": null\n" + "  }\n" + "}").getBytes(UTF_8)).localEndpoint()).isNull();
        assertThat(SpanBytesDecoder.JSON_V2.decodeOne(("{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"id\": \"6b221d5bc9e6496c\",\n" + "  \"localEndpoint\": {\n" + "  }\n" + "}").getBytes(UTF_8)).localEndpoint()).isNull();
        assertThat(SpanBytesDecoder.JSON_V2.decodeOne(("{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"id\": \"6b221d5bc9e6496c\",\n" + "  \"remoteEndpoint\": {\n" + "    \"serviceName\": null,\n" + "    \"ipv4\": null,\n" + "    \"ipv6\": null,\n" + "    \"port\": null\n" + "  }\n" + "}").getBytes(UTF_8)).remoteEndpoint()).isNull();
        assertThat(SpanBytesDecoder.JSON_V2.decodeOne(("{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"id\": \"6b221d5bc9e6496c\",\n" + "  \"remoteEndpoint\": {\n" + "  }\n" + "}").getBytes(UTF_8)).remoteEndpoint()).isNull();
    }

    @Test
    public void niceErrorOnIncomplete_annotation() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Incomplete annotation at $.annotations[0].timestamp");
        String json = "{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"name\": \"get-traces\",\n" + "  \"id\": \"6b221d5bc9e6496c\",\n" + "  \"annotations\": [\n" + "    { \"timestamp\": 1472470996199000}\n" + "  ]\n" + "}";
        SpanBytesDecoder.JSON_V2.decodeOne(json.getBytes(UTF_8));
    }

    @Test
    public void niceErrorOnNull_traceId() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Expected a string but was NULL");
        String json = "{\n" + "  \"traceId\": null,\n" + "  \"name\": \"get-traces\",\n" + "  \"id\": \"6b221d5bc9e6496c\"\n" + "}";
        SpanBytesDecoder.JSON_V2.decodeOne(json.getBytes(UTF_8));
    }

    @Test
    public void niceErrorOnNull_id() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Expected a string but was NULL");
        String json = "{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"name\": \"get-traces\",\n" + "  \"id\": null\n" + "}";
        SpanBytesDecoder.JSON_V2.decodeOne(json.getBytes(UTF_8));
    }

    @Test
    public void niceErrorOnNull_tagValue() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("No value at $.tags.foo");
        String json = "{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"name\": \"get-traces\",\n" + "  \"id\": \"6b221d5bc9e6496c\",\n" + "  \"tags\": {\n" + "    \"foo\": NULL\n" + "  }\n" + "}";
        SpanBytesDecoder.JSON_V2.decodeOne(json.getBytes(UTF_8));
    }

    @Test
    public void niceErrorOnNull_annotationValue() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("$.annotations[0].value");
        String json = "{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"name\": \"get-traces\",\n" + "  \"id\": \"6b221d5bc9e6496c\",\n" + "  \"annotations\": [\n" + "    { \"timestamp\": 1472470996199000, \"value\": NULL}\n" + "  ]\n" + "}";
        SpanBytesDecoder.JSON_V2.decodeOne(json.getBytes(UTF_8));
    }

    @Test
    public void niceErrorOnNull_annotationTimestamp() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("$.annotations[0].timestamp");
        String json = "{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"name\": \"get-traces\",\n" + "  \"id\": \"6b221d5bc9e6496c\",\n" + "  \"annotations\": [\n" + "    { \"timestamp\": NULL, \"value\": \"foo\"}\n" + "  ]\n" + "}";
        SpanBytesDecoder.JSON_V2.decodeOne(json.getBytes(UTF_8));
    }

    @Test
    public void readSpan_localEndpoint_noServiceName() {
        String json = "{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"name\": \"get-traces\",\n" + "  \"id\": \"6b221d5bc9e6496c\",\n" + "  \"localEndpoint\": {\n" + "    \"ipv4\": \"127.0.0.1\"\n" + "  }\n" + "}";
        assertThat(SpanBytesDecoder.JSON_V2.decodeOne(json.getBytes(UTF_8)).localServiceName()).isNull();
    }

    @Test
    public void readSpan_remoteEndpoint_noServiceName() {
        String json = "{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"name\": \"get-traces\",\n" + "  \"id\": \"6b221d5bc9e6496c\",\n" + "  \"remoteEndpoint\": {\n" + "    \"ipv4\": \"127.0.0.1\"\n" + "  }\n" + "}";
        assertThat(SpanBytesDecoder.JSON_V2.decodeOne(json.getBytes(UTF_8)).remoteServiceName()).isNull();
    }

        @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    @org.openjdk.jmh.annotations.BenchmarkMode(org.openjdk.jmh.annotations.Mode.Throughput)
    @org.openjdk.jmh.annotations.Warmup(iterations = 10, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Measurement(iterations = 30, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.OutputTimeUnit(java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Fork(value = 1 )
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_niceErrorOnTruncatedSpans_PROTO3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::niceErrorOnTruncatedSpans_PROTO3, this.description("niceErrorOnTruncatedSpans_PROTO3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_niceErrorOnTruncatedSpan_PROTO3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::niceErrorOnTruncatedSpan_PROTO3, this.description("niceErrorOnTruncatedSpan_PROTO3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyListOk_JSON_V1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyListOk_JSON_V1, this.description("emptyListOk_JSON_V1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyListOk_JSON_V2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyListOk_JSON_V2, this.description("emptyListOk_JSON_V2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyListOk_PROTO3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyListOk_PROTO3, this.description("emptyListOk_PROTO3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_JSON_V2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_JSON_V2, this.description("spanRoundTrip_JSON_V2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_PROTO3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_PROTO3, this.description("spanRoundTrip_PROTO3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_localSpanRoundTrip_JSON_V2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::localSpanRoundTrip_JSON_V2, this.description("localSpanRoundTrip_JSON_V2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_localSpanRoundTrip_PROTO3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::localSpanRoundTrip_PROTO3, this.description("localSpanRoundTrip_PROTO3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorSpanRoundTrip_JSON_V2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorSpanRoundTrip_JSON_V2, this.description("errorSpanRoundTrip_JSON_V2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_errorSpanRoundTrip_PROTO3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::errorSpanRoundTrip_PROTO3, this.description("errorSpanRoundTrip_PROTO3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_64bitTraceId_JSON_V2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_64bitTraceId_JSON_V2, this.description("spanRoundTrip_64bitTraceId_JSON_V2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_64bitTraceId_PROTO3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_64bitTraceId_PROTO3, this.description("spanRoundTrip_64bitTraceId_PROTO3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_shared_JSON_V2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_shared_JSON_V2, this.description("spanRoundTrip_shared_JSON_V2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_shared_PROTO3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_shared_PROTO3, this.description("spanRoundTrip_shared_PROTO3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_specialCharsInJson_JSON_V2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::specialCharsInJson_JSON_V2, this.description("specialCharsInJson_JSON_V2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_specialCharsInJson_PROTO3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::specialCharsInJson_PROTO3, this.description("specialCharsInJson_PROTO3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_falseOnEmpty_inputSpans_JSON_V2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::falseOnEmpty_inputSpans_JSON_V2, this.description("falseOnEmpty_inputSpans_JSON_V2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_falseOnEmpty_inputSpans_PROTO3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::falseOnEmpty_inputSpans_PROTO3, this.description("falseOnEmpty_inputSpans_PROTO3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_niceErrorOnMalformed_inputSpans_JSON_V2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::niceErrorOnMalformed_inputSpans_JSON_V2, this.description("niceErrorOnMalformed_inputSpans_JSON_V2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_niceErrorOnMalformed_inputSpans_PROTO3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::niceErrorOnMalformed_inputSpans_PROTO3, this.description("niceErrorOnMalformed_inputSpans_PROTO3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_traceRoundTrip_JSON_V2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::traceRoundTrip_JSON_V2, this.description("traceRoundTrip_JSON_V2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_traceRoundTrip_PROTO3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::traceRoundTrip_PROTO3, this.description("traceRoundTrip_PROTO3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_traceRoundTrip_PROTO3_directBuffer() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::traceRoundTrip_PROTO3_directBuffer, this.description("traceRoundTrip_PROTO3_directBuffer"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_traceRoundTrip_PROTO3_heapBuffer() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::traceRoundTrip_PROTO3_heapBuffer, this.description("traceRoundTrip_PROTO3_heapBuffer"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_traceRoundTrip_PROTO3_heapBufferOffset() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::traceRoundTrip_PROTO3_heapBufferOffset, this.description("traceRoundTrip_PROTO3_heapBufferOffset"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spansRoundTrip_JSON_V2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spansRoundTrip_JSON_V2, this.description("spansRoundTrip_JSON_V2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spansRoundTrip_PROTO3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spansRoundTrip_PROTO3, this.description("spansRoundTrip_PROTO3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_noRemoteServiceName_JSON_V2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_noRemoteServiceName_JSON_V2, this.description("spanRoundTrip_noRemoteServiceName_JSON_V2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_noRemoteServiceName_PROTO3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_noRemoteServiceName_PROTO3, this.description("spanRoundTrip_noRemoteServiceName_PROTO3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_noAnnotations_rootServerSpan_JSON_V2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_noAnnotations_rootServerSpan_JSON_V2, this.description("spanRoundTrip_noAnnotations_rootServerSpan_JSON_V2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_noAnnotations_rootServerSpan_PROTO3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_noAnnotations_rootServerSpan_PROTO3, this.description("spanRoundTrip_noAnnotations_rootServerSpan_PROTO3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_noAnnotations_rootServerSpan_incomplete_JSON_V2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_noAnnotations_rootServerSpan_incomplete_JSON_V2, this.description("spanRoundTrip_noAnnotations_rootServerSpan_incomplete_JSON_V2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_noAnnotations_rootServerSpan_incomplete_PROTO3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_noAnnotations_rootServerSpan_incomplete_PROTO3, this.description("spanRoundTrip_noAnnotations_rootServerSpan_incomplete_PROTO3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_noAnnotations_rootServerSpan_shared_JSON_V2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_noAnnotations_rootServerSpan_shared_JSON_V2, this.description("spanRoundTrip_noAnnotations_rootServerSpan_shared_JSON_V2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_noAnnotations_rootServerSpan_shared_PROTO3() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_noAnnotations_rootServerSpan_shared_PROTO3, this.description("spanRoundTrip_noAnnotations_rootServerSpan_shared_PROTO3"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_niceErrorOnUppercase_traceId_JSON_V2() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::niceErrorOnUppercase_traceId_JSON_V2, this.description("niceErrorOnUppercase_traceId_JSON_V2"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_readsTraceIdHighFromTraceIdField() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::readsTraceIdHighFromTraceIdField, this.description("readsTraceIdHighFromTraceIdField"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ignoresNull_topLevelFields() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ignoresNull_topLevelFields, this.description("ignoresNull_topLevelFields"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ignoresNull_endpoint_topLevelFields() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ignoresNull_endpoint_topLevelFields, this.description("ignoresNull_endpoint_topLevelFields"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_skipsIncompleteEndpoint() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::skipsIncompleteEndpoint, this.description("skipsIncompleteEndpoint"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_niceErrorOnIncomplete_annotation() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::niceErrorOnIncomplete_annotation, this.description("niceErrorOnIncomplete_annotation"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_niceErrorOnNull_traceId() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::niceErrorOnNull_traceId, this.description("niceErrorOnNull_traceId"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_niceErrorOnNull_id() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::niceErrorOnNull_id, this.description("niceErrorOnNull_id"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_niceErrorOnNull_tagValue() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::niceErrorOnNull_tagValue, this.description("niceErrorOnNull_tagValue"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_niceErrorOnNull_annotationValue() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::niceErrorOnNull_annotationValue, this.description("niceErrorOnNull_annotationValue"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_niceErrorOnNull_annotationTimestamp() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::niceErrorOnNull_annotationTimestamp, this.description("niceErrorOnNull_annotationTimestamp"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_readSpan_localEndpoint_noServiceName() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::readSpan_localEndpoint_noServiceName, this.description("readSpan_localEndpoint_noServiceName"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_readSpan_remoteEndpoint_noServiceName() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::readSpan_remoteEndpoint_noServiceName, this.description("readSpan_remoteEndpoint_noServiceName"));
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().thrown, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private SpanBytesDecoderTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new SpanBytesDecoderTest();
        }

        @java.lang.Override
        public SpanBytesDecoderTest implementation() {
            return this.implementation;
        }
    }
}
