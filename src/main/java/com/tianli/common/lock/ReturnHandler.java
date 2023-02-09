package com.tianli.common.lock;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-09
 **/
public interface ReturnHandler<T> {

    T execute();
}
