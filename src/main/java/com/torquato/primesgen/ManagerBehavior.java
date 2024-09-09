package com.torquato.primesgen;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.SortedSet;
import java.util.TreeSet;

@Slf4j
public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

    public static final int MAX_PRIMES = 20;

    public interface Command extends Serializable {
    }

    public record InstructionCommand(String instruction) implements Command {
    }

    public record ResultCommand(BigInteger number) implements Command {
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(ManagerBehavior::new);
    }

    private ManagerBehavior(ActorContext<Command> context) {
        super(context);
    }

    private final SortedSet<BigInteger> sortedSet = new TreeSet<>();

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(InstructionCommand.class, (command) -> {
                    if ("start".equals(command.instruction)) {
                        executeStart();
                    }
                    return Behaviors.same();
                })
                .onMessage(ResultCommand.class, (command) -> {
                    sortedSet.add(command.number);
                    log.info("Primes count: {}", sortedSet.size());
                    if (sortedSet.size() == MAX_PRIMES) {
                        log.info("All primes: {}", sortedSet);
                    }
                    return Behaviors.same();
                })
                .build();
    }

    private void executeStart() {
        for (int i = 1; i <= MAX_PRIMES; i++) {
            final ActorRef<WorkerBehavior.Command> worker = getContext().spawn(
                    WorkerBehavior.create(),
                    "worker-" + i
            );
            final WorkerBehavior.Command command = new WorkerBehavior.Command(
                    "start",
                    getContext().getSelf()
            );
            worker.tell(command);
            worker.tell(command);
        }
    }
}
