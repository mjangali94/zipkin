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
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static zipkin2.internal.HexCodec.lowerHexToUnsignedLong;

public class HexCodecTest {

    @Test
    public void lowerHexToUnsignedLong_downgrades128bitIdsByDroppingHighBits() {
        assertThat(lowerHexToUnsignedLong("463ac35c9f6413ad48485a3953bb6124")).isEqualTo(lowerHexToUnsignedLong("48485a3953bb6124"));
    }

    @Test
    public void lowerHexToUnsignedLongTest() {
        assertThat(lowerHexToUnsignedLong("ffffffffffffffff")).isEqualTo(-1);
        assertThat(lowerHexToUnsignedLong("0")).isEqualTo(0);
        assertThat(lowerHexToUnsignedLong(Long.toHexString(Long.MAX_VALUE))).isEqualTo(Long.MAX_VALUE);
        try {
            // too long
            lowerHexToUnsignedLong("fffffffffffffffffffffffffffffffff");
            failBecauseExceptionWasNotThrown(NumberFormatException.class);
        } catch (NumberFormatException e) {
        }
        try {
            // too short
            lowerHexToUnsignedLong("");
            failBecauseExceptionWasNotThrown(NumberFormatException.class);
        } catch (NumberFormatException e) {
        }
        try {
            // bad charset
            lowerHexToUnsignedLong("rs");
            failBecauseExceptionWasNotThrown(NumberFormatException.class);
        } catch (NumberFormatException e) {
        }
        try {
            // uppercase
            lowerHexToUnsignedLong("48485A3953BB6124");
            failBecauseExceptionWasNotThrown(NumberFormatException.class);
        } catch (NumberFormatException e) {
            assertThat(e).hasMessage("48485A3953BB6124 should be a 1 to 32 character lower-hex string with no prefix");
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
        public void benchmark_lowerHexToUnsignedLong_downgrades128bitIdsByDroppingHighBits() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lowerHexToUnsignedLong_downgrades128bitIdsByDroppingHighBits, this.description("lowerHexToUnsignedLong_downgrades128bitIdsByDroppingHighBits"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lowerHexToUnsignedLongTest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lowerHexToUnsignedLongTest, this.description("lowerHexToUnsignedLongTest"));
        }

        private HexCodecTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new HexCodecTest();
        }

        @java.lang.Override
        public HexCodecTest implementation() {
            return this.implementation;
        }
    }
}
