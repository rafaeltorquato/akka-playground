package com.torquato.racing;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class RaceController extends AbstractBehavior<RaceController.Command> {

    public static final String TIMER_KEY = "race-controller-timer";

    public interface Command extends Serializable {
    }

    public record StartCommand() implements Command {
    }

    public record RacerUpdateCommand(ActorRef<Racer.Command> racer,
                                     double position) implements Command {
    }

    private record GetPostionsCommand() implements Command {
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(RaceController::new);
    }

    //Instance

    private Map<ActorRef<Racer.Command>, Double> positions;
    private long start;
    private int raceLength = 100;

    private RaceController(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartCommand.class, command -> {
                    this.start = System.currentTimeMillis();
                    this.positions = new HashMap<>(10);
                    for (int i = 1; i <= 10; i++) {
                        ActorRef<Racer.Command> racer = getContext().spawn(Racer.create(), "racer-" + i);
                        this.positions.put(racer, 0.0);
                        racer.tell(new Racer.StartCommand(this.raceLength));
                    }
                    return Behaviors.withTimers(timer -> {
                        timer.startTimerAtFixedRate(
                                TIMER_KEY,
                                new GetPostionsCommand(),
                                Duration.ofSeconds(1)
                        );
                        return this;
                    });
                })
                .onMessage(RacerUpdateCommand.class, command -> {
                    this.positions.put(command.racer, command.position);
                    return this;
                })
                .onMessage(GetPostionsCommand.class, command -> {
                    for (ActorRef<Racer.Command> racer : this.positions.keySet()) {
                        racer.tell(new Racer.PositionCommand(getContext().getSelf()));
                    }
                    displayRace();
                    return this;
                })
                .build();
    }

    private void displayRace() {
        int displayLength = 160;
        for (int i = 0; i < 50; ++i) System.out.println();
        System.out.println("Race has been running for " + ((System.currentTimeMillis() - this.start) / 1000) + " seconds.");
        System.out.println("    " + new String(new char[displayLength]).replace('\0', '='));
        positions.forEach((k, v) -> {
            String path = k.path().toString();
            path = path.replaceAll("akka://RaceController/user/", "");
            System.out.println(path + " : " + new String(new char[(int) (v * displayLength / 100)]).replace('\0', '*'));
        });
    }


}
