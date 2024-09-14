package com.torquato.blockchain.utils;

import com.torquato.blockchain.model.Block;
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

    public static boolean validateBlock(final Block block) {
        final String dataToEncode = block.getPreviousHash()
                + block.getTransaction().getTimestamp()
                + block.getNonce()
                + block.getTransaction();
        final String checkHash = calculateHash(dataToEncode);
        return block.getHash().equals(checkHash);
    }

}
