package com.torquato.blockchain.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Block {

    private final String previousHash;
    private final Transaction transaction;
    private int nonce;
    private String hash;

    public Block(Transaction transaction, String previousHash) {
        this.previousHash = previousHash;
        this.transaction = transaction;
    }


}
