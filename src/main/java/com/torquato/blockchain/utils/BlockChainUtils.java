package com.torquato.blockchain.utils;

import com.torquato.blockchain.model.Block;
import com.torquato.blockchain.model.HashResult;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class BlockChainUtils {

    @SneakyThrows
    public static String calculateHash(final String data) {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        final byte[] rawHash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
        final StringBuilder hexString = new StringBuilder();
        for (byte hash : rawHash) {
            final String hex = Integer.toHexString(0xff & hash);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }


    public static HashResult mineBlock(final Block block,
                                       final int difficultyLevel,
                                       final int startNonce,
                                       final int endNonce) {
        String hash = new String(new char[difficultyLevel]).replace("\0", "X");
        final String target = new String(new char[difficultyLevel]).replace("\0", "0");

        int nonce = startNonce;
        while (!hash.substring(0, difficultyLevel).equals(target) && nonce < endNonce) {
            nonce++;
            final String dataToEncode = block.getPreviousHash()
                    + block.getTransaction().getTimestamp()
                    + nonce
                    + block.getTransaction();
            hash = calculateHash(dataToEncode);
        }
        if (hash.substring(0, difficultyLevel).equals(target)) {
            HashResult hashResult = new HashResult();
            hashResult.foundAHash(hash, nonce);
            return hashResult;
        } else {
            return null;
        }
    }

    public static boolean validateBlock(final Block block) {
        final String dataToEncode = block.getPreviousHash()
                + block.getTransaction().getTimestamp()
                + block.getNonce()
                + block.getTransaction();
        final String checkHash = calculateHash(dataToEncode);
        return block.getHash().equals(checkHash);
    }

}
