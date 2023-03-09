package com.tianli.address;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-19
 **/

public class PushHttpClient {

    private static CloseableHttpClient client = null;

    private PushHttpClient() {

    }

    public static synchronized CloseableHttpClient getClient() {
        if (client == null) {
            synchronized (PushHttpClient.class) {
                if (client == null) {
                    client = HttpClientBuilder.create().build();
                }
            }
        }

        return client;
    }


}
