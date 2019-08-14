package us.hebi.robobuf;

import com.google.protobuf.ByteString;
import org.junit.Test;
import us.hebi.robobuf.java.ForeignEnum;
import us.hebi.robobuf.java.ForeignMessage;
import us.hebi.robobuf.java.RepeatedPackables;
import us.hebi.robobuf.java.TestAllTypes;
import us.hebi.robobuf.java.TestAllTypes.NestedEnum;
import us.hebi.robobuf.java.external.ImportEnum;

import java.io.IOException;
import java.util.Arrays;

/**
 * "ground-truth" data generated by protobuf-Java
 *
 * @author Florian Enner
 * @since 13 Aug 2019
 */
public class TestSamples {

    /**
     * Make sure Java bindings can still parse messages after doing a round-trip
     * TODO: maybe also check for equality?
     */
    @Test
    public void testCompatibility() throws IOException {
        // primitives
        TestAllTypes.parseFrom(us.hebi.robobuf.robo
                .TestAllTypes.parseFrom(optionalPrimitives())
                .toByteArray());

        // enum
        TestAllTypes.parseFrom(us.hebi.robobuf.robo
                .TestAllTypes.parseFrom(optionalEnums())
                .toByteArray());

        // packed
        RepeatedPackables.Packed.parseFrom(us.hebi.robobuf.robo.RepeatedPackables
                .Packed.parseFrom(repeatedPackablesPacked())
                .toByteArray());

        // non packed
        RepeatedPackables.NonPacked.parseFrom(us.hebi.robobuf.robo.RepeatedPackables
                .NonPacked.parseFrom(repeatedPackablesNonPacked())
                .toByteArray());

    }

    static byte[] optionalPrimitives() {
        TestAllTypes msg = TestAllTypes.newBuilder()
                .setId(99) // needs to be set
                .setOptionalBool(true)
                .setOptionalDouble(100.0d)
                .setOptionalFloat(101.0f)
                .setOptionalFixed32(102)
                .setOptionalFixed64(103)
                .setOptionalSfixed32(104)
                .setOptionalSfixed64(105)
                .setOptionalSint32(106)
                .setOptionalSint64(107)
                .setOptionalInt32(108)
                .setOptionalInt64(109)
                .setOptionalUint32(110)
                .setOptionalUint64(111)
                .build();
        return msg.toByteArray();
    }

    static byte[] repeatedPackablesNonPacked() {
        RepeatedPackables.NonPacked msg = RepeatedPackables.NonPacked.newBuilder()
                .addAllBools(Arrays.asList(true, false, true, true))
                .addAllDoubles(Arrays.asList(Double.POSITIVE_INFINITY, -2d, 3d, 4d))
                .addAllFloats(Arrays.asList(10f, 20f, -30f, Float.NaN))
                .addAllFixed32S(Arrays.asList(2, -2, 4, 67423))
                .addAllFixed64S(Arrays.asList(3231313L, 6L, -7L, 8L))
                .addAllSfixed32S(Arrays.asList(2, -3, 4, 5))
                .addAllSfixed64S(Arrays.asList(5L, -6L, 7L, -8L))
                .addAllSint32S(Arrays.asList(2, -3, 4, 5))
                .addAllSint64S(Arrays.asList(5L, 6L, -7L, 8L))
                .addAllInt32S(Arrays.asList(2, 3, -4, 5))
                .addAllInt64S(Arrays.asList(5L, -6L, 7L, 8L))
                .addAllUint32S(Arrays.asList(2, 300, 4, 5))
                .addAllUint64S(Arrays.asList(5L, 6L, 23L << 40, 8L))
                .build();
        return msg.toByteArray();
    }

