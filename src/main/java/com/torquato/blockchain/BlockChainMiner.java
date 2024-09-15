package com.torquato.blockchain;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import com.torquato.blockchain.model.Block;
import com.torquato.blockchain.model.BlockChain;
import com.torquato.blockchain.model.BlockValidationException;
import com.torquato.blockchain.model.HashResult;
import com.torquato.blockchain.utils.BlocksData;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

@Slf4j
public class BlockChainMiner {

    private final int difficultyLevel = 5;
    private final BlockChain blocks = new BlockChain();
    private final long start = System.currentTimeMillis();
    private ActorSystem<ManagerBehavior.Command> actorSystem;

    private void mineNextBlock() {
        int nextBlockId = this.blocks.getSize();
        if (nextBlockId < BlocksData.size()) {
            final String lastHash = nextBlockId > 0 ? this.blocks.getLastHash() : "0";
            final Block block = BlocksData.getNextBlock(nextBlockId, lastHash);
            final CompletionStage<HashResult> results = AskPattern.ask(this.actorSystem,
                    me -> new ManagerBehavior.MineBlockCommand(block, me, this.difficultyLevel),
                    Duration.ofSeconds(120),
                    this.actorSystem.scheduler());

            results.whenComplete((reply, failure) -> {

                if (reply == null || !reply.isComplete()) {
                    log.error("ERROR: No valid hash was found for a block");
                } else {
                    block.setHash(reply.getHash());
                    block.setNonce(reply.getNonce());

                    try {
                        this.blocks.addBlock(block);
                        log.info("Block added with hash : {}", block.getHash());
                        log.info("Block added with nonce: {}", block.getNonce());
                        mineNextBlock();
                    } catch (BlockValidationException e) {
                        log.error("ERROR: No valid hash was found for a block");
                    }
                }

            });

        } else {
            long end = System.currentTimeMillis();
            this.actorSystem.terminate();
            this.blocks.printAndValidate();
            log.info("Time taken {} ms.", (end - start));
        }
    }

    public void mineBlocks() {
        this.actorSystem = ActorSystem.create(ManagerBehavior.create(), "BlockChainMiner");
        mineNextBlock();
    }

}
