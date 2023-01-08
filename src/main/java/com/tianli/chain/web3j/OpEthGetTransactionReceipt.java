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
 * @Date 2022-07-11 14:42
 */
public class OpEthGetTransactionReceipt extends Response<OpTransactionReceipt> {
    public OpEthGetTransactionReceipt() {
    }

    public Optional<OpTransactionReceipt> getTransactionReceipt() {
        return Optional.ofNullable(this.getResult());
    }

    public static class ResponseDeserialiser extends JsonDeserializer<OpTransactionReceipt> {
        private ObjectReader objectReader = ObjectMapperFactory.getObjectReader();

        public ResponseDeserialiser() {
        }

        @Override
        public OpTransactionReceipt deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return jsonParser.getCurrentToken() != JsonToken.VALUE_NULL ? this.objectReader.readValue(jsonParser, OpTransactionReceipt.class) : null;
        }
    }
}
