package com.torquato.racing;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Random;

@Slf4j
public class Racer extends AbstractBehavior<Racer.Command> {

    private int averageSpeedAdjustmentFactor;
    private Random random;
    private double currentSpeed = 0;

    public Racer(ActorContext<Command> context) {
        super(context);
    }

    //Instance

    public static Behavior<Racer.Command> create() {
        return Behaviors.setup(Racer::new);
    }

    private double getMaxSpeed() {
        double defaultAverageSpeed = 48.2;
        return defaultAverageSpeed * (1 + ((double) averageSpeedAdjustmentFactor / 100));
    }

    private double getDistanceMovedPerSecond() {
        return currentSpeed * 1000 / 3600;
    }

    private void determineNextSpeed(int raceLength, double currentPosition) {
        if (currentPosition < ((double) raceLength / 4)) {
            currentSpeed = currentSpeed + (((getMaxSpeed() - currentSpeed) / 10) * random.nextDouble());
        } else {
            currentSpeed = currentSpeed * (0.5 + random.nextDouble());
        }

        if (currentSpeed > getMaxSpeed())
            currentSpeed = getMaxSpeed();

        if (currentSpeed < 5)
            currentSpeed = 5;

        if (currentPosition > ((double) raceLength / 2) && currentSpeed < getMaxSpeed() / 2) {
            currentSpeed = getMaxSpeed() / 2;
        }
    }

    @Override
    public Receive<Command> createReceive() {
        return notYetStarted();
    }

    private Receive<Command> notYetStarted() {
        return newReceiveBuilder()
                .onMessage(StartCommand.class, (command) -> {
                    this.random = new Random();
                    this.averageSpeedAdjustmentFactor = random.nextInt(30) - 10;
                    return running(command.raceLength, 0);
                })
                .build();
    }

    private Receive<Command> running(final int raceLength, final double currentPosition) {
        return newReceiveBuilder()
                .onMessage(PositionCommand.class, (command) -> {
                    determineNextSpeed(raceLength, currentPosition);
                    double newPosition = currentPosition + getDistanceMovedPerSecond();
                    boolean completed = newPosition >= raceLength;
                    if (completed) {
                        newPosition = raceLength;
                    }
                    command.controller.tell(new RaceController.RacerUpdateCommand(
                            getContext().getSelf(),
                            newPosition
                    ));
                    return completed ? completed(newPosition, System.currentTimeMillis()) : running(raceLength, newPosition);
                })
                .build();
    }

    private Receive<Command> completed(final double currentPosition, final long timestamp) {
        return newReceiveBuilder()
                .onMessage(PositionCommand.class, (command) -> {
                    command.controller.tell(new RaceController.RacerUpdateCommand(
                            getContext().getSelf(),
                            currentPosition
                    ));
                    command.controller.tell(new RaceController.RacerFinishedCommand(
                            getContext().getSelf(),
                            timestamp
                    ));
                    return waitingToStop();
                })
                .build();
    }

    private Receive<Command> waitingToStop() {
        return newReceiveBuilder()
                .onAnyMessage(command -> Behaviors.same())
                .onSignal(PostStop.class, signal -> {
                    log.info("I'm about to terminate. Signal: {}", signal);
                    return Behaviors.same();
                })
                .build();
    }

    public interface Command extends Serializable {
    }

    public record StartCommand(int raceLength) implements Command {
    }

    public record PositionCommand(ActorRef<RaceController.Command> controller) implements Command {
    }


}
