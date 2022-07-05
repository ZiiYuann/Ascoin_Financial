package com.tianli.user;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import lombok.Data;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

@Data
public class SignedRequestDecoder {
	
	private String secret;

	private ObjectMapper objectMapper;

	/**
	 * @param secret the application secret used in creating and verifying the signature of the signed request.
	 */
	public SignedRequestDecoder(String secret) {
		this.secret = secret;
		this.objectMapper = new ObjectMapper();
		this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		this.objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
	}
	

	@SuppressWarnings("unchecked")
	public Map<String, ?> decodeSignedRequest(String signedRequest) {
		return decodeSignedRequest(signedRequest, Map.class);
	}


	public <T> T decodeSignedRequest(String signedRequest, Class<T> type) {
		String[] split = signedRequest.split("\\.");
		String encodedSignature = split[0];
		String payload = split[1];		
		String decoded = base64DecodeToString(payload);		
		byte[] signature = base64DecodeToBytes(encodedSignature);
		try {
			T data = objectMapper.readValue(decoded, type);			
			String algorithm = objectMapper.readTree(decoded).get("algorithm").textValue();
			if (algorithm == null || !algorithm.equals("HMAC-SHA256")) {
				throw new RuntimeException("Unknown encryption algorithm: " + algorithm);
			}			
			byte[] expectedSignature = encrypt(payload, secret);
			if (!Arrays.equals(expectedSignature, signature)) {
				throw new RuntimeException("Invalid signature.");
			}			
			return data;
		} catch (IOException e) {
			throw new RuntimeException("Error parsing payload.", e);
		}
	}

	private String padForBase64(String base64) {
		return base64 + PADDING.substring(0, (4-base64.length() % 4) % 4);
	}

	private byte[] base64DecodeToBytes(String in) {
		return Base64.getDecoder().decode(padForBase64(in.replace('_', '/').replace('-', '+')).getBytes());
	}

	private String base64DecodeToString(String in) {
		return new String(base64DecodeToBytes(in));
	}

	private byte[] encrypt(String base, String key) {
		try {
			SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), HMAC_SHA256_MAC_NAME);
			Mac mac = Mac.getInstance(HMAC_SHA256_MAC_NAME);
			mac.init(secretKeySpec);
			return mac.doFinal(base.getBytes());
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		} catch (InvalidKeyException e) {
			throw new IllegalStateException(e);
		}
	}
	
	private static final String PADDING = "===";

	private static final String HMAC_SHA256_MAC_NAME = "HMACSHA256";

}