    static byte[] repeatedPackablesPacked() {
        RepeatedPackables.Packed msg = RepeatedPackables.Packed.newBuilder()
                .addAllBools(Arrays.asList(true, false, true, true))
                .addAllDoubles(Arrays.asList(Double.POSITIVE_INFINITY, -2d, 3d, 4d))
                .addAllFloats(Arrays.asList(10f, 20f, -30f, Float.NaN))
                .addAllFixed32S(Arrays.asList(2, -2, 4, 67423))
                .addAllFixed64S(Arrays.asList(3231313L, 6L, -7L, 8L))
                .addAllSfixed32S(Arrays.asList(2, -3, 4, 5))
                .addAllSfixed64S(Arrays.asList(5L, -6L, 7L, -8L))
                .addAllSint32S(Arrays.asList(2, -3, 4, 5))
                .addAllSint64S(Arrays.asList(5L, 6L, -7L, 8L))
                .addAllInt32S(Arrays.asList(2, 3, -4, 5))
                .addAllInt64S(Arrays.asList(5L, -6L, 7L, 8L))
                .addAllUint32S(Arrays.asList(2, 300, 4, 5))
                .addAllUint64S(Arrays.asList(5L, 6L, 23L << 40, 8L))
                .build();
        return msg.toByteArray();
    }

    static byte[] optionalEnums() {
        TestAllTypes msg = TestAllTypes.newBuilder()
                .setId(0)
                .setOptionalNestedEnum(NestedEnum.FOO)
                .setOptionalForeignEnum(ForeignEnum.FOREIGN_BAR)
                .setOptionalImportEnum(ImportEnum.IMPORT_BAZ)
                .build();
        return msg.toByteArray();
    }

    static byte[] repeatedEnums(){
        return TestAllTypes.newBuilder()
                .setId(0)
                .addRepeatedNestedEnum(NestedEnum.FOO)
                .addRepeatedNestedEnum(NestedEnum.BAR)
                .addRepeatedNestedEnum(NestedEnum.BAZ)
                .addRepeatedNestedEnum(NestedEnum.BAZ)
                .build()
                .toByteArray();
    }

    static byte[] optionalString() {
        TestAllTypes msg = TestAllTypes.newBuilder()
                .setId(0)
                .setOptionalNestedEnum(NestedEnum.FOO)
                .setOptionalForeignEnum(ForeignEnum.FOREIGN_BAR)
                .setOptionalImportEnum(ImportEnum.IMPORT_BAZ)
                .build();
        return msg.toByteArray();
    }

    static byte[] optionalMessages() {
        TestAllTypes msg = TestAllTypes.newBuilder()
                .setId(0)
                .setOptionalNestedMessage(TestAllTypes.NestedMessage.newBuilder().setBb(2).build())
                .setOptionalForeignMessage(ForeignMessage.newBuilder().setC(3).build())
                .build();
        return msg.toByteArray();
    }

    static byte[] repeatedMessages(){
        return TestAllTypes.newBuilder()
                .setId(0)
                .addRepeatedForeignMessage(ForeignMessage.newBuilder().setC(0))
                .addRepeatedForeignMessage(ForeignMessage.newBuilder().setC(1))
                .addRepeatedForeignMessage(ForeignMessage.newBuilder().setC(2))
                .build()
                .toByteArray();
    }

    static byte[] repeatedStrings() {
        return TestAllTypes.newBuilder()
                .setId(0)
                .addAllRepeatedString(Arrays.asList("hello", "world", "ascii", "utf8\uD83D\uDCA9"))
                .build()
                .toByteArray();
    }

    static byte[] optionalBytes() {
        return TestAllTypes.newBuilder()
                .setId(0)
                .setDefaultBytes(ByteString.copyFromUtf8("utf8\uD83D\uDCA9"))
                .build()
                .toByteArray();
    }

    static byte[] repeatedBytes(){
        return TestAllTypes.newBuilder()
                .setId(0)
                .addRepeatedBytes(ByteString.copyFromUtf8("ascii"))
                .addRepeatedBytes(ByteString.copyFromUtf8("utf8\uD83D\uDCA9"))
                .build()
                .toByteArray();
    }

}


