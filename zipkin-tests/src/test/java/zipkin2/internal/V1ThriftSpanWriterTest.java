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

import org.junit.Before;
import org.junit.Test;
import zipkin2.Endpoint;
import zipkin2.Span;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static zipkin2.Span.Kind.CLIENT;
import static zipkin2.Span.Kind.CONSUMER;
import static zipkin2.Span.Kind.PRODUCER;
import static zipkin2.Span.Kind.SERVER;
import static zipkin2.internal.ThriftField.TYPE_I16;
import static zipkin2.internal.ThriftField.TYPE_I32;
import static zipkin2.internal.ThriftField.TYPE_I64;
import static zipkin2.internal.ThriftField.TYPE_LIST;
import static zipkin2.internal.ThriftField.TYPE_STRING;
import static zipkin2.internal.ThriftField.TYPE_STRUCT;

public class V1ThriftSpanWriterTest {

    Span span = Span.newBuilder().traceId("1").id("2").build();

    Endpoint endpoint = Endpoint.newBuilder().serviceName("frontend").ip("1.2.3.4").build();

    // bigger than needed to test sizeOf
    byte[] bytes = new byte[2048];

    WriteBuffer buf = WriteBuffer.wrap(bytes);

    V1ThriftSpanWriter writer = new V1ThriftSpanWriter();

    byte[] endpointBytes = new byte[ThriftEndpointCodec.sizeInBytes(endpoint)];

    @Before
    public void init() {
        ThriftEndpointCodec.write(endpoint, WriteBuffer.wrap(endpointBytes, 0));
    }

    @Test
    public void endpoint_highPort() {
        int highPort = 63840;
        Endpoint endpoint = Endpoint.newBuilder().ip("127.0.0.1").port(63840).build();
        byte[] buff = new byte[ThriftEndpointCodec.sizeInBytes(endpoint)];
        ThriftEndpointCodec.write(endpoint, WriteBuffer.wrap(buff, 0));
        assertThat(buff).containsSequence(TYPE_I32, 0, 1, 127, 0, 0, // ipv4
        1).containsSequence(TYPE_I16, 0, 2, (highPort >> 8) & 0xFF, // port
        highPort & 0xFF);
        assertThat(ThriftEndpointCodec.read(ReadBuffer.wrap(buff)).portAsInt()).isEqualTo(highPort);
    }

    @Test
    public void write_startsWithI64Prefix() {
        byte[] buff = writer.write(span);
        assertThat(buff).hasSize(writer.sizeInBytes(span)).startsWith(TYPE_I64, 0, // short value of field number 1
        1);
    }

    @Test
    public void writeList_startsWithListPrefix() {
        byte[] buff = writer.writeList(asList(span));
        assertThat(buff).hasSize(5 + writer.sizeInBytes(span)).startsWith(TYPE_STRUCT, 0, 0, 0, 1);
    }

    @Test
    public void writeList_startsWithListPrefix_multiple() {
        byte[] buff = writer.writeList(asList(span, span));
        assertThat(buff).hasSize(5 + writer.sizeInBytes(span) * 2).startsWith(TYPE_STRUCT, 0, 0, 0, 2);
    }

    @Test
    public void writeList_empty() {
        assertThat(writer.writeList(asList())).isEmpty();
    }

    @Test
    public void writeList_offset_startsWithListPrefix() {
        writer.writeList(asList(span, span), bytes, 1);
        assertThat(bytes).startsWith(0, TYPE_STRUCT, 0, 0, 0, 2);
    }

    @Test
    public void doesntWriteAnnotationsWhenMissingTimestamp() {
        writer.write(span.toBuilder().kind(CLIENT).build(), buf);
        byte[] bytes2 = new byte[2048];
        writer.write(span, WriteBuffer.wrap(bytes2));
        assertThat(bytes).containsExactly(bytes2);
    }

    @Test
    public void writesCoreAnnotations_client_noEndpoint() {
        writesCoreAnnotationsNoEndpoint(CLIENT, "cs", "cr");
    }

    @Test
    public void writesCoreAnnotations_server_noEndpoint() {
        writesCoreAnnotationsNoEndpoint(SERVER, "sr", "ss");
    }

    @Test
    public void writesCoreAnnotations_producer_noEndpoint() {
        writesCoreAnnotationsNoEndpoint(PRODUCER, "ms", "ws");
    }

