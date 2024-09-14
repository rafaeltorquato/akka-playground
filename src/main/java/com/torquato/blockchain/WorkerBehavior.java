package com.torquato.blockchain;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.torquato.blockchain.model.Block;
import com.torquato.blockchain.model.HashResult;
import com.torquato.blockchain.utils.BlockChainUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
public class WorkerBehavior extends AbstractBehavior<WorkerBehavior.Command> {

    public interface Command extends Serializable {
    }

    public record BlockMiningCommand(Block block,
                                     int startNonce,
                                     int difficultyLevel,
                                     ActorRef<HashResult> targetActor) implements Command {
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(WorkerBehavior::new);
    }

    private WorkerBehavior(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(BlockMiningCommand.class, command -> {
                    final Block block = command.block;
                    String hash = new String(new char[command.difficultyLevel]).replace("\0", "X");
                    final String target = new String(new char[command.difficultyLevel]).replace("\0", "0");

                    int nonce = command.startNonce;
                    while (!hash.substring(0, command.difficultyLevel).equals(target) && nonce < command.startNonce + 1000) {
                        nonce++;
                        final String dataToEncode = block.getPreviousHash()
                                + block.getTransaction().getTimestamp()
                                + nonce
                                + block.getTransaction();
                        hash = BlockChainUtils.calculateHash(dataToEncode);
                    }
                    if (hash.substring(0, command.difficultyLevel).equals(target)) {
                        HashResult hashResult = new HashResult();
                        hashResult.foundAHash(hash, nonce);
                        command.targetActor.tell(hashResult);
                    }
                    return Behaviors.same();
                })
                .build();
    }

}
