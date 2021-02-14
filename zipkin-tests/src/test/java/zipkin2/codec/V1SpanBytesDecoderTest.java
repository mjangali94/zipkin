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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import zipkin2.Endpoint;
import zipkin2.Span;
import static org.assertj.core.api.Assertions.assertThat;
import static zipkin2.TestObjects.BACKEND;
import static zipkin2.TestObjects.TRACE;
import static zipkin2.codec.SpanBytesEncoderTest.LOCAL_SPAN;
import static zipkin2.codec.SpanBytesEncoderTest.NO_ANNOTATIONS_ROOT_SERVER_SPAN;
import static zipkin2.codec.SpanBytesEncoderTest.SPAN;
import static zipkin2.codec.SpanBytesEncoderTest.UTF8_SPAN;
import static zipkin2.codec.SpanBytesEncoderTest.UTF_8;

/**
 * V1 tests for {@link SpanBytesDecoderTest}
 */
public class V1SpanBytesDecoderTest {

    Span span = SPAN;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void niceErrorOnTruncatedSpans_THRIFT() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Truncated: length 8 > bytes available 2 reading List<Span> from TBinary");
        byte[] encoded = SpanBytesEncoder.THRIFT.encodeList(TRACE);
        SpanBytesDecoder.THRIFT.decodeList(Arrays.copyOfRange(encoded, 0, 10));
    }

    @Test
    public void niceErrorOnTruncatedSpan_THRIFT() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Truncated: length 8 > bytes available 7 reading Span from TBinary");
        byte[] encoded = SpanBytesEncoder.THRIFT.encode(SPAN);
        SpanBytesDecoder.THRIFT.decodeOne(Arrays.copyOfRange(encoded, 0, 10));
    }

    @Test
    public void emptyListOk_THRIFT() {
        assertThat(SpanBytesDecoder.THRIFT.decodeList(new byte[0])).isEmpty();
        byte[] emptyListLiteral = { 12, /* TYPE_STRUCT */
        0, 0, 0, 0 /* zero length */
        };
        assertThat(SpanBytesDecoder.THRIFT.decodeList(emptyListLiteral)).isEmpty();
    }

    @Test
    public void spanRoundTrip_JSON_V1() {
        assertThat(SpanBytesDecoder.JSON_V1.decodeOne(SpanBytesEncoder.JSON_V1.encode(span))).isEqualTo(span);
    }

    @Test
    public void spanRoundTrip_THRIFT() {
        assertThat(SpanBytesDecoder.THRIFT.decodeOne(SpanBytesEncoder.THRIFT.encode(span))).isEqualTo(span);
    }

    @Test
    public void localSpanRoundTrip_JSON_V1() {
        assertThat(SpanBytesDecoder.JSON_V1.decodeOne(SpanBytesEncoder.JSON_V1.encode(LOCAL_SPAN))).isEqualTo(LOCAL_SPAN);
    }

    @Test
    public void localSpanRoundTrip_THRIFT() {
        assertThat(SpanBytesDecoder.THRIFT.decodeOne(SpanBytesEncoder.THRIFT.encode(LOCAL_SPAN))).isEqualTo(LOCAL_SPAN);
    }

    @Test
    public void spanRoundTrip_64bitTraceId_JSON_V1() {
        span = span.toBuilder().traceId(span.traceId().substring(16)).build();
        assertThat(SpanBytesDecoder.JSON_V1.decodeOne(SpanBytesEncoder.JSON_V1.encode(span))).isEqualTo(span);
    }

    @Test
    public void spanRoundTrip_64bitTraceId_THRIFT() {
        span = span.toBuilder().traceId(span.traceId().substring(16)).build();
        assertThat(SpanBytesDecoder.THRIFT.decodeOne(SpanBytesEncoder.THRIFT.encode(span))).isEqualTo(span);
    }

    @Test
    public void spanRoundTrip_shared_JSON_V1() {
        span = span.toBuilder().kind(Span.Kind.SERVER).shared(true).build();
        assertThat(SpanBytesDecoder.JSON_V1.decodeOne(SpanBytesEncoder.JSON_V1.encode(span))).isEqualTo(span);
    }

    @Test
    public void spanRoundTrip_shared_THRIFT() {
        span = span.toBuilder().kind(Span.Kind.SERVER).shared(true).build();
        assertThat(SpanBytesDecoder.THRIFT.decodeOne(SpanBytesEncoder.THRIFT.encode(span))).isEqualTo(span);
    }

    /**
     * This isn't a test of what we "should" accept as a span, rather that characters that trip-up
     * json don't fail in codec.
     */
    @Test
    public void specialCharsInJson_JSON_V1() {
        assertThat(SpanBytesDecoder.JSON_V1.decodeOne(SpanBytesEncoder.JSON_V1.encode(UTF8_SPAN))).isEqualTo(UTF8_SPAN);
    }

    @Test
    public void specialCharsInJson_THRIFT() {
        assertThat(SpanBytesDecoder.THRIFT.decodeOne(SpanBytesEncoder.THRIFT.encode(UTF8_SPAN))).isEqualTo(UTF8_SPAN);
    }

    @Test
    public void falseOnEmpty_inputSpans_JSON_V1() {
        assertThat(SpanBytesDecoder.JSON_V1.decodeList(new byte[0], new ArrayList<>())).isFalse();
    }

    @Test
    public void falseOnEmpty_inputSpans_THRIFT() {
        assertThat(SpanBytesDecoder.THRIFT.decodeList(new byte[0], new ArrayList<>())).isFalse();
    }

    /**
     * Particulary, thrift can mistake malformed content as a huge list. Let's not blow up.
     */
    @Test
    public void niceErrorOnMalformed_inputSpans_JSON_V1() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Malformed reading List<Span> from ");
        SpanBytesDecoder.JSON_V1.decodeList(new byte[] { 'h', 'e', 'l', 'l', 'o' });
    }

    @Test
    public void niceErrorOnMalformed_inputSpans_THRIFT() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Truncated: length 1 > bytes available 0 reading List<Span> from TBinary");
        SpanBytesDecoder.THRIFT.decodeList(new byte[] { 'h', 'e', 'l', 'l', 'o' });
    }

    @Test
    public void traceRoundTrip_JSON_V1() {
        byte[] message = SpanBytesEncoder.JSON_V1.encodeList(TRACE);
        assertThat(SpanBytesDecoder.JSON_V1.decodeList(message)).isEqualTo(TRACE);
    }

    @Test
    public void traceRoundTrip_THRIFT() {
        byte[] message = SpanBytesEncoder.THRIFT.encodeList(TRACE);
        assertThat(SpanBytesDecoder.THRIFT.decodeList(message)).isEqualTo(TRACE);
    }

    @Test
    public void spansRoundTrip_JSON_V1() {
        List<Span> tenClientSpans = Collections.nCopies(10, span);
        byte[] message = SpanBytesEncoder.JSON_V1.encodeList(tenClientSpans);
        assertThat(SpanBytesDecoder.JSON_V1.decodeList(message)).isEqualTo(tenClientSpans);
    }

    @Test
    public void spansRoundTrip_THRIFT() {
        List<Span> tenClientSpans = Collections.nCopies(10, span);
        byte[] message = SpanBytesEncoder.THRIFT.encodeList(tenClientSpans);
        assertThat(SpanBytesDecoder.THRIFT.decodeList(message)).isEqualTo(tenClientSpans);
    }

    @Test
    public void spanRoundTrip_noRemoteServiceName_JSON_V1() {
        span = span.toBuilder().remoteEndpoint(BACKEND.toBuilder().serviceName(null).build()).build();
        assertThat(SpanBytesDecoder.JSON_V1.decodeOne(SpanBytesEncoder.JSON_V1.encode(span))).isEqualTo(span);
    }

    @Test
    public void spanRoundTrip_noRemoteServiceName_THRIFT() {
        span = span.toBuilder().remoteEndpoint(BACKEND.toBuilder().serviceName(null).build()).build();
        assertThat(SpanBytesDecoder.THRIFT.decodeOne(SpanBytesEncoder.THRIFT.encode(span))).isEqualTo(span);
    }

    @Test
    public void spanRoundTrip_noAnnotations_rootServerSpan_JSON_V1() {
        span = NO_ANNOTATIONS_ROOT_SERVER_SPAN;
        assertThat(SpanBytesDecoder.JSON_V1.decodeOne(SpanBytesEncoder.JSON_V1.encode(span))).isEqualTo(span);
    }

    @Test
    public void spanRoundTrip_noAnnotations_rootServerSpan_THRIFT() {
        span = NO_ANNOTATIONS_ROOT_SERVER_SPAN;
        assertThat(SpanBytesDecoder.THRIFT.decodeOne(SpanBytesEncoder.THRIFT.encode(span))).isEqualTo(span);
    }

    @Test
    public void spanRoundTrip_noAnnotations_rootServerSpan_incomplete_JSON_V1() {
        span = NO_ANNOTATIONS_ROOT_SERVER_SPAN.toBuilder().duration(null).build();
        assertThat(SpanBytesDecoder.JSON_V1.decodeOne(SpanBytesEncoder.JSON_V1.encode(span))).isEqualTo(span);
    }

    @Test
    public void spanRoundTrip_noAnnotations_rootServerSpan_incomplete_THRIFT() {
        span = NO_ANNOTATIONS_ROOT_SERVER_SPAN.toBuilder().duration(null).build();
        assertThat(SpanBytesDecoder.THRIFT.decodeOne(SpanBytesEncoder.THRIFT.encode(span))).isEqualTo(span);
    }

    @Test
    public void spanRoundTrip_noAnnotations_rootServerSpan_shared_JSON_V1() {
        span = NO_ANNOTATIONS_ROOT_SERVER_SPAN.toBuilder().shared(true).build();
        assertThat(SpanBytesDecoder.JSON_V1.decodeOne(SpanBytesEncoder.JSON_V1.encode(span))).isEqualTo(span);
    }

    @Test
    public void spanRoundTrip_noAnnotations_rootServerSpan_shared_THRIFT() {
        span = NO_ANNOTATIONS_ROOT_SERVER_SPAN.toBuilder().shared(true).build();
        assertThat(SpanBytesDecoder.THRIFT.decodeOne(SpanBytesEncoder.THRIFT.encode(span))).isEqualTo(span);
    }

    @Test
    @Ignore
    public void niceErrorOnUppercase_traceId_JSON_V1() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("48485A3953BB6124 should be lower-hex encoded with no prefix");
        String json = "{\n" + "  \"traceId\": \"48485A3953BB6124\",\n" + "  \"name\": \"get-traces\",\n" + "  \"id\": \"6b221d5bc9e6496c\"\n" + "}";
        SpanBytesDecoder.JSON_V1.decodeOne(json.getBytes(UTF_8));
    }

    @Test
    public void readsTraceIdHighFromTraceIdField() {
        byte[] with128BitTraceId = ("{\n" + "  \"traceId\": \"48485a3953bb61246b221d5bc9e6496c\",\n" + "  \"name\": \"get-traces\",\n" + "  \"id\": \"6b221d5bc9e6496c\"\n" + "}").getBytes(UTF_8);
        byte[] withLower64bitsTraceId = ("{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"name\": \"get-traces\",\n" + "  \"id\": \"6b221d5bc9e6496c\"\n" + "}").getBytes(UTF_8);
        assertThat(SpanBytesDecoder.JSON_V1.decodeOne(with128BitTraceId)).isEqualTo(SpanBytesDecoder.JSON_V1.decodeOne(withLower64bitsTraceId).toBuilder().traceId("48485a3953bb61246b221d5bc9e6496c").build());
    }

    @Test
    public void ignoresNull_topLevelFields() {
        String json = "{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"parentId\": null,\n" + "  \"id\": \"6b221d5bc9e6496c\",\n" + "  \"name\": null,\n" + "  \"timestamp\": null,\n" + "  \"duration\": null,\n" + "  \"annotations\": null,\n" + "  \"binaryAnnotations\": null,\n" + "  \"debug\": null,\n" + "  \"shared\": null\n" + "}";
        SpanBytesDecoder.JSON_V1.decodeOne(json.getBytes(UTF_8));
    }

    @Test
    public void ignoresNull_endpoint_topLevelFields() {
        String json = "{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"id\": \"6b221d5bc9e6496c\",\n" + "  \"binaryAnnotations\": [\n" + "    {\n" + "      \"key\": \"lc\",\n" + "      \"value\": \"\",\n" + "      \"endpoint\": {\n" + "        \"serviceName\": null,\n" + "    \"ipv4\": \"127.0.0.1\",\n" + "        \"ipv6\": null,\n" + "        \"port\": null\n" + "      }\n" + "    }\n" + "  ]\n" + "}";
        assertThat(SpanBytesDecoder.JSON_V1.decodeOne(json.getBytes(UTF_8)).localEndpoint()).isEqualTo(Endpoint.newBuilder().ip("127.0.0.1").build());
    }

    @Test
    public void skipsIncompleteEndpoint() {
        String json = "{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"id\": \"6b221d5bc9e6496c\",\n" + "  \"binaryAnnotations\": [\n" + "    {\n" + "      \"key\": \"lc\",\n" + "      \"value\": \"\",\n" + "      \"endpoint\": {\n" + "        \"serviceName\": null,\n" + "        \"ipv4\": null,\n" + "        \"ipv6\": null,\n" + "        \"port\": null\n" + "      }\n" + "    }\n" + "  ]\n" + "}";
        assertThat(SpanBytesDecoder.JSON_V1.decodeOne(json.getBytes(UTF_8)).localEndpoint()).isNull();
        json = "{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"id\": \"6b221d5bc9e6496c\",\n" + "  \"binaryAnnotations\": [\n" + "    {\n" + "      \"key\": \"lc\",\n" + "      \"value\": \"\",\n" + "      \"endpoint\": {\n" + "      }\n" + "    }\n" + "  ]\n" + "}";
        assertThat(SpanBytesDecoder.JSON_V1.decodeOne(json.getBytes(UTF_8)).localEndpoint()).isNull();
    }

    @Test
    public void ignoresNonAddressBooleanBinaryAnnotations() {
        String json = "{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"id\": \"6b221d5bc9e6496c\",\n" + "  \"binaryAnnotations\": [\n" + "    {\n" + "      \"key\": \"aa\",\n" + "      \"value\": true,\n" + "      \"endpoint\": {\n" + "        \"serviceName\": \"foo\"\n" + "      }\n" + "    }\n" + "  ]\n" + "}";
        Span decoded = SpanBytesDecoder.JSON_V1.decodeOne(json.getBytes(UTF_8));
        assertThat(decoded.tags()).isEmpty();
        assertThat(decoded.localEndpoint()).isNull();
        assertThat(decoded.remoteEndpoint()).isNull();
    }

    @Test
    public void niceErrorOnIncomplete_annotation() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Incomplete annotation at $.annotations[0].timestamp");
        String json = "{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"name\": \"get-traces\",\n" + "  \"id\": \"6b221d5bc9e6496c\",\n" + "  \"annotations\": [\n" + "    { \"timestamp\": 1472470996199000}\n" + "  ]\n" + "}";
        SpanBytesDecoder.JSON_V1.decodeOne(json.getBytes(UTF_8));
    }

    @Test
    public void niceErrorOnNull_traceId() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Expected a string but was NULL");
        String json = "{\n" + "  \"traceId\": null,\n" + "  \"name\": \"get-traces\",\n" + "  \"id\": \"6b221d5bc9e6496c\"\n" + "}";
        SpanBytesDecoder.JSON_V1.decodeOne(json.getBytes(UTF_8));
    }

    @Test
    public void niceErrorOnNull_id() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Expected a string but was NULL");
        String json = "{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"name\": \"get-traces\",\n" + "  \"id\": null\n" + "}";
        SpanBytesDecoder.JSON_V1.decodeOne(json.getBytes(UTF_8));
    }

    @Test
    public void niceErrorOnNull_annotationValue() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("$.annotations[0].value");
        String json = "{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"name\": \"get-traces\",\n" + "  \"id\": \"6b221d5bc9e6496c\",\n" + "  \"annotations\": [\n" + "    { \"timestamp\": 1472470996199000, \"value\": NULL}\n" + "  ]\n" + "}";
        SpanBytesDecoder.JSON_V1.decodeOne(json.getBytes(UTF_8));
    }

    @Test
    public void niceErrorOnNull_annotationTimestamp() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("$.annotations[0].timestamp");
        String json = "{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"name\": \"get-traces\",\n" + "  \"id\": \"6b221d5bc9e6496c\",\n" + "  \"annotations\": [\n" + "    { \"timestamp\": NULL, \"value\": \"foo\"}\n" + "  ]\n" + "}";
        SpanBytesDecoder.JSON_V1.decodeOne(json.getBytes(UTF_8));
    }

    @Test
    public void readSpan_localEndpoint_noServiceName() {
        String json = "{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"name\": \"get-traces\",\n" + "  \"id\": \"6b221d5bc9e6496c\",\n" + "  \"localEndpoint\": {\n" + "    \"ipv4\": \"127.0.0.1\"\n" + "  }\n" + "}";
        assertThat(SpanBytesDecoder.JSON_V1.decodeOne(json.getBytes(UTF_8)).localServiceName()).isNull();
    }

    @Test
    public void readSpan_remoteEndpoint_noServiceName() {
        String json = "{\n" + "  \"traceId\": \"6b221d5bc9e6496c\",\n" + "  \"name\": \"get-traces\",\n" + "  \"id\": \"6b221d5bc9e6496c\",\n" + "  \"remoteEndpoint\": {\n" + "    \"ipv4\": \"127.0.0.1\"\n" + "  }\n" + "}";
        assertThat(SpanBytesDecoder.JSON_V1.decodeOne(json.getBytes(UTF_8)).remoteServiceName()).isNull();
    }

        @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    @org.openjdk.jmh.annotations.BenchmarkMode(org.openjdk.jmh.annotations.Mode.Throughput)
    @org.openjdk.jmh.annotations.Warmup(iterations = 10, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Measurement(iterations = 30, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.OutputTimeUnit(java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Fork(value = 1 )
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_niceErrorOnTruncatedSpans_THRIFT() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::niceErrorOnTruncatedSpans_THRIFT, this.description("niceErrorOnTruncatedSpans_THRIFT"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_niceErrorOnTruncatedSpan_THRIFT() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::niceErrorOnTruncatedSpan_THRIFT, this.description("niceErrorOnTruncatedSpan_THRIFT"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_emptyListOk_THRIFT() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::emptyListOk_THRIFT, this.description("emptyListOk_THRIFT"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_JSON_V1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_JSON_V1, this.description("spanRoundTrip_JSON_V1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_THRIFT() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_THRIFT, this.description("spanRoundTrip_THRIFT"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_localSpanRoundTrip_JSON_V1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::localSpanRoundTrip_JSON_V1, this.description("localSpanRoundTrip_JSON_V1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_localSpanRoundTrip_THRIFT() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::localSpanRoundTrip_THRIFT, this.description("localSpanRoundTrip_THRIFT"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_64bitTraceId_JSON_V1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_64bitTraceId_JSON_V1, this.description("spanRoundTrip_64bitTraceId_JSON_V1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_64bitTraceId_THRIFT() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_64bitTraceId_THRIFT, this.description("spanRoundTrip_64bitTraceId_THRIFT"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_shared_JSON_V1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_shared_JSON_V1, this.description("spanRoundTrip_shared_JSON_V1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_shared_THRIFT() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_shared_THRIFT, this.description("spanRoundTrip_shared_THRIFT"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_specialCharsInJson_JSON_V1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::specialCharsInJson_JSON_V1, this.description("specialCharsInJson_JSON_V1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_specialCharsInJson_THRIFT() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::specialCharsInJson_THRIFT, this.description("specialCharsInJson_THRIFT"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_falseOnEmpty_inputSpans_JSON_V1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::falseOnEmpty_inputSpans_JSON_V1, this.description("falseOnEmpty_inputSpans_JSON_V1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_falseOnEmpty_inputSpans_THRIFT() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::falseOnEmpty_inputSpans_THRIFT, this.description("falseOnEmpty_inputSpans_THRIFT"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_niceErrorOnMalformed_inputSpans_JSON_V1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::niceErrorOnMalformed_inputSpans_JSON_V1, this.description("niceErrorOnMalformed_inputSpans_JSON_V1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_niceErrorOnMalformed_inputSpans_THRIFT() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::niceErrorOnMalformed_inputSpans_THRIFT, this.description("niceErrorOnMalformed_inputSpans_THRIFT"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_traceRoundTrip_JSON_V1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::traceRoundTrip_JSON_V1, this.description("traceRoundTrip_JSON_V1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_traceRoundTrip_THRIFT() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::traceRoundTrip_THRIFT, this.description("traceRoundTrip_THRIFT"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spansRoundTrip_JSON_V1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spansRoundTrip_JSON_V1, this.description("spansRoundTrip_JSON_V1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spansRoundTrip_THRIFT() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spansRoundTrip_THRIFT, this.description("spansRoundTrip_THRIFT"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_noRemoteServiceName_JSON_V1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_noRemoteServiceName_JSON_V1, this.description("spanRoundTrip_noRemoteServiceName_JSON_V1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_noRemoteServiceName_THRIFT() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_noRemoteServiceName_THRIFT, this.description("spanRoundTrip_noRemoteServiceName_THRIFT"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_noAnnotations_rootServerSpan_JSON_V1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_noAnnotations_rootServerSpan_JSON_V1, this.description("spanRoundTrip_noAnnotations_rootServerSpan_JSON_V1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_noAnnotations_rootServerSpan_THRIFT() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_noAnnotations_rootServerSpan_THRIFT, this.description("spanRoundTrip_noAnnotations_rootServerSpan_THRIFT"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_noAnnotations_rootServerSpan_incomplete_JSON_V1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_noAnnotations_rootServerSpan_incomplete_JSON_V1, this.description("spanRoundTrip_noAnnotations_rootServerSpan_incomplete_JSON_V1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_noAnnotations_rootServerSpan_incomplete_THRIFT() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_noAnnotations_rootServerSpan_incomplete_THRIFT, this.description("spanRoundTrip_noAnnotations_rootServerSpan_incomplete_THRIFT"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_noAnnotations_rootServerSpan_shared_JSON_V1() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_noAnnotations_rootServerSpan_shared_JSON_V1, this.description("spanRoundTrip_noAnnotations_rootServerSpan_shared_JSON_V1"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanRoundTrip_noAnnotations_rootServerSpan_shared_THRIFT() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanRoundTrip_noAnnotations_rootServerSpan_shared_THRIFT, this.description("spanRoundTrip_noAnnotations_rootServerSpan_shared_THRIFT"));
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
        public void benchmark_ignoresNonAddressBooleanBinaryAnnotations() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ignoresNonAddressBooleanBinaryAnnotations, this.description("ignoresNonAddressBooleanBinaryAnnotations"));
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

        private V1SpanBytesDecoderTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new V1SpanBytesDecoderTest();
        }

        @java.lang.Override
        public V1SpanBytesDecoderTest implementation() {
            return this.implementation;
        }
    }
}
