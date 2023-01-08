package com.tianli.chain.web3j;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectReader;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.Response;

import java.io.IOException;
import java.util.Optional;

/**
 * @Author cs
 * @Date 2022-07-14 14:07
 */
public class ArbitrumEthGetTransactionReceipt extends Response<ArbitrumTransactionReceipt> {
    public ArbitrumEthGetTransactionReceipt() {
    }

    public Optional<ArbitrumTransactionReceipt> getTransactionReceipt() {
        return Optional.ofNullable(this.getResult());
    }

    public static class ResponseDeserialiser extends JsonDeserializer<ArbitrumTransactionReceipt> {
        private ObjectReader objectReader = ObjectMapperFactory.getObjectReader();

        public ResponseDeserialiser() {
        }

        @Override
        public ArbitrumTransactionReceipt deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return jsonParser.getCurrentToken() != JsonToken.VALUE_NULL ? this.objectReader.readValue(jsonParser, ArbitrumTransactionReceipt.class) : null;
        }
    }
}
