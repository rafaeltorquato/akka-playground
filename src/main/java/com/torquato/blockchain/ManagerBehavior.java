package com.torquato.blockchain;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.torquato.blockchain.model.Block;
import com.torquato.blockchain.model.HashResult;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

    public interface Command extends Serializable {
    }

    public record MineBlockCommand(Block block,
                                   ActorRef<HashResult> sender,
                                   int difficulty) implements Command {
    }

    public record HashResultCommand(HashResult hashResult) implements Command {
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(ManagerBehavior::new);
    }

    //Class stuff

    private ActorRef<HashResult> sender;
    private Block block;
    private int difficulty;
    private int currentNonce = 0;
    private boolean currentlyMining = false;

    private ManagerBehavior(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onSignal(Terminated.class, (handler) -> {
                    startNextWorker();

                    return Behaviors.same();
                })
                .onMessage(MineBlockCommand.class, (command) -> {
                    this.sender = command.sender;
                    this.block = command.block;
                    this.difficulty = command.difficulty;
                    this.currentlyMining = true;

                    for (int i = 0; i < Runtime.getRuntime().availableProcessors() + 1; i++) {
                        startNextWorker();
                    }

                    return Behaviors.same();
                })
                .onMessage(HashResultCommand.class, (command) -> {
                    stopAllChildren();
                    this.sender.tell(command.hashResult);
                    this.currentlyMining = false;

                    return Behaviors.same();
                })
                .build();
    }

    private void stopAllChildren() {
        getContext().getChildren()
                .forEach(a -> getContext().stop(a));
    }

    private void startNextWorker() {
        if (!currentlyMining) return;

        final Behavior<WorkerBehavior.Command> workerBehavior = Behaviors.supervise(WorkerBehavior.create())
                .onFailure(SupervisorStrategy.resume());
        final ActorRef<WorkerBehavior.Command> worker = getContext().spawn(
                workerBehavior,
                "worker-" + currentNonce
        );
        getContext().watch(worker);

        worker.tell(new WorkerBehavior.BlockMiningCommand(
                this.block,
                currentNonce * 1000,
                difficulty,
                getContext().getSelf()
        ));

        currentNonce++;
    }


}
