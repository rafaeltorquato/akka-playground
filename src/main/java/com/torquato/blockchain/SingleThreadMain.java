//package com.torquato.blockchain;
//
//
//import com.torquato.blockchain.model.Block;
//import com.torquato.blockchain.model.BlockChain;
//import com.torquato.blockchain.model.BlockValidationException;
//import com.torquato.blockchain.model.HashResult;
//import com.torquato.blockchain.utils.BlockChainUtils;
//import com.torquato.blockchain.utils.BlocksData;
//
//public class SingleThreadMain {
//
//    public static void main(String[] args) throws BlockValidationException {
//
//        int difficultyLevel = 6;
//
//        Long start = System.currentTimeMillis();
//        BlockChain blocks = new BlockChain();
//
//        String lastHash = "0";
//        for (int i = 0; i < 10; i++) {
//            Block nextBlock = BlocksData.getNextBlock(i, lastHash);
//
//            HashResult hashResult = BlockChainUtils.mineBlock(nextBlock, difficultyLevel, 0, 100000000);
//            if (hashResult == null) {
//                throw new RuntimeException("Didn't find a valid hash for block " + i);
//            }
//
//            nextBlock.setHash(hashResult.getHash());
//            nextBlock.setNonce(hashResult.getNonce());
//            blocks.addBlock(nextBlock);
//            System.out.println("Block " + i + " hash : " + nextBlock.getHash());
//            System.out.println("Block " + i + " nonce: " + nextBlock.getNonce());
//            lastHash = nextBlock.getHash();
//        }
//
//        Long end = System.currentTimeMillis();
//        blocks.printAndValidate();
//
//        System.out.println("Time taken " + (end - start) + " ms.");
//    }
//}
