package com.tianli.tool.crypto.schnorrkel.sign;

import cafe.cryptography.curve25519.CompressedRistretto;
import cafe.cryptography.curve25519.RistrettoElement;

public class PublicKey {

    private RistrettoElement ristretto;

    private CompressedRistretto compressedRistretto;

    public PublicKey(RistrettoElement ristretto) {
        this.ristretto = ristretto;
        this.compressedRistretto = ristretto.compress();
    }

    public byte[] toPublicKey() {
        return compressedRistretto.toByteArray();
    }

    public RistrettoElement getRistretto() {
        return ristretto;
    }

    public CompressedRistretto getCompressedRistretto() {
        return compressedRistretto;
    }
}