    @Test
    public void writesCoreAnnotations_consumer_noEndpoint() {
        writesCoreAnnotationsNoEndpoint(CONSUMER, "wr", "mr");
    }

    void writesCoreAnnotationsNoEndpoint(Span.Kind kind, String begin, String end) {
        span = span.toBuilder().kind(kind).timestamp(5).duration(10).build();
        writer.write(span, buf);
        assertThat(bytes).containsSequence(TYPE_LIST, 0, 6, TYPE_STRUCT, 0, 0, 0, // two annotations
        2).containsSequence(TYPE_I64, 0, 1, 0, 0, 0, 0, 0, 0, 0, // timestamp
        5).containsSequence(TYPE_STRING, 0, 2, 0, 0, 0, 2, begin.charAt(0), begin.charAt(1)).containsSequence(TYPE_I64, 0, 1, 0, 0, 0, 0, 0, 0, 0, // timestamp
        15).containsSequence(TYPE_STRING, 0, 2, 0, 0, 0, 2, end.charAt(0), end.charAt(1));
    }

    @Test
    public void writesBeginAnnotation_client_noEndpoint() {
        writesBeginAnnotationNoEndpoint(CLIENT, "cs");
    }

    @Test
    public void writesBeginAnnotation_server_noEndpoint() {
        writesBeginAnnotationNoEndpoint(SERVER, "sr");
    }

    @Test
    public void writesBeginAnnotation_producer_noEndpoint() {
        writesBeginAnnotationNoEndpoint(PRODUCER, "ms");
    }

    @Test
    public void writesBeginAnnotation_consumer_noEndpoint() {
        writesBeginAnnotationNoEndpoint(CONSUMER, "mr");
    }

    void writesBeginAnnotationNoEndpoint(Span.Kind kind, String begin) {
        span = span.toBuilder().kind(kind).timestamp(5).build();
        writer.write(span, buf);
        assertThat(bytes).containsSequence(TYPE_LIST, 0, 6, TYPE_STRUCT, 0, 0, 0, // one annotation
        1).containsSequence(TYPE_I64, 0, 1, 0, 0, 0, 0, 0, 0, 0, // timestamp
        5).containsSequence(TYPE_STRING, 0, 2, 0, 0, 0, 2, begin.charAt(0), begin.charAt(1));
    }

    @Test
    public void writesAddressBinaryAnnotation_client() {
        writesAddressBinaryAnnotation(CLIENT, "sa");
    }

    @Test
    public void writesAddressBinaryAnnotation_server() {
        writesAddressBinaryAnnotation(SERVER, "ca");
    }

    @Test
    public void writesAddressBinaryAnnotation_producer() {
        writesAddressBinaryAnnotation(PRODUCER, "ma");
    }

    @Test
    public void writesAddressBinaryAnnotation_consumer() {
        writesAddressBinaryAnnotation(CONSUMER, "ma");
    }

    void writesAddressBinaryAnnotation(Span.Kind kind, String addr) {
        writer.write(span.toBuilder().kind(kind).remoteEndpoint(endpoint).build(), buf);
        assertThat(bytes).containsSequence(TYPE_LIST, 0, 8, TYPE_STRUCT, 0, 0, 0, // one binary annotation
        1).containsSequence(TYPE_STRING, 0, 1, 0, 0, 0, 2, addr.charAt(0), // key
        addr.charAt(1)).containsSequence(TYPE_STRING, 0, 2, 0, 0, 0, 1, // value
        1).containsSequence(TYPE_I32, 0, 3, 0, 0, 0, // type 0 == boolean
        0).containsSequence(endpointBytes);
    }

    @Test
    public void annotationsHaveEndpoints() {
        writer.write(span.toBuilder().localEndpoint(endpoint).addAnnotation(5, "foo").build(), buf);
        assertThat(bytes).containsSequence(TYPE_LIST, 0, 6, TYPE_STRUCT, 0, 0, 0, // one annotation
        1).containsSequence(TYPE_I64, 0, 1, 0, 0, 0, 0, 0, 0, 0, // timestamp
        5).containsSequence(TYPE_STRING, 0, 2, 0, 0, 0, 3, 'f', 'o', // value
        'o').containsSequence(endpointBytes);
    }

    @Test
    public void writesTimestampAndDuration() {
        writer.write(span.toBuilder().timestamp(5).duration(10).build(), buf);
        assertThat(bytes).containsSequence(TYPE_I64, 0, 10, 0, 0, 0, 0, 0, 0, 0, // timestamp
        5).containsSequence(TYPE_I64, 0, 11, 0, 0, 0, 0, 0, 0, 0, // duration
        10);
    }

