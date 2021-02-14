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
package zipkin2.storage;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import zipkin2.CheckResult;
import zipkin2.Component;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ForwardingStorageComponentTest {

    /**
     * This test is intentionally brittle. It should break if we add new methods on {@link
     * StorageComponent}!
     */
    @Test
    public void declaresAllMethodsToForward() {
        assertThat(ForwardingStorageComponent.class.getDeclaredMethods()).extracting(Method::getName).containsAll(Stream.concat(Stream.of(Component.class.getDeclaredMethods()).map(Method::getName), Stream.of(StorageComponent.class.getDeclaredMethods()).map(Method::getName)).collect(Collectors.toList()));
    }

    StorageComponent delegate = mock(StorageComponent.class);

    StorageComponent forwarder = new ForwardingStorageComponent() {

        @Override
        protected StorageComponent delegate() {
            return delegate;
        }
    };

    @AfterEach
    public void verifyNoExtraCalls() {
        verifyNoMoreInteractions(delegate);
    }

    @Test
    public void delegatesToString() {
        assertThat(forwarder.toString()).isEqualTo(delegate.toString());
    }

    @Test
    public void delegatesCheck() {
        CheckResult down = CheckResult.failed(new RuntimeException("failed"));
        when(delegate.check()).thenReturn(down);
        assertThat(forwarder.check()).isEqualTo(down);
        verify(delegate).check();
    }

    @Test
    public void delegatesIsOverCapacity() {
        Exception wayOver = new RejectedExecutionException();
        when(delegate.isOverCapacity(wayOver)).thenReturn(true);
        assertThat(forwarder.isOverCapacity(wayOver)).isEqualTo(true);
        verify(delegate).isOverCapacity(wayOver);
    }

    @Test
    public void delegatesClose() throws IOException {
        doNothing().when(delegate).close();
        forwarder.close();
        verify(delegate).close();
    }

    @Test
    public void delegatesSpanStore() {
        SpanStore spanStore = mock(SpanStore.class);
        when(delegate.spanStore()).thenReturn(spanStore);
        assertThat(forwarder.spanStore()).isEqualTo(spanStore);
        verify(delegate).spanStore();
    }

    @Test
    public void delegatesAutocompleteTags() {
        AutocompleteTags autocompleteTags = mock(AutocompleteTags.class);
        when(delegate.autocompleteTags()).thenReturn(autocompleteTags);
        assertThat(forwarder.autocompleteTags()).isEqualTo(autocompleteTags);
        verify(delegate).autocompleteTags();
    }

    @Test
    public void delegatesServiceAndSpanNames() {
        ServiceAndSpanNames serviceAndSpanNames = mock(ServiceAndSpanNames.class);
        when(delegate.serviceAndSpanNames()).thenReturn(serviceAndSpanNames);
        assertThat(forwarder.serviceAndSpanNames()).isEqualTo(serviceAndSpanNames);
        verify(delegate).serviceAndSpanNames();
    }

    @Test
    public void delegatesSpanConsumer() {
        SpanConsumer spanConsumer = mock(SpanConsumer.class);
        when(delegate.spanConsumer()).thenReturn(spanConsumer);
        assertThat(forwarder.spanConsumer()).isEqualTo(spanConsumer);
        verify(delegate).spanConsumer();
    }

        @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    @org.openjdk.jmh.annotations.BenchmarkMode(org.openjdk.jmh.annotations.Mode.Throughput)
    @org.openjdk.jmh.annotations.Warmup(iterations = 10, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Measurement(iterations = 30, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.OutputTimeUnit(java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Fork(value = 1 )
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_declaresAllMethodsToForward() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::declaresAllMethodsToForward, this.description("declaresAllMethodsToForward"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delegatesToString() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::delegatesToString, this.description("delegatesToString"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delegatesCheck() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::delegatesCheck, this.description("delegatesCheck"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delegatesIsOverCapacity() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::delegatesIsOverCapacity, this.description("delegatesIsOverCapacity"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delegatesClose() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::delegatesClose, this.description("delegatesClose"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delegatesSpanStore() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::delegatesSpanStore, this.description("delegatesSpanStore"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delegatesAutocompleteTags() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::delegatesAutocompleteTags, this.description("delegatesAutocompleteTags"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delegatesServiceAndSpanNames() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::delegatesServiceAndSpanNames, this.description("delegatesServiceAndSpanNames"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_delegatesSpanConsumer() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::delegatesSpanConsumer, this.description("delegatesSpanConsumer"));
        }

        private ForwardingStorageComponentTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new ForwardingStorageComponentTest();
        }

        @java.lang.Override
        public ForwardingStorageComponentTest implementation() {
            return this.implementation;
        }
    }
}
