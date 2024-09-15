package com.torquato.blockchain.model;

import com.torquato.blockchain.utils.BlockChainUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;

@Slf4j
public class BlockChain {

    private final LinkedList<Block> blocks;

    public BlockChain() {
        blocks = new LinkedList<>();
    }

    public void addBlock(Block block) throws BlockValidationException {
        String lastHash = "0";

        if (!this.blocks.isEmpty()) {
            lastHash = this.blocks.getLast().getHash();
        }

        if (!lastHash.equals(block.getPreviousHash())) {
            throw new BlockValidationException();
        }

        if (!BlockChainUtils.validateBlock(block)) {
            throw new BlockValidationException();
        }

        this.blocks.add(block);
    }

    public void printAndValidate() {
        String lastHash = "0";
        for (Block block : this.blocks) {
            System.out.println("Block " + block.getTransaction().getId() + " ");
            System.out.println(block.getTransaction());

            if (block.getPreviousHash().equals(lastHash)) {
                log.info("Last hash matches ");
            } else {
                log.info("Last hash doesn't match ");
            }

            if (BlockChainUtils.validateBlock(block)) {
                log.info("and hash is valid");
            } else {
                log.info("and hash is invalid");
            }

            lastHash = block.getHash();

        }
    }

    public String getLastHash() {
        if (!blocks.isEmpty())
            return blocks.getLast().getHash();
        return null;
    }

    public int getSize() {
        return blocks.size();
    }

}
