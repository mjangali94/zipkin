cd zipkin
git checkout -f JUnit2JMH
git pull
mvn clean compile -Dlicense.skip=true -DskipTests 
cd zipkin-tests/
mvn clean install -Dlicense.skip=true >> ../../myResult/junit_BEFORE.txt
cd ../
git checkout -f PMT-PTW
git pull
mvn clean compile -Dlicense.skip=true -DskipTests 
cd zipkin-tests/
mvn clean install -Dlicense.skip=true >> ../../myResult/junit_AFTER.txt
cd ../
git checkout -f master
git pull
mvn clean compile -Dlicense.skip=true -DskipTests 
cd benchmarks/
mvn package
cd target
java -jar benchmarks.jar zipkin2.internal.WriteBufferBenchmarks.utf8SizeInBytes_chinese -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.WriteBufferBenchmarks.utf8SizeInBytes_chinese_BEFORE.json
java -jar benchmarks.jar zipkin2.internal.WriteBufferBenchmarks.varIntSizeInBytes_32 -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.WriteBufferBenchmarks.varIntSizeInBytes_32_BEFORE.json
java -jar benchmarks.jar zipkin2.internal.WriteBufferBenchmarks.varIntSizeInBytes_64 -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.WriteBufferBenchmarks.varIntSizeInBytes_64_BEFORE.json
java -jar benchmarks.jar zipkin2.internal.WriteBufferBenchmarks.writeLongLe -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.WriteBufferBenchmarks.writeLongLe_BEFORE.json
java -jar benchmarks.jar zipkin2.internal.WriteBufferBenchmarks.writeUtf8_chinese -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.WriteBufferBenchmarks.writeUtf8_chinese_BEFORE.json
java -jar benchmarks.jar zipkin2.internal.WriteBufferBenchmarks.writeUtf8_chinese_jdk -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.WriteBufferBenchmarks.writeUtf8_chinese_jdk_BEFORE.json
java -jar benchmarks.jar zipkin2.internal.WriteBufferBenchmarks.writeVarint_32 -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.WriteBufferBenchmarks.writeVarint_32_BEFORE.json
java -jar benchmarks.jar zipkin2.internal.WriteBufferBenchmarks.writeVarint_64 -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.WriteBufferBenchmarks.writeVarint_64_BEFORE.json
java -jar benchmarks.jar zipkin2.SpanBenchmarks.buildClientSpan -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.SpanBenchmarks.buildClientSpan_BEFORE.json
java -jar benchmarks.jar zipkin2.SpanBenchmarks.buildClientSpan_clear -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.SpanBenchmarks.buildClientSpan_clear_BEFORE.json
java -jar benchmarks.jar zipkin2.SpanBenchmarks.buildClientSpan_clone -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.SpanBenchmarks.buildClientSpan_clone_BEFORE.json
java -jar benchmarks.jar zipkin2.SpanBenchmarks.buildClientSpan_longs -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.SpanBenchmarks.buildClientSpan_longs_BEFORE.json
java -jar benchmarks.jar zipkin2.SpanBenchmarks.deserialize_kryo -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.SpanBenchmarks.deserialize_kryo_BEFORE.json
java -jar benchmarks.jar zipkin2.SpanBenchmarks.padLeft_15Chars -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.SpanBenchmarks.padLeft_15Chars_BEFORE.json
java -jar benchmarks.jar zipkin2.SpanBenchmarks.padLeft_17Chars -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.SpanBenchmarks.padLeft_17Chars_BEFORE.json
java -jar benchmarks.jar zipkin2.SpanBenchmarks.padLeft_1Char -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.SpanBenchmarks.padLeft_1Char_BEFORE.json
java -jar benchmarks.jar zipkin2.SpanBenchmarks.padLeft_31Chars -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.SpanBenchmarks.padLeft_31Chars_BEFORE.json
java -jar benchmarks.jar zipkin2.SpanBenchmarks.serialize_kryo -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.SpanBenchmarks.serialize_kryo_BEFORE.json
java -jar benchmarks.jar zipkin2.codec.JsonCodecBenchmarks.bytebuffer_jacksonDecoder -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.codec.JsonCodecBenchmarks.bytebuffer_jacksonDecoder_BEFORE.json
java -jar benchmarks.jar zipkin2.codec.JsonCodecBenchmarks.bytebuffer_moshiDecoder -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.codec.JsonCodecBenchmarks.bytebuffer_moshiDecoder_BEFORE.json
java -jar benchmarks.jar zipkin2.codec.JsonCodecBenchmarks.bytebuffer_zipkinDecoder -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.codec.JsonCodecBenchmarks.bytebuffer_zipkinDecoder_BEFORE.json
java -jar benchmarks.jar zipkin2.codec.JsonCodecBenchmarks.bytes_jacksonDecoder -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.codec.JsonCodecBenchmarks.bytes_jacksonDecoder_BEFORE.json
java -jar benchmarks.jar zipkin2.codec.JsonCodecBenchmarks.bytes_moshiDecoder -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.codec.JsonCodecBenchmarks.bytes_moshiDecoder_BEFORE.json
java -jar benchmarks.jar zipkin2.codec.JsonCodecBenchmarks.bytes_zipkinDecoder -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.codec.JsonCodecBenchmarks.bytes_zipkinDecoder_BEFORE.json
java -jar benchmarks.jar zipkin2.internal.ReadBufferBenchmarks.readLong -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.ReadBufferBenchmarks.readLong_BEFORE.json
java -jar benchmarks.jar zipkin2.internal.ReadBufferBenchmarks.readLongReverseBytes -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.ReadBufferBenchmarks.readLongReverseBytes_BEFORE.json
java -jar benchmarks.jar zipkin2.internal.ReadBufferBenchmarks.readLong_8arity -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.ReadBufferBenchmarks.readLong_8arity_BEFORE.json
java -jar benchmarks.jar zipkin2.internal.ReadBufferBenchmarks.readLong_8arity_localArray -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.ReadBufferBenchmarks.readLong_8arity_localArray_BEFORE.json
java -jar benchmarks.jar zipkin2.internal.ReadBufferBenchmarks.readLong_localArray -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.ReadBufferBenchmarks.readLong_localArray_BEFORE.json
cd ../../
git checkout -f PMT-PTW
git pull
mvn clean compile -Dlicense.skip=true -DskipTests 
cd benchmarks/
mvn package
cd target
java -jar benchmarks.jar zipkin2.internal.WriteBufferBenchmarks.utf8SizeInBytes_chinese -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.WriteBufferBenchmarks.utf8SizeInBytes_chinese_AFTER.json
java -jar benchmarks.jar zipkin2.internal.WriteBufferBenchmarks.varIntSizeInBytes_32 -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.WriteBufferBenchmarks.varIntSizeInBytes_32_AFTER.json
java -jar benchmarks.jar zipkin2.internal.WriteBufferBenchmarks.varIntSizeInBytes_64 -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.WriteBufferBenchmarks.varIntSizeInBytes_64_AFTER.json
java -jar benchmarks.jar zipkin2.internal.WriteBufferBenchmarks.writeLongLe -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.WriteBufferBenchmarks.writeLongLe_AFTER.json
java -jar benchmarks.jar zipkin2.internal.WriteBufferBenchmarks.writeUtf8_chinese -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.WriteBufferBenchmarks.writeUtf8_chinese_AFTER.json
java -jar benchmarks.jar zipkin2.internal.WriteBufferBenchmarks.writeUtf8_chinese_jdk -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.WriteBufferBenchmarks.writeUtf8_chinese_jdk_AFTER.json
java -jar benchmarks.jar zipkin2.internal.WriteBufferBenchmarks.writeVarint_32 -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.WriteBufferBenchmarks.writeVarint_32_AFTER.json
java -jar benchmarks.jar zipkin2.internal.WriteBufferBenchmarks.writeVarint_64 -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.WriteBufferBenchmarks.writeVarint_64_AFTER.json
java -jar benchmarks.jar zipkin2.SpanBenchmarks.buildClientSpan -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.SpanBenchmarks.buildClientSpan_AFTER.json
java -jar benchmarks.jar zipkin2.SpanBenchmarks.buildClientSpan_clear -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.SpanBenchmarks.buildClientSpan_clear_AFTER.json
java -jar benchmarks.jar zipkin2.SpanBenchmarks.buildClientSpan_clone -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.SpanBenchmarks.buildClientSpan_clone_AFTER.json
java -jar benchmarks.jar zipkin2.SpanBenchmarks.buildClientSpan_longs -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.SpanBenchmarks.buildClientSpan_longs_AFTER.json
java -jar benchmarks.jar zipkin2.SpanBenchmarks.deserialize_kryo -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.SpanBenchmarks.deserialize_kryo_AFTER.json
java -jar benchmarks.jar zipkin2.SpanBenchmarks.padLeft_15Chars -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.SpanBenchmarks.padLeft_15Chars_AFTER.json
java -jar benchmarks.jar zipkin2.SpanBenchmarks.padLeft_17Chars -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.SpanBenchmarks.padLeft_17Chars_AFTER.json
java -jar benchmarks.jar zipkin2.SpanBenchmarks.padLeft_1Char -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.SpanBenchmarks.padLeft_1Char_AFTER.json
java -jar benchmarks.jar zipkin2.SpanBenchmarks.padLeft_31Chars -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.SpanBenchmarks.padLeft_31Chars_AFTER.json
java -jar benchmarks.jar zipkin2.SpanBenchmarks.serialize_kryo -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.SpanBenchmarks.serialize_kryo_AFTER.json
java -jar benchmarks.jar zipkin2.codec.JsonCodecBenchmarks.bytebuffer_jacksonDecoder -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.codec.JsonCodecBenchmarks.bytebuffer_jacksonDecoder_AFTER.json
java -jar benchmarks.jar zipkin2.codec.JsonCodecBenchmarks.bytebuffer_moshiDecoder -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.codec.JsonCodecBenchmarks.bytebuffer_moshiDecoder_AFTER.json
java -jar benchmarks.jar zipkin2.codec.JsonCodecBenchmarks.bytebuffer_zipkinDecoder -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.codec.JsonCodecBenchmarks.bytebuffer_zipkinDecoder_AFTER.json
java -jar benchmarks.jar zipkin2.codec.JsonCodecBenchmarks.bytes_jacksonDecoder -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.codec.JsonCodecBenchmarks.bytes_jacksonDecoder_AFTER.json
java -jar benchmarks.jar zipkin2.codec.JsonCodecBenchmarks.bytes_moshiDecoder -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.codec.JsonCodecBenchmarks.bytes_moshiDecoder_AFTER.json
java -jar benchmarks.jar zipkin2.codec.JsonCodecBenchmarks.bytes_zipkinDecoder -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.codec.JsonCodecBenchmarks.bytes_zipkinDecoder_AFTER.json
java -jar benchmarks.jar zipkin2.internal.ReadBufferBenchmarks.readLong -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.ReadBufferBenchmarks.readLong_AFTER.json
java -jar benchmarks.jar zipkin2.internal.ReadBufferBenchmarks.readLongReverseBytes -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.ReadBufferBenchmarks.readLongReverseBytes_AFTER.json
java -jar benchmarks.jar zipkin2.internal.ReadBufferBenchmarks.readLong_8arity -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.ReadBufferBenchmarks.readLong_8arity_AFTER.json
java -jar benchmarks.jar zipkin2.internal.ReadBufferBenchmarks.readLong_8arity_localArray -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.ReadBufferBenchmarks.readLong_8arity_localArray_AFTER.json
java -jar benchmarks.jar zipkin2.internal.ReadBufferBenchmarks.readLong_localArray -bm thrpt -wi 10 -i 30 -f 1 -tu s -w 1 -r 1 -rf json -rff ../../myResult/zipkin2.internal.ReadBufferBenchmarks.readLong_localArray_AFTER.json

