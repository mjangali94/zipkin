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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.assertj.core.api.Assertions.assertThat;

public class AnnotationTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void messageWhenMissingValue() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("value");
        Annotation.create(1L, null);
    }

    @Test
    public void toString_isNice() {
        assertThat(Annotation.create(1L, "foo")).hasToString("Annotation{timestamp=1, value=foo}");
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    @org.openjdk.jmh.annotations.BenchmarkMode(org.openjdk.jmh.annotations.Mode.Throughput)
    @org.openjdk.jmh.annotations.Warmup(iterations = 10, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Measurement(iterations = 30, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.OutputTimeUnit(java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Fork(value = 1 )
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_messageWhenMissingValue() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::messageWhenMissingValue, this.description("messageWhenMissingValue"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_toString_isNice() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::toString_isNice, this.description("toString_isNice"));
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().thrown, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private AnnotationTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new AnnotationTest();
        }

        @java.lang.Override
        public AnnotationTest implementation() {
            return this.implementation;
        }
    }
}
