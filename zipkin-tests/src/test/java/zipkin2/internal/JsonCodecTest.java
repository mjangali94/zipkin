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

import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static zipkin2.internal.JsonCodec.exceptionReading;

public class JsonCodecTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void doesntStackOverflowOnToBufferWriterBug_lessThanBytes() {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("Bug found using FooWriter to write Foo as json. Wrote 1/2 bytes: a");
        class FooWriter implements WriteBuffer.Writer {

            @Override
            public int sizeInBytes(Object value) {
                return 2;
            }

            @Override
            public void write(Object value, WriteBuffer buffer) {
                buffer.writeByte('a');
                throw new RuntimeException("buggy");
            }
        }
        class Foo {

            @Override
            public String toString() {
                return new String(JsonCodec.write(new FooWriter(), this), UTF_8);
            }
        }
        // cause the exception
        new Foo().toString();
    }

    @Test
    public void doesntStackOverflowOnToBufferWriterBug_Overflow() {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("Bug found using FooWriter to write Foo as json. Wrote 2/2 bytes: ab");
        // pretend there was a bug calculating size, ex it calculated incorrectly as to small
        class FooWriter implements WriteBuffer.Writer {

            @Override
            public int sizeInBytes(Object value) {
                return 2;
            }

            @Override
            public void write(Object value, WriteBuffer buffer) {
                buffer.writeByte('a');
                buffer.writeByte('b');
                // wrote larger than size!
                buffer.writeByte('c');
            }
        }
        class Foo {

            @Override
            public String toString() {
                return new String(JsonCodec.write(new FooWriter(), this), UTF_8);
            }
        }
        // cause the exception
        new Foo().toString();
    }

    @Test
    public void exceptionReading_malformedJsonWraps() {
        // grab a real exception from the gson library
        Exception error = null;
        byte[] bytes = "[\"='".getBytes(UTF_8);
        try {
            new JsonCodec.JsonReader(ReadBuffer.wrap(bytes)).beginObject();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IOException | IllegalStateException e) {
            error = e;
        }
        try {
            exceptionReading("List<Span>", error);
            failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("Malformed reading List<Span> from json");
        }
    }

        @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    @org.openjdk.jmh.annotations.BenchmarkMode(org.openjdk.jmh.annotations.Mode.Throughput)
    @org.openjdk.jmh.annotations.Warmup(iterations = 10, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Measurement(iterations = 30, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.OutputTimeUnit(java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Fork(value = 1 )
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doesntStackOverflowOnToBufferWriterBug_lessThanBytes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doesntStackOverflowOnToBufferWriterBug_lessThanBytes, this.description("doesntStackOverflowOnToBufferWriterBug_lessThanBytes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_doesntStackOverflowOnToBufferWriterBug_Overflow() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::doesntStackOverflowOnToBufferWriterBug_Overflow, this.description("doesntStackOverflowOnToBufferWriterBug_Overflow"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_exceptionReading_malformedJsonWraps() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::exceptionReading_malformedJsonWraps, this.description("exceptionReading_malformedJsonWraps"));
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().thrown, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private JsonCodecTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new JsonCodecTest();
        }

        @java.lang.Override
        public JsonCodecTest implementation() {
            return this.implementation;
        }
    }
}