    @Test
    public void writesEmptySpanName() {
        Span span = Span.newBuilder().traceId("1").id("2").build();
        writer.write(span, buf);
        assertThat(bytes).containsSequence(ThriftField.TYPE_STRING, 0, 3, 0, 0, 0, // name (empty is 32 zero bits)
        0);
    }

    @Test
    public void writesTraceAndSpanIds() {
        writer.write(span, buf);
        assertThat(bytes).startsWith(TYPE_I64, 0, 1, 0, 0, 0, 0, 0, 0, 0, // trace ID
        1).containsSequence(TYPE_I64, 0, 4, 0, 0, 0, 0, 0, 0, 0, // ID
        2);
    }

    @Test
    public void writesParentAnd128BitTraceId() {
        writer.write(Span.newBuilder().traceId("00000000000000010000000000000002").parentId("3").id("4").build(), buf);
        assertThat(bytes).startsWith(TYPE_I64, 0, 1, 0, 0, 0, 0, 0, 0, 0, // trace ID
        2).containsSequence(TYPE_I64, 0, 12, 0, 0, 0, 0, 0, 0, 0, // trace ID high
        1).containsSequence(TYPE_I64, 0, 5, 0, 0, 0, 0, 0, 0, 0, // parent ID
        3);
    }

    /**
     * For finagle compatibility
     */
    @Test
    public void writesEmptyAnnotationAndBinaryAnnotations() {
        Span span = Span.newBuilder().traceId("1").id("2").build();
        writer.write(span, buf);
        assertThat(bytes).containsSequence(TYPE_LIST, 0, 6, TYPE_STRUCT, 0, 0, 0, // empty annotations
        0).containsSequence(TYPE_LIST, 0, 8, TYPE_STRUCT, 0, 0, 0, // empty binary annotations
        0);
    }

    @Test
    public void writesEmptyLocalComponentWhenNoAnnotationsOrTags() {
        span = span.toBuilder().name("foo").localEndpoint(endpoint).build();
        writer.write(span, buf);
        assertThat(bytes).containsSequence(TYPE_LIST, 0, 8, TYPE_STRUCT, 0, 0, 0, // one binary annotation
        1).containsSequence(TYPE_STRING, 0, 1, 0, 0, 0, 2, 'l', // key
        'c').containsSequence(TYPE_STRING, 0, 2, 0, 0, 0, // empty value
        0).containsSequence(TYPE_I32, 0, 3, 0, 0, 0, // type 6 == string
        6).containsSequence(endpointBytes);
    }

    @Test
    public void writesEmptyServiceName() {
        span = span.toBuilder().name("foo").localEndpoint(Endpoint.newBuilder().ip("127.0.0.1").build()).build();
        writer.write(span, buf);
        assertThat(bytes).containsSequence(ThriftField.TYPE_STRING, 0, 3, 0, 0, 0, // serviceName (empty is 32 zero bits)
        0);
    }

    /**
     * To match finagle
     */
    @Test
    public void writesDebugFalse() {
        span = span.toBuilder().debug(false).build();
        writer.write(span, buf);
        assertThat(bytes).containsSequence(ThriftField.TYPE_BOOL, 0);
    }

    @Test
    public void tagsAreBinaryAnnotations() {
        writer.write(span.toBuilder().putTag("foo", "bar").build(), buf);
        assertThat(bytes).containsSequence(TYPE_LIST, 0, 8, TYPE_STRUCT, 0, 0, 0, // one binary annotation
        1).containsSequence(TYPE_STRING, 0, 1, 0, 0, 0, 3, 'f', 'o', // key
        'o').containsSequence(TYPE_STRING, 0, 2, 0, 0, 0, 3, 'b', 'a', // value
        'r').containsSequence(TYPE_I32, 0, 3, 0, 0, 0, // type 6 == string
        6);
    }

