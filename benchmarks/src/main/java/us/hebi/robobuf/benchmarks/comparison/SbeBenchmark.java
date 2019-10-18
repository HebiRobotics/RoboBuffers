package us.hebi.robobuf.benchmarks.comparison;

import com.google.protobuf.AbstractMessageLite;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Parser;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;
import uk.co.real_logic.protobuf.examples.Examples;
import uk.co.real_logic.protobuf.fix.Fix;
import uk.co.real_logic.robo.examples.Examples.Car;
import uk.co.real_logic.robo.fix.Fix.MarketDataIncrementalRefreshTrades;
import us.hebi.robobuf.ProtoMessage;
import us.hebi.robobuf.ProtoSink;
import us.hebi.robobuf.ProtoSource;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Generates messages with the same contents as the old benchmarks in Simple Binary Encoding (SBE)
 *
 * NUC8i7BEH / Windows / JDK8
 *
 *
 * === RoboBuffers (Unsafe) ===
 * Benchmark                                Mode  Cnt   Score    Error  Units
 * SbeBenchmark.carUnsafeRoboRead           avgt   10  33.602 ± 0.568  ms/op
 * SbeBenchmark.carUnsafeRoboReadReadWrite  avgt   10  56.860 ± 1.176  ms/op
 * SbeBenchmark.marketUnsafeRoboRead        avgt   10  25.022 ± 1.249  ms/op
 * SbeBenchmark.marketUnsafeRoboReadWrite   avgt   10  41.227 ± 0.612  ms/op
 *
 * === RoboBuffers (Safe) ===
 * SbeBenchmark.carRoboRead                    avgt   10   44.306 ±  1.814  ms/op -- 226 MB/s
 * SbeBenchmark.carRoboReadWrite               avgt   10   72.673 ±  3.227  ms/op -- 138 MB/s
 * SbeBenchmark.marketRoboRead                 avgt   10   28.309 ±  0.242  ms/op -- 353 MB/s
 * SbeBenchmark.marketRoboReadWrite            avgt   10   51.189 ±  0.979  ms/op -- 196 MB/s
 *
 * === Protobuf-Java
 * Benchmark                          Mode  Cnt   Score    Error  Units
 * SbeBenchmark.carProtoRead          avgt   10  64.838 ±  6.028  ms/op -- 153 MB/s
 * SbeBenchmark.carProtoReadWrite     avgt   10  93.787 ±  2.824  ms/op -- 106 MB/s
 * SbeBenchmark.marketProtoRead       avgt   10  46.673 ±  6.769  ms/op -- 214 MB/s
 * SbeBenchmark.marketProtoReadWrite  avgt   10  88.855 ± 14.795  ms/op -- 112 MB/s
 *
 * === Protobuf-JavaLite
 * SbeBenchmark.carProtoRead                   avgt   10  147.068 ±  3.332  ms/op -- 68 MB/s
 * SbeBenchmark.carProtoReadWrite              avgt   10  247.656 ± 28.943  ms/op -- 40 MB/s
 * SbeBenchmark.marketProtoRead                avgt   10  154.648 ± 14.391  ms/op -- 64 MB/s
 * SbeBenchmark.marketProtoReadWrite           avgt   10  251.861 ±  7.509  ms/op -- 40 MB/s
 *
 * @author Florian Enner
 * @since 15 Oct 2019
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(2)
@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class SbeBenchmark {

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(".*" + SbeBenchmark.class.getSimpleName() + ".*Unsafe.*")
                .verbosity(VerboseMode.NORMAL)
                .build();
        new Runner(options).run();
    }

    private static byte[] multiplyToNumBytes(byte[] singleMessage, int maxNumBytes) {
        try {

            int sizePerMessage = ProtoSink.computeRawVarint32Size(singleMessage.length) + singleMessage.length;
            int numMessages = maxNumBytes / sizePerMessage;

            byte[] data = new byte[sizePerMessage * numMessages];
            ProtoSink sink = ProtoSink.createFastest().setOutput(data);
            for (int i = 0; i < numMessages; i++) {
                sink.writeRawVarint32(singleMessage.length);
                sink.writeRawBytes(singleMessage);
            }

            return data;
        } catch (IOException e) {
            throw new AssertionError();
        }
    }

    // ===================== REUSABLE STATE =====================
    final MarketDataIncrementalRefreshTrades marketMsg = new MarketDataIncrementalRefreshTrades();
    final Car carMsg = new Car();
    final ProtoSource source = ProtoSource.createInstance();
    final ProtoSink sink = ProtoSink.createInstance();
    final ProtoSource unsafeSource = ProtoSource.createFastest();
    final ProtoSink unsafeSink = ProtoSink.createFastest();

    // ===================== DATASETS =====================
    final static int MAX_DATASET_SIZE = 10 * 1024 * 1024;
    final byte[] marketDataMessages = multiplyToNumBytes(SbeThroughputBenchmarkRobo.buildMarketData(marketMsg).toByteArray(), MAX_DATASET_SIZE);
    final byte[] carDataMessages = multiplyToNumBytes(SbeThroughputBenchmarkRobo.buildCarData(carMsg).toByteArray(), MAX_DATASET_SIZE);
    final byte[] output = new byte[MAX_DATASET_SIZE];

    // ===================== UNSAFE OPTION DISABLED (e.g. Android) =====================
    @Benchmark
    public int marketRoboRead() throws IOException {
        return readRobo(source.setInput(marketDataMessages), marketMsg);
    }

    @Benchmark
    public int marketRoboReadWrite() throws IOException {
        return readWriteRobo(source.setInput(marketDataMessages), marketMsg, sink.setOutput(output));
    }

    @Benchmark
    public int carRoboRead() throws IOException {
        return readRobo(source.setInput(carDataMessages), carMsg);
    }

    @Benchmark
    public int carRoboReadWrite() throws IOException {
        return readWriteRobo(source.setInput(carDataMessages), carMsg, sink.setOutput(output));
    }

    // ===================== UNSAFE OPTION ENABLED =====================
    @Benchmark
    public int marketUnsafeRoboRead() throws IOException {
        return readRobo(unsafeSource.setInput(marketDataMessages), marketMsg);
    }

    @Benchmark
    public int marketUnsafeRoboReadWrite() throws IOException {
        return readWriteRobo(unsafeSource.setInput(marketDataMessages), marketMsg, unsafeSink.setOutput(output));
    }

    @Benchmark
    public int carUnsafeRoboRead() throws IOException {
        return readRobo(unsafeSource.setInput(carDataMessages), carMsg);
    }

    @Benchmark
    public int carUnsafeRoboReadReadWrite() throws IOException {
        return readWriteRobo(unsafeSource.setInput(carDataMessages), carMsg, unsafeSink.setOutput(output));
    }

    // ===================== STOCK PROTOBUF =====================
    @Benchmark
    public int marketProtoRead() throws IOException {
        return readProto(marketDataMessages, Fix.MarketDataIncrementalRefreshTrades.parser());
    }

    @Benchmark
    public int marketProtoReadWrite() throws IOException {
        return readWriteProto(marketDataMessages, output, Fix.MarketDataIncrementalRefreshTrades.parser());
    }

    @Benchmark
    public int carProtoRead() throws IOException {
        return readProto(carDataMessages, Examples.Car.parser());
    }

    @Benchmark
    public int carProtoReadWrite() throws IOException {
        return readWriteProto(carDataMessages, output, Examples.Car.parser());
    }

    // ===================== UTIL METHODS =====================
    static int readRobo(final ProtoSource source, final ProtoMessage message) throws IOException {
        while (!source.isAtEnd()) {
            final int length = source.readRawVarint32();
            int limit = source.pushLimit(length);
            message.clearQuick().mergeFrom(source);
            source.popLimit(limit);
        }
        return source.getPosition();
    }

    static int readWriteRobo(final ProtoSource source, final ProtoMessage message, final ProtoSink sink) throws IOException {
        while (!source.isAtEnd()) {
            // read delimited
            final int length = source.readRawVarint32();
            int limit = source.pushLimit(length);
            message.clearQuick().mergeFrom(source);
            source.popLimit(limit);

            // write delimited
            sink.writeRawVarint32(message.getSerializedSize());
            message.writeTo(sink);
        }
        return sink.position();
    }

    static <MessageType extends AbstractMessageLite> int readProto(byte[] input, Parser<MessageType> parser) throws IOException {
        CodedInputStream source = CodedInputStream.newInstance(input);
        while (!source.isAtEnd()) {
            final int length = source.readRawVarint32();
            int limit = source.pushLimit(length);
            parser.parseFrom(source);
            source.popLimit(limit);
        }
        return source.getTotalBytesRead();
    }

    static <MessageType extends AbstractMessageLite> int readWriteProto(byte[] input, byte[] output, Parser<MessageType> parser) throws IOException {
        CodedInputStream source = CodedInputStream.newInstance(input);
        CodedOutputStream sink = CodedOutputStream.newInstance(output);
        while (!source.isAtEnd()) {
            // read delimited
            final int length = source.readRawVarint32();
            int limit = source.pushLimit(length);
            MessageType msg = parser.parseFrom(source);
            source.popLimit(limit);

            // write delimited
            sink.writeUInt32NoTag(msg.getSerializedSize());
            msg.writeTo(sink);
        }
        return sink.getTotalBytesWritten();
    }

}