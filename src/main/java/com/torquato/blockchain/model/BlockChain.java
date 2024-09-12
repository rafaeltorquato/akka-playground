package com.torquato.blockchain.model;

import com.torquato.blockchain.utils.BlockChainUtils;

import java.util.LinkedList;

public class BlockChain {

	private LinkedList<Block> blocks;
	
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
		for (Block block : blocks) {
			System.out.println("Block " + block.getTransaction().getId() + " ");
			System.out.println(block.getTransaction());
			
			if (block.getPreviousHash().equals(lastHash)) {
				System.out.print("Last hash matches ");
			} else {
				System.out.print("Last hash doesn't match ");
			}
			
			if (BlockChainUtils.validateBlock(block)) {
				System.out.println("and hash is valid");
			} else {
				System.out.println("and hash is invalid");
			}
			
			lastHash = block.getHash();
			
		}
	}

	public String getLastHash() {
		if (blocks.size() > 0)
			return blocks.getLast().getHash();
		return null;
	}

	public int getSize() {
		return blocks.size();
	}

}
