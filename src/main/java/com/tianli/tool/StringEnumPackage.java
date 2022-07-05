package com.tianli.tool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

/**
 * Created by wangqiyun on 2018/7/18.
 */
public class StringEnumPackage extends ArrayList<String> {
    public StringEnumPackage(Class<? extends Enum> c) {
        Enum[] constants = c.getEnumConstants();
        for (Enum e : constants) {
            this.add(e.name());
        }
    }

    public StringEnumPackage(String... str) {
        Collections.addAll(this, str);
    }

    public StringEnumPackage(Supplier<Collection<String>> supplier) {
        this.addAll(supplier.get());
    }
}
