package org.tron.tronj.abi.datatypes.generated;

import org.tron.tronj.abi.datatypes.StaticArray;
import org.tron.tronj.abi.datatypes.Type;

import java.util.List;

/**
 * Auto generated code.
 * <p><strong>Do not modifiy!</strong>
 * <p>Please use org.tron.tronj.codegen.AbiTypesGenerator in the
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 */
public class StaticArray20<T extends Type> extends StaticArray<T> {
    @Deprecated
    public StaticArray20(List<T> values) {
        super(20, values);
    }

    @Deprecated
    @SafeVarargs
    public StaticArray20(T... values) {
        super(20, values);
    }

    public StaticArray20(Class<T> type, List<T> values) {
        super(type, 20, values);
    }

    @SafeVarargs
    public StaticArray20(Class<T> type, T... values) {
        super(type, 20, values);
    }
}
