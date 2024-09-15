package com.torquato.blockchain;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.*;
import com.torquato.blockchain.model.Block;
import com.torquato.blockchain.model.HashResult;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

    //Class stuff
    private final StashBuffer<Command> stashBuffer;
    private ActorRef<HashResult> sender;
    private Block block;
    private int difficulty;
    private int currentNonce = 0;
    private int nonceBlockSize = 0;
    private boolean currentlyMining = false;
    private ManagerBehavior(final ActorContext<Command> context,
                            final StashBuffer<Command> stashBuffer) {
        super(context);
        this.stashBuffer = stashBuffer;
    }

    public static Behavior<Command> create() {
        return Behaviors.withStash(
                10,
                (stash) -> Behaviors.setup((ctx) -> new ManagerBehavior(ctx, stash))
        );
    }

    @Override
    public Receive<Command> createReceive() {
        return waitingMessageReceiver();
    }

    private Receive<Command> waitingMessageReceiver() {
        return newReceiveBuilder()
                .onSignal(Terminated.class, (handler) -> Behaviors.same())
                .onMessage(MineBlockCommand.class, (command) -> {
                    this.currentlyMining = true;
                    this.sender = command.sender;
                    this.block = command.block;
                    this.difficulty = command.difficulty;
                    this.nonceBlockSize = command.nonceBlockSize;

                    for (int i = 0; i < Runtime.getRuntime().availableProcessors() + 1; i++) {
                        startNextWorker();
                    }

                    return processingMessageReceiver();
                })
                .build();
    }

    private Receive<Command> processingMessageReceiver() {
        return newReceiveBuilder()
                .onSignal(Terminated.class, (signal) -> {
                    startNextWorker();

                    return Behaviors.same();
                })
                .onMessage(MineBlockCommand.class, (command) -> {
                    log.warn("Delaying a mining message...");
                    if (!this.stashBuffer.isFull()) {
                        this.stashBuffer.stash(command);
                    } else {
                        // Sending again to the queue, alternatively it can be dropped
                        getContext().getSelf().tell(command);
                    }

                    return Behaviors.same();
                })
                .onMessage(HashResultCommand.class, (command) -> {
                    this.currentlyMining = false;

                    stopAllChildren();
                    this.sender.tell(command.hashResult);

                    return this.stashBuffer.unstashAll(waitingMessageReceiver());
                })
                .build();
    }

    private void stopAllChildren() {
        getContext().getChildren()
                .forEach(a -> getContext().stop(a));
    }

    private void startNextWorker() {
        if (!this.currentlyMining) return;

        final Behavior<WorkerBehavior.Command> workerBehavior = Behaviors.supervise(WorkerBehavior.create())
                .onFailure(SupervisorStrategy.resume());
        final ActorRef<WorkerBehavior.Command> worker = getContext().spawn(
                workerBehavior,
                "worker-" + this.currentNonce
        );
        getContext().watch(worker);

        worker.tell(new WorkerBehavior.BlockMiningCommand(
                this.block,
                this.currentNonce * this.nonceBlockSize,
                difficulty,
                nonceBlockSize,
                getContext().getSelf()
        ));

        this.currentNonce++;
    }

    public interface Command extends Serializable {
    }

    public record MineBlockCommand(Block block,
                                   ActorRef<HashResult> sender,
                                   int difficulty,
                                   int nonceBlockSize) implements Command {
    }

    public record HashResultCommand(HashResult hashResult) implements Command {
    }


}
