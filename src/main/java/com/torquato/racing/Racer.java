package com.torquato.racing;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.util.Random;

public class Racer extends AbstractBehavior<Racer.Command> {

    public interface Command extends Serializable {
    }

    public record StartCommand(int raceLength) implements Command {
    }

    public record PositionCommand(ActorRef<RaceController.Command> controller) implements Command {
    }

    public static Behavior<Racer.Command> create() {
        return Behaviors.setup(Racer::new);
    }

    //Instance

    private int averageSpeedAdjustmentFactor;
    private Random random;

    private double currentSpeed = 0;
    private double currentPosition = 0;
    private int raceLength;

    public Racer(ActorContext<Command> context) {
        super(context);
    }

    private double getMaxSpeed() {
        double defaultAverageSpeed = 48.2;
        return defaultAverageSpeed * (1 + ((double) averageSpeedAdjustmentFactor / 100));
    }

    private double getDistanceMovedPerSecond() {
        return currentSpeed * 1000 / 3600;
    }

    private void determineNextSpeed() {
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
        return newReceiveBuilder()
                .onMessage(StartCommand.class, (command) -> {
                    this.raceLength = command.raceLength;
                    this.random = new Random();
                    this.averageSpeedAdjustmentFactor = random.nextInt(30) - 10;
                    return Behaviors.same();
                })
                .onMessage(PositionCommand.class, (command) -> {
                    determineNextSpeed();
                    currentPosition += getDistanceMovedPerSecond();
                    if (currentPosition > raceLength) {
                        currentPosition = raceLength;
                    }
                    command.controller.tell(new RaceController.RacerUpdateCommand(
                            getContext().getSelf(),
                            this.currentPosition
                    ));
                    return Behaviors.same();
                })
                .build();
    }

}