        @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    @org.openjdk.jmh.annotations.BenchmarkMode(org.openjdk.jmh.annotations.Mode.Throughput)
    @org.openjdk.jmh.annotations.Warmup(iterations = 10, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Measurement(iterations = 30, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.OutputTimeUnit(java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Fork(value = 1 )
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_endpoint_highPort() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::endpoint_highPort, this.description("endpoint_highPort"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_write_startsWithI64Prefix() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::write_startsWithI64Prefix, this.description("write_startsWithI64Prefix"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writeList_startsWithListPrefix() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writeList_startsWithListPrefix, this.description("writeList_startsWithListPrefix"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writeList_startsWithListPrefix_multiple() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writeList_startsWithListPrefix_multiple, this.description("writeList_startsWithListPrefix_multiple"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writeList_empty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writeList_empty, this.description("writeList_empty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writeList_offset_startsWithListPrefix() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writeList_offset_startsWithListPrefix, this.description("writeList_offset_startsWithListPrefix"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doesntWriteAnnotationsWhenMissingTimestamp() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doesntWriteAnnotationsWhenMissingTimestamp, this.description("doesntWriteAnnotationsWhenMissingTimestamp"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writesCoreAnnotations_client_noEndpoint() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writesCoreAnnotations_client_noEndpoint, this.description("writesCoreAnnotations_client_noEndpoint"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writesCoreAnnotations_server_noEndpoint() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writesCoreAnnotations_server_noEndpoint, this.description("writesCoreAnnotations_server_noEndpoint"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writesCoreAnnotations_producer_noEndpoint() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writesCoreAnnotations_producer_noEndpoint, this.description("writesCoreAnnotations_producer_noEndpoint"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writesCoreAnnotations_consumer_noEndpoint() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writesCoreAnnotations_consumer_noEndpoint, this.description("writesCoreAnnotations_consumer_noEndpoint"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writesBeginAnnotation_client_noEndpoint() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writesBeginAnnotation_client_noEndpoint, this.description("writesBeginAnnotation_client_noEndpoint"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writesBeginAnnotation_server_noEndpoint() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writesBeginAnnotation_server_noEndpoint, this.description("writesBeginAnnotation_server_noEndpoint"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writesBeginAnnotation_producer_noEndpoint() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writesBeginAnnotation_producer_noEndpoint, this.description("writesBeginAnnotation_producer_noEndpoint"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writesBeginAnnotation_consumer_noEndpoint() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writesBeginAnnotation_consumer_noEndpoint, this.description("writesBeginAnnotation_consumer_noEndpoint"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writesAddressBinaryAnnotation_client() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writesAddressBinaryAnnotation_client, this.description("writesAddressBinaryAnnotation_client"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writesAddressBinaryAnnotation_server() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writesAddressBinaryAnnotation_server, this.description("writesAddressBinaryAnnotation_server"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writesAddressBinaryAnnotation_producer() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writesAddressBinaryAnnotation_producer, this.description("writesAddressBinaryAnnotation_producer"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writesAddressBinaryAnnotation_consumer() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writesAddressBinaryAnnotation_consumer, this.description("writesAddressBinaryAnnotation_consumer"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_annotationsHaveEndpoints() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::annotationsHaveEndpoints, this.description("annotationsHaveEndpoints"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writesTimestampAndDuration() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writesTimestampAndDuration, this.description("writesTimestampAndDuration"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writesEmptySpanName() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writesEmptySpanName, this.description("writesEmptySpanName"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writesTraceAndSpanIds() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writesTraceAndSpanIds, this.description("writesTraceAndSpanIds"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writesParentAnd128BitTraceId() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writesParentAnd128BitTraceId, this.description("writesParentAnd128BitTraceId"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writesEmptyAnnotationAndBinaryAnnotations() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writesEmptyAnnotationAndBinaryAnnotations, this.description("writesEmptyAnnotationAndBinaryAnnotations"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writesEmptyLocalComponentWhenNoAnnotationsOrTags() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writesEmptyLocalComponentWhenNoAnnotationsOrTags, this.description("writesEmptyLocalComponentWhenNoAnnotationsOrTags"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writesEmptyServiceName() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writesEmptyServiceName, this.description("writesEmptyServiceName"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_writesDebugFalse() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::writesDebugFalse, this.description("writesDebugFalse"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_tagsAreBinaryAnnotations() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::tagsAreBinaryAnnotations, this.description("tagsAreBinaryAnnotations"));
        }

        @java.lang.Override
        public void before() throws java.lang.Throwable {
            super.before();
            this.implementation().init();
        }

        private V1ThriftSpanWriterTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new V1ThriftSpanWriterTest();
        }

        @java.lang.Override
        public V1ThriftSpanWriterTest implementation() {
            return this.implementation;
        }
    }
}
