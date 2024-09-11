package com.torquato.racing;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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

    public record RacerFinishedCommand(ActorRef<Racer.Command> racer, Long timestamp) implements Command {
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(RaceController::new);
    }

    //Instance

    private Map<ActorRef<Racer.Command>, Double> positions;
    private Map<ActorRef<Racer.Command>, Long> finishingTimes;
    private long start;
    private final int raceLength = 100;

    private RaceController(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartCommand.class, command -> {
                    this.start = System.currentTimeMillis();
                    this.positions = new HashMap<>(10);
                    this.finishingTimes = new HashMap<>(10);
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
                        return Behaviors.same();
                    });
                })
                .onMessage(RacerUpdateCommand.class, command -> {
                    this.positions.put(command.racer, command.position);
                    return Behaviors.same();
                })
                .onMessage(GetPostionsCommand.class, command -> {
                    for (ActorRef<Racer.Command> racer : this.positions.keySet()) {
                        racer.tell(new Racer.PositionCommand(getContext().getSelf()));
                    }
                    displayRace();
                    return Behaviors.same();
                })
                .onMessage(RacerFinishedCommand.class, command -> {
                    this.finishingTimes.put(command.racer, command.timestamp);
                    if (this.finishingTimes.size() == 10) {
                        return racerCommandReceive();
                    }
                    return Behaviors.same();
                })
                .build();
    }

    private Receive<Command> racerCommandReceive() {
        return newReceiveBuilder()
                .onMessage(GetPostionsCommand.class, command -> {
                    displayResults();
                    return Behaviors.withTimers(timer -> {
                        timer.cancelAll();
                        return Behaviors.stopped();
                    });
                })
                .build();
    }

    private void displayRace() {
        int displayLength = 100;
        for (int i = 0; i < 50; ++i) System.out.println();
        System.out.println("Race has been running for " + ((System.currentTimeMillis() - this.start) / 1000) + " seconds.");
        System.out.println("    " + new String(new char[displayLength]).replace('\0', '='));
        for (ActorRef<Racer.Command> racer : this.positions.keySet()) {
            final String path = racer
                    .path()
                    .toString()
                    .replaceAll("akka://RaceController/user/racer-", "");
            Double v = this.positions.get(racer);
            System.out.println(path + " : " + new String(new char[(int) (v * displayLength / 100)]).replace('\0', '*'));
        }
    }

    private void displayResults() {
        System.out.println("Results:");
        final List<ActorRef<Racer.Command>> ordered = new ArrayList<>(this.finishingTimes.keySet());
        ordered.sort((o1, o2) -> finishingTimes.get(o1).compareTo(finishingTimes.get(o2)));
        for (int i = 0; i < ordered.size(); ++i) {
            final Long timestamp = finishingTimes.get(ordered.get(i));
            final String path = ordered.get(i)
                    .path()
                    .toString()
                    .replaceAll("akka://RaceController/user/racer-", "");
            System.out.printf("%d-%s: %sms\n", (i + 1), path, timestamp - this.start);
        }
    }


}
