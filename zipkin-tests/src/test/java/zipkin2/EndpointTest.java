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

import java.net.Inet4Address;
import java.net.Inet6Address;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import static org.assertj.core.api.Assertions.assertThat;

public class EndpointTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void missingIpv4IsNull() {
        assertThat(Endpoint.newBuilder().build().ipv4()).isNull();
    }

    /**
     * Many getPort operations return -1 by default. Leniently coerse to null.
     */
    @Test
    public void newBuilderWithPort_NegativeCoercesToNull() {
        assertThat(Endpoint.newBuilder().port(-1).build().port()).isNull();
    }

    @Test
    public void newBuilderWithPort_0CoercesToNull() {
        assertThat(Endpoint.newBuilder().port(0).build().port()).isNull();
    }

    @Test
    public void newBuilderWithPort_highest() {
        assertThat(Endpoint.newBuilder().port(65535).build().port()).isEqualTo(65535);
    }

    @Test
    public void ip_addr_ipv4() throws Exception {
        Endpoint.Builder newBuilder = Endpoint.newBuilder();
        assertThat(newBuilder.parseIp(Inet4Address.getByName("43.0.192.2"))).isTrue();
        Endpoint endpoint = newBuilder.build();
        assertExpectedIpv4(endpoint);
    }

    @Test
    public void ip_bytes_ipv4() throws Exception {
        Endpoint.Builder newBuilder = Endpoint.newBuilder();
        assertThat(newBuilder.parseIp(Inet4Address.getByName("43.0.192.2").getAddress())).isTrue();
        Endpoint endpoint = newBuilder.build();
        assertExpectedIpv4(endpoint);
    }

    @Test
    public void ip_string_ipv4() {
        Endpoint.Builder newBuilder = Endpoint.newBuilder();
        assertThat(newBuilder.parseIp("43.0.192.2")).isTrue();
        Endpoint endpoint = newBuilder.build();
        assertExpectedIpv4(endpoint);
    }

    @Test
    public void ip_ipv6() throws Exception {
        String ipv6 = "2001:db8::c001";
        Endpoint endpoint = Endpoint.newBuilder().ip(ipv6).build();
        assertThat(endpoint.ipv4()).isNull();
        assertThat(endpoint.ipv4Bytes()).isNull();
        assertThat(endpoint.ipv6()).isEqualTo(ipv6);
        assertThat(endpoint.ipv6Bytes()).containsExactly(Inet6Address.getByName(ipv6).getAddress());
    }

    @Test
    public void ip_ipv6_addr() throws Exception {
        String ipv6 = "2001:db8::c001";
        Endpoint endpoint = Endpoint.newBuilder().ip(Inet6Address.getByName(ipv6)).build();
        assertThat(endpoint.ipv4()).isNull();
        assertThat(endpoint.ipv4Bytes()).isNull();
        assertThat(endpoint.ipv6()).isEqualTo(ipv6);
        assertThat(endpoint.ipv6Bytes()).containsExactly(Inet6Address.getByName(ipv6).getAddress());
    }

    @Test
    public void parseIp_ipv6_bytes() throws Exception {
        String ipv6 = "2001:db8::c001";
        Endpoint.Builder newBuilder = Endpoint.newBuilder();
        assertThat(newBuilder.parseIp(Inet6Address.getByName(ipv6))).isTrue();
        Endpoint endpoint = newBuilder.build();
        assertThat(endpoint.ipv4()).isNull();
        assertThat(endpoint.ipv4Bytes()).isNull();
        assertThat(endpoint.ipv6()).isEqualTo(ipv6);
        assertThat(endpoint.ipv6Bytes()).containsExactly(Inet6Address.getByName(ipv6).getAddress());
    }

    @Test
    public void ip_ipv6_mappedIpv4() {
        String ipv6 = "::FFFF:43.0.192.2";
        Endpoint endpoint = Endpoint.newBuilder().ip(ipv6).build();
        assertExpectedIpv4(endpoint);
    }

    @Test
    public void ip_ipv6_addr_mappedIpv4() throws Exception {
        String ipv6 = "::FFFF:43.0.192.2";
        Endpoint endpoint = Endpoint.newBuilder().ip(Inet6Address.getByName(ipv6)).build();
        assertExpectedIpv4(endpoint);
    }

    @Test
    public void ip_ipv6_compatIpv4() {
        String ipv6 = "::0000:43.0.192.2";
        Endpoint endpoint = Endpoint.newBuilder().ip(ipv6).build();
        assertExpectedIpv4(endpoint);
    }

    @Test
    public void ip_ipv6_addr_compatIpv4() throws Exception {
        String ipv6 = "::0000:43.0.192.2";
        Endpoint endpoint = Endpoint.newBuilder().ip(Inet6Address.getByName(ipv6)).build();
        assertExpectedIpv4(endpoint);
    }

    @Test
    public void ipv6_notMappedIpv4() {
        String ipv6 = "::ffef:43.0.192.2";
        Endpoint endpoint = Endpoint.newBuilder().ip(ipv6).build();
        assertThat(endpoint.ipv4()).isNull();
        assertThat(endpoint.ipv4Bytes()).isNull();
        assertThat(endpoint.ipv6()).isNull();
        assertThat(endpoint.ipv6Bytes()).isNull();
    }

    @Test
    public void ipv6_downcases() {
        Endpoint endpoint = Endpoint.newBuilder().ip("2001:DB8::C001").build();
        assertThat(endpoint.ipv6()).isEqualTo("2001:db8::c001");
    }

    @Test
    public void ip_ipv6_compatIpv4_compressed() {
        String ipv6 = "::43.0.192.2";
        Endpoint endpoint = Endpoint.newBuilder().ip(ipv6).build();
        assertExpectedIpv4(endpoint);
    }

    /**
     * This ensures we don't mistake IPv6 localhost for a mapped IPv4 0.0.0.1
     */
    @Test
    public void ipv6_localhost() {
        String ipv6 = "::1";
        Endpoint endpoint = Endpoint.newBuilder().ip(ipv6).build();
        assertThat(endpoint.ipv4()).isNull();
        assertThat(endpoint.ipv4Bytes()).isNull();
        assertThat(endpoint.ipv6()).isEqualTo(ipv6);
        assertThat(endpoint.ipv6Bytes()).containsExactly(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1);
    }

    /**
     * This is an unusable compat Ipv4 of 0.0.0.2. This makes sure it isn't mistaken for localhost
     */
    @Test
    public void ipv6_notLocalhost() {
        String ipv6 = "::2";
        Endpoint endpoint = Endpoint.newBuilder().ip(ipv6).build();
        assertThat(endpoint.ipv4()).isNull();
        assertThat(endpoint.ipv6()).isEqualTo(ipv6);
    }

    /**
     * The integer arg of port should be a whole number
     */
    @Test
    public void newBuilderWithPort_tooLargeIsInvalid() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("invalid port 65536");
        assertThat(Endpoint.newBuilder().port(65536).build().port()).isNull();
    }

    /**
     * The integer arg of port should fit in a 16bit unsigned value
     */
    @Test
    public void newBuilderWithPort_tooHighIsInvalid() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("invalid port 65536");
        Endpoint.newBuilder().port(65536).build();
    }

    /**
     * Catches common error when zero is passed instead of null for a port
     */
    @Test
    public void coercesZeroPortToNull() {
        Endpoint endpoint = Endpoint.newBuilder().port(0).build();
        assertThat(endpoint.port()).isNull();
    }

    @Test
    public void lowercasesServiceName() {
        assertThat(Endpoint.newBuilder().serviceName("fFf").ip("127.0.0.1").build().serviceName()).isEqualTo("fff");
    }

    static void assertExpectedIpv4(Endpoint endpoint) {
        assertThat(endpoint.ipv4()).isEqualTo("43.0.192.2");
        assertThat(endpoint.ipv4Bytes()).containsExactly(43, 0, 192, 2);
        assertThat(endpoint.ipv6()).isNull();
        assertThat(endpoint.ipv6Bytes()).isNull();
    }

        @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    @org.openjdk.jmh.annotations.BenchmarkMode(org.openjdk.jmh.annotations.Mode.Throughput)
    @org.openjdk.jmh.annotations.Warmup(iterations = 10, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Measurement(iterations = 30, time = 1, timeUnit = java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.OutputTimeUnit(java.util.concurrent.TimeUnit.SECONDS)
    @org.openjdk.jmh.annotations.Fork(value = 1 )
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_missingIpv4IsNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::missingIpv4IsNull, this.description("missingIpv4IsNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_newBuilderWithPort_NegativeCoercesToNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::newBuilderWithPort_NegativeCoercesToNull, this.description("newBuilderWithPort_NegativeCoercesToNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_newBuilderWithPort_0CoercesToNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::newBuilderWithPort_0CoercesToNull, this.description("newBuilderWithPort_0CoercesToNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_newBuilderWithPort_highest() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::newBuilderWithPort_highest, this.description("newBuilderWithPort_highest"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ip_addr_ipv4() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ip_addr_ipv4, this.description("ip_addr_ipv4"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ip_bytes_ipv4() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ip_bytes_ipv4, this.description("ip_bytes_ipv4"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ip_string_ipv4() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ip_string_ipv4, this.description("ip_string_ipv4"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ip_ipv6() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ip_ipv6, this.description("ip_ipv6"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ip_ipv6_addr() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ip_ipv6_addr, this.description("ip_ipv6_addr"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_parseIp_ipv6_bytes() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::parseIp_ipv6_bytes, this.description("parseIp_ipv6_bytes"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ip_ipv6_mappedIpv4() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ip_ipv6_mappedIpv4, this.description("ip_ipv6_mappedIpv4"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ip_ipv6_addr_mappedIpv4() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ip_ipv6_addr_mappedIpv4, this.description("ip_ipv6_addr_mappedIpv4"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ip_ipv6_compatIpv4() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ip_ipv6_compatIpv4, this.description("ip_ipv6_compatIpv4"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ip_ipv6_addr_compatIpv4() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ip_ipv6_addr_compatIpv4, this.description("ip_ipv6_addr_compatIpv4"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ipv6_notMappedIpv4() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ipv6_notMappedIpv4, this.description("ipv6_notMappedIpv4"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ipv6_downcases() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ipv6_downcases, this.description("ipv6_downcases"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ip_ipv6_compatIpv4_compressed() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ip_ipv6_compatIpv4_compressed, this.description("ip_ipv6_compatIpv4_compressed"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ipv6_localhost() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ipv6_localhost, this.description("ipv6_localhost"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_ipv6_notLocalhost() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::ipv6_notLocalhost, this.description("ipv6_notLocalhost"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_newBuilderWithPort_tooLargeIsInvalid() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::newBuilderWithPort_tooLargeIsInvalid, this.description("newBuilderWithPort_tooLargeIsInvalid"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_newBuilderWithPort_tooHighIsInvalid() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::newBuilderWithPort_tooHighIsInvalid, this.description("newBuilderWithPort_tooHighIsInvalid"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_coercesZeroPortToNull() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::coercesZeroPortToNull, this.description("coercesZeroPortToNull"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_lowercasesServiceName() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::lowercasesServiceName, this.description("lowercasesServiceName"));
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().thrown, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private EndpointTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new EndpointTest();
        }

        @java.lang.Override
        public EndpointTest implementation() {
            return this.implementation;
        }
    }
}
