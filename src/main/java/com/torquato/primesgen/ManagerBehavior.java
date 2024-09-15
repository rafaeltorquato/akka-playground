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
import java.time.Duration;
import java.util.SortedSet;
import java.util.TreeSet;

@Slf4j
public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {

    public static final int MAX_PRIMES = 20;
    private final SortedSet<BigInteger> sortedSet = new TreeSet<>();
    private ActorRef<SortedSet<BigInteger>> instructor;

    private ManagerBehavior(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(ManagerBehavior::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(InstructionCommand.class, (command) -> {
                    if ("start".equals(command.instruction)) {
                        this.instructor = command.instructor;
                        for (int i = 1; i <= MAX_PRIMES; i++) {
                            final ActorRef<WorkerBehavior.Command> worker = getContext().spawn(
                                    WorkerBehavior.create(),
                                    "worker-" + i
                            );
                            askWorker(worker);
                        }
                    }

                    return Behaviors.same();
                })
                .onMessage(ResultCommand.class, (command) -> {
                    sortedSet.add(command.number);
                    log.info("Primes count: {}", sortedSet.size());
                    if (sortedSet.size() == MAX_PRIMES) {
                        this.instructor.tell(this.sortedSet);
                    }

                    return Behaviors.same();
                })
                .onMessage(NoResultCommand.class, (command) -> {
                    log.warn("Retrying Worker {}...", command.worker.path());
                    askWorker(command.worker);

                    return Behaviors.same();
                })
                .build();
    }

    private void askWorker(ActorRef<WorkerBehavior.Command> worker) {
        getContext().ask(
                Command.class,
                worker,
                Duration.ofSeconds(2),
                (me) -> new WorkerBehavior.Command("start", me),
                (response, throwable) -> {
                    if (response != null) {
                        return response;
                    }
                    log.warn("Worker {} failed to respond.", worker.path());
                    return new NoResultCommand(worker);

                });
    }
    public interface Command extends Serializable {
    }

    public record InstructionCommand(String instruction,
                                     ActorRef<SortedSet<BigInteger>> instructor) implements Command {
    }

    public record ResultCommand(BigInteger number) implements Command {
    }

    public record NoResultCommand(ActorRef<WorkerBehavior.Command> worker) implements Command {
    }

}
