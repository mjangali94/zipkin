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

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.TestObjects;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class QueryRequestTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    QueryRequest.Builder queryBuilder = QueryRequest.newBuilder().endTs(TestObjects.TODAY).lookback(60).limit(10);

    Span span = Span.newBuilder().traceId("10").id("10").name("receive").localEndpoint(Endpoint.newBuilder().serviceName("app").build()).kind(Span.Kind.CONSUMER).timestamp(TestObjects.TODAY * 1000).build();

    @Test
    public void serviceNameCanBeNull() {
        assertThat(queryBuilder.build().serviceName()).isNull();
    }

    @Test
    public void serviceName_coercesEmptyToNull() {
        assertThat(queryBuilder.serviceName("").build().serviceName()).isNull();
    }

    @Test
    public void remoteServiceNameCanBeNull() {
        assertThat(queryBuilder.build().remoteServiceName()).isNull();
    }

    @Test
    public void remoteServiceName_coercesEmptyToNull() {
        assertThat(queryBuilder.remoteServiceName("").build().remoteServiceName()).isNull();
    }

    @Test
    public void spanName_coercesAllToNull() {
        assertThat(queryBuilder.spanName("all").build().spanName()).isNull();
    }

    @Test
    public void spanName_coercesEmptyToNull() {
        assertThat(queryBuilder.spanName("").build().spanName()).isNull();
    }

    @Test
    public void annotationQuerySkipsEmptyKeys() {
        Map<String, String> query = new LinkedHashMap<>();
        query.put("", "bar");
        assertThat(queryBuilder.annotationQuery(query).build().annotationQuery()).isEmpty();
    }

    @Test
    public void annotationQueryTrimsSpaces() {
        // spaces in http.path mixed with 'and'
        assertThat(queryBuilder.parseAnnotationQuery("fo and o and bar and http.path = /a ").annotationQuery).containsOnly(entry("fo", ""), entry("o", ""), entry("bar", ""), entry("http.path", "/a"));
        // http.path in the beginning, more spaces
        assertThat(queryBuilder.parseAnnotationQuery(" http.path = /a   and fo and o   and bar").annotationQuery).containsOnly(entry("fo", ""), entry("o", ""), entry("bar", ""), entry("http.path", "/a"));
        // @adriancole said this would be hard to parse, annotation containing spaces
        assertThat(queryBuilder.parseAnnotationQuery("L O L").annotationQuery).containsOnly(entry("L O L", ""));
        // annotation with spaces combined with tag
        assertThat(queryBuilder.parseAnnotationQuery("L O L and http.path = /a").annotationQuery).containsOnly(entry("L O L", ""), entry("http.path", "/a"));
        assertThat(queryBuilder.parseAnnotationQuery("bar =123 and L O L and http.path = /a and A B C").annotationQuery).containsOnly(entry("L O L", ""), entry("http.path", "/a"), entry("bar", "123"), entry("A B C", ""));
    }

    @Test
    public void annotationQueryParameterSpecificity() {
        // when a parameter is specified both as a tag and annotation, the tag wins because it's considered to be more
        // specific
        assertThat(queryBuilder.parseAnnotationQuery("a=123 and a").annotationQuery).containsOnly(entry("a", "123"));
        assertThat(queryBuilder.parseAnnotationQuery("a and a=123").annotationQuery).containsOnly(entry("a", "123"));
        // also last tag wins
        assertThat(queryBuilder.parseAnnotationQuery("a=123 and a=456").annotationQuery).containsOnly(entry("a", "456"));
        assertThat(queryBuilder.parseAnnotationQuery("a and a=123 and a=456").annotationQuery).containsOnly(entry("a", "456"));
    }

    @Test
    public void endTsMustBePositive() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("endTs <= 0");
        queryBuilder.endTs(0L).build();
    }

    @Test
    public void lookbackMustBePositive() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("lookback <= 0");
        queryBuilder.lookback(0).build();
    }

    @Test
    public void limitMustBePositive() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("limit <= 0");
        queryBuilder.limit(0).build();
    }

    @Test
    public void annotationQuery_roundTrip() {
        String annotationQuery = "http.method=GET and error";
        QueryRequest request = queryBuilder.serviceName("security-service").parseAnnotationQuery(annotationQuery).build();
        assertThat(request.annotationQuery()).containsEntry("error", "").containsEntry("http.method", "GET");
        assertThat(request.annotationQueryString()).isEqualTo(annotationQuery);
    }

    @Test
    public void annotationQuery_missingValue() {
        String annotationQuery = "http.method=";
        QueryRequest request = queryBuilder.serviceName("security-service").parseAnnotationQuery(annotationQuery).build();
        assertThat(request.annotationQuery()).containsKey("http.method");
    }

    @Test
    public void annotationQueryWhenNoInputIsEmpty() {
        assertThat(queryBuilder.build().annotationQuery()).isEmpty();
    }

    @Test
    public void minDuration_mustBePositive() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("minDuration <= 0");
        queryBuilder.minDuration(0L).build();
    }

    @Test
    public void maxDuration_onlyWithMinDuration() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("maxDuration is only valid with minDuration");
        queryBuilder.maxDuration(0L).build();
    }

    @Test
    public void maxDuration_greaterThanOrEqualToMinDuration() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("maxDuration < minDuration");
        queryBuilder.minDuration(1L).maxDuration(0L).build();
    }

    @Test
    public void test_matchesTimestamp() {
        QueryRequest request = queryBuilder.build();
        assertThat(request.test(asList(span))).isTrue();
    }

    @Test
    public void test_rootSpanNotFirst() {
        QueryRequest request = queryBuilder.build();
        assertThat(request.test(asList(span.toBuilder().id("2").parentId(span.id()).timestamp(null).build(), span))).isTrue();
    }

    @Test
    public void test_noRootSpanLeastWins() {
        QueryRequest request = queryBuilder.build();
        assertThat(request.test(asList(span.toBuilder().id("2").parentId(span.id()).timestamp(span.timestamp() + TestObjects.DAY * 1000).build(), span.toBuilder().id("3").parentId(span.id()).build()))).isTrue();
    }

    @Test
    public void test_noTimestamp() {
        QueryRequest request = queryBuilder.build();
        assertThat(request.test(asList(span.toBuilder().timestamp(null).build()))).isFalse();
    }

    @Test
    public void test_timestampPastLookback() {
        QueryRequest request = queryBuilder.endTs(TestObjects.TODAY + 70).build();
        assertThat(request.test(asList(span))).isFalse();
    }

    @Test
    public void test_wrongServiceName() {
        QueryRequest request = queryBuilder.serviceName("aloha").build();
        assertThat(request.test(asList(span))).isFalse();
    }

    @Test
    public void test_spanName() {
        QueryRequest request = queryBuilder.spanName("aloha").build();
        assertThat(request.test(asList(span))).isFalse();
        assertThat(request.test(asList(span.toBuilder().name("aloha").build()))).isTrue();
    }

    @Test
    public void test_remoteServiceName() {
        QueryRequest request = queryBuilder.remoteServiceName("db").build();
        assertThat(request.test(asList(span))).isFalse();
        assertThat(request.test(asList(span.toBuilder().remoteEndpoint(Endpoint.newBuilder().serviceName("db").build()).build()))).isTrue();
    }

    @Test
    public void test_minDuration() {
        QueryRequest request = queryBuilder.minDuration(100L).build();
        assertThat(request.test(asList(span.toBuilder().duration(99L).build()))).isFalse();
        assertThat(request.test(asList(span.toBuilder().duration(100L).build()))).isTrue();
    }

    @Test
    public void test_maxDuration() {
        QueryRequest request = queryBuilder.minDuration(100L).maxDuration(110L).build();
        assertThat(request.test(asList(span.toBuilder().duration(99L).build()))).isFalse();
        assertThat(request.test(asList(span.toBuilder().duration(100L).build()))).isTrue();
        assertThat(request.test(asList(span.toBuilder().duration(111L).build()))).isFalse();
    }

    Span foo = span.toBuilder().traceId("1").name("call1").id("1").addAnnotation(span.timestamp(), "foo").build();

    // would be foo bar, except lexicographically bar precedes foo
    Span barAndFoo = span.toBuilder().traceId("2").name("call2").id("2").addAnnotation(span.timestamp(), "bar").addAnnotation(span.timestamp(), "foo").build();

    Span fooAndBazAndQux = span.toBuilder().traceId("3").name("call3").id("3").addAnnotation(span.timestamp(), "foo").putTag("baz", "qux").build();

    Span barAndFooAndBazAndQux = span.toBuilder().traceId("4").name("call4").id("4").addAnnotation(span.timestamp(), "bar").addAnnotation(span.timestamp(), "foo").putTag("baz", "qux").build();

    @Test
    public void test_annotationQuery_tagKey() {
        QueryRequest query = queryBuilder.parseAnnotationQuery("baz").build();
        assertThat(query.test(asList(foo))).isFalse();
        assertThat(query.test(asList(barAndFoo))).isFalse();
        assertThat(query.test(asList(barAndFooAndBazAndQux))).isTrue();
        assertThat(query.test(asList(fooAndBazAndQux))).isTrue();
    }

    @Test
    public void test_annotationQuery_annotation() {
        QueryRequest query = queryBuilder.parseAnnotationQuery("foo").build();
        assertThat(query.test(asList(foo))).isTrue();
        assertThat(query.test(asList(barAndFoo))).isTrue();
        assertThat(query.test(asList(barAndFooAndBazAndQux))).isTrue();
        assertThat(query.test(asList(fooAndBazAndQux))).isTrue();
    }

    @Test
    public void test_annotationQuery_twoAnnotation() {
        QueryRequest query = queryBuilder.parseAnnotationQuery("foo and bar").build();
        assertThat(query.test(asList(foo))).isFalse();
        assertThat(query.test(asList(barAndFoo))).isTrue();
        assertThat(query.test(asList(barAndFooAndBazAndQux))).isTrue();
        assertThat(query.test(asList(fooAndBazAndQux))).isFalse();
    }

    @Test
    public void test_annotationQuery_annotationsAndTag() {
        QueryRequest query = queryBuilder.parseAnnotationQuery("foo and bar and baz=qux").build();
        assertThat(query.test(asList(foo))).isFalse();
        assertThat(query.test(asList(barAndFoo))).isFalse();
        assertThat(query.test(asList(barAndFooAndBazAndQux))).isTrue();
        assertThat(query.test(asList(fooAndBazAndQux))).isFalse();
    }

        @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    @org.openjdk.jmh.annotations.BenchmarkMode(org.openjdk.jmh.annotations.Mode.Throughput)
    @org.openjdk.jmh.annotations.Warmup(iterations = 10, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Measurement(iterations = 30, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.OutputTimeUnit(java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Fork(value = 1 )
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_serviceNameCanBeNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::serviceNameCanBeNull, this.description("serviceNameCanBeNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_serviceName_coercesEmptyToNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::serviceName_coercesEmptyToNull, this.description("serviceName_coercesEmptyToNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_remoteServiceNameCanBeNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::remoteServiceNameCanBeNull, this.description("remoteServiceNameCanBeNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_remoteServiceName_coercesEmptyToNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::remoteServiceName_coercesEmptyToNull, this.description("remoteServiceName_coercesEmptyToNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanName_coercesAllToNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanName_coercesAllToNull, this.description("spanName_coercesAllToNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_spanName_coercesEmptyToNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::spanName_coercesEmptyToNull, this.description("spanName_coercesEmptyToNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_annotationQuerySkipsEmptyKeys() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::annotationQuerySkipsEmptyKeys, this.description("annotationQuerySkipsEmptyKeys"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_annotationQueryTrimsSpaces() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::annotationQueryTrimsSpaces, this.description("annotationQueryTrimsSpaces"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_annotationQueryParameterSpecificity() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::annotationQueryParameterSpecificity, this.description("annotationQueryParameterSpecificity"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_endTsMustBePositive() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::endTsMustBePositive, this.description("endTsMustBePositive"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lookbackMustBePositive() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lookbackMustBePositive, this.description("lookbackMustBePositive"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_limitMustBePositive() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::limitMustBePositive, this.description("limitMustBePositive"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_annotationQuery_roundTrip() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::annotationQuery_roundTrip, this.description("annotationQuery_roundTrip"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_annotationQuery_missingValue() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::annotationQuery_missingValue, this.description("annotationQuery_missingValue"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_annotationQueryWhenNoInputIsEmpty() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::annotationQueryWhenNoInputIsEmpty, this.description("annotationQueryWhenNoInputIsEmpty"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_minDuration_mustBePositive() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::minDuration_mustBePositive, this.description("minDuration_mustBePositive"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maxDuration_onlyWithMinDuration() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::maxDuration_onlyWithMinDuration, this.description("maxDuration_onlyWithMinDuration"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_maxDuration_greaterThanOrEqualToMinDuration() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::maxDuration_greaterThanOrEqualToMinDuration, this.description("maxDuration_greaterThanOrEqualToMinDuration"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test_matchesTimestamp() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test_matchesTimestamp, this.description("test_matchesTimestamp"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test_rootSpanNotFirst() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test_rootSpanNotFirst, this.description("test_rootSpanNotFirst"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test_noRootSpanLeastWins() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test_noRootSpanLeastWins, this.description("test_noRootSpanLeastWins"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test_noTimestamp() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test_noTimestamp, this.description("test_noTimestamp"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test_timestampPastLookback() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test_timestampPastLookback, this.description("test_timestampPastLookback"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test_wrongServiceName() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test_wrongServiceName, this.description("test_wrongServiceName"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test_spanName() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test_spanName, this.description("test_spanName"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test_remoteServiceName() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test_remoteServiceName, this.description("test_remoteServiceName"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test_minDuration() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test_minDuration, this.description("test_minDuration"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test_maxDuration() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test_maxDuration, this.description("test_maxDuration"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test_annotationQuery_tagKey() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test_annotationQuery_tagKey, this.description("test_annotationQuery_tagKey"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test_annotationQuery_annotation() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test_annotationQuery_annotation, this.description("test_annotationQuery_annotation"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test_annotationQuery_twoAnnotation() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test_annotationQuery_twoAnnotation, this.description("test_annotationQuery_twoAnnotation"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_test_annotationQuery_annotationsAndTag() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::test_annotationQuery_annotationsAndTag, this.description("test_annotationQuery_annotationsAndTag"));
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().thrown, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private QueryRequestTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new QueryRequestTest();
        }

        @java.lang.Override
        public QueryRequestTest implementation() {
            return this.implementation;
        }
    }
}
