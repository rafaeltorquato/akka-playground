//package com.torquato.blockchain;
//
//import com.torquato.blockchain.model.Block;
//import com.torquato.blockchain.model.BlockChain;
//import com.torquato.blockchain.model.BlockValidationException;
//import com.torquato.blockchain.model.HashResult;
//import com.torquato.blockchain.utils.BlocksData;
//
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class MultiThreadMain {
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
//            ExecutorService es = Executors.newFixedThreadPool(10);
//            Block nextBlock = BlocksData.getNextBlock(i, lastHash);
//            HashResult hashResult = new HashResult();
//            Thread resultsThread = new Thread(new CheckForResults(hashResult));
//            resultsThread.start();
//
//            for (int nonceSeed = 0; nonceSeed < 10000; nonceSeed++) {
//                BlockMiner miner = new BlockMiner(nextBlock, nonceSeed * 1000, hashResult, difficultyLevel);
//                es.execute(miner);
//            }
//
//            try {
//                resultsThread.join();
//                es.shutdownNow();
//                if (!hashResult.isComplete()) {
//                    throw new RuntimeException("Failed to find a has for block " + i);
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//            nextBlock.setHash(hashResult.getHash());
//            nextBlock.setNonce(hashResult.getNonce());
//
//            blocks.addBlock(nextBlock);
//            System.out.println("Block " + i + " hash : " + nextBlock.getHash());
//            System.out.println("Block " + i + " nonce: " + nextBlock.getNonce());
//            lastHash = nextBlock.getHash();
//        }
//
//        Long end = System.currentTimeMillis();
//
//        blocks.printAndValidate();
//
//        System.out.println("Time taken " + (end - start) + " ms.");
//    }
//}
