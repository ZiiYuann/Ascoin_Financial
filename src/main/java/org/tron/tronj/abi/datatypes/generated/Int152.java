package org.tron.tronj.abi.datatypes.generated;

import org.tron.tronj.abi.datatypes.Int;

import java.math.BigInteger;

/**
 * Auto generated code.
 * <p><strong>Do not modifiy!</strong>
 * <p>Please use org.tron.tronj.codegen.AbiTypesGenerator in the
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 */
public class Int152 extends Int {
    public static final Int152 DEFAULT = new Int152(BigInteger.ZERO);

    public Int152(BigInteger value) {
        super(152, value);
    }

    public Int152(long value) {
        this(BigInteger.valueOf(value));
    }
}
