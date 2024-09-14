package com.torquato.blockchain.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(of = {"nonce", "hash"})
public class HashResult {

    private int nonce;
    private String hash;
    private boolean complete = false;

    public HashResult() {
    }

    public int getNonce() {
        return nonce;
    }

    public String getHash() {
        return hash;
    }

    public boolean isComplete() {
        return complete;
    }

    public void foundAHash(String hash, int nonce) {
        this.hash = hash;
        this.nonce = nonce;
        this.complete = true;
    }

}
