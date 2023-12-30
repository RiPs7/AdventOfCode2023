package com.rips7.day;

import com.rips7.util.maths.Combinatorics.Pair;
import com.rips7.util.maths.Combinatorics.Triplet;
import com.rips7.util.maths.Maths;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Day20 implements Day<Long> {

    @Override
    public Long part1(String input) {
        return Machine.parse(input).pushButton(1000);
    }

    @Override
    public Long part2(String input) {
        return Machine.parse(input).pushButtonUntilOutput();
    }

    private record Machine(Map<Module, List<Module>> connections) {
        private static Machine parse(final String input) {
            final Map<String, String[]> allRawConnections = Arrays.stream(input.split("\n"))
                .map(line -> line.split(" -> "))
                .collect(Collectors.toMap(p -> p[0], p -> p[1].split(", ")));
            final Set<Module> modules = allRawConnections.keySet().stream()
                .map(moduleName -> switch(moduleName.charAt(0)) {
                    case '&' -> new Conjunction(moduleName.substring(1));
                    case '%' -> new FlipFlop(moduleName.substring(1));
                    case 'b' -> new Broadcaster();
                    default -> throw new RuntimeException("Cannot parse module %s".formatted(moduleName));
                })
                .collect(Collectors.toCollection(HashSet::new));
            allRawConnections.values().stream()
                .flatMap(Arrays::stream)
                .filter(moduleName -> modules.stream().map(m -> m.name).noneMatch(name -> name.equals(moduleName)))
                .forEach(moduleName -> modules.add(new Output(moduleName)));
            final Map<Module, List<Module>> connections = modules.stream()
                .collect(Collectors.toMap(Function.identity(), e -> new ArrayList<>()));
            connections.forEach((module, list) -> {
                if (module instanceof Output) {
                    return;
                }
                final String moduleName = module.moduleName();
                final String[] rawConnections = allRawConnections.get(moduleName);
                Arrays.stream(rawConnections)
                    .map(connection -> modules.stream()
                        .filter(m -> m.name.equals(connection))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Module %s does not exist".formatted(connection))))
                    .forEach(list::add);
            });
            configureConjunctions(connections);
            return new Machine(connections);
        }

        private static void configureConjunctions(final Map<Module, List<Module>> connections) {
            connections.keySet().stream()
                .filter(module -> module instanceof Conjunction)
                .map(module -> (Conjunction) module)
                .forEach(conjunctionModule -> connections.entrySet().stream()
                    .filter(e -> e.getValue().contains(conjunctionModule))
                    .forEach(e -> conjunctionModule.addConjunction(e.getKey())));
        }

        private Broadcaster getBroadcaster() {
            return connections.keySet().stream()
                .filter(module -> module instanceof Broadcaster)
                .map(module -> (Broadcaster) module)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find broadcaster"));
        }

        private long pushButtonUntilOutput() {
            // Output is fed a pulse by a conjunction module, so it will receive a low pulse when all watches of the
            // conjunction module send a high pulse. We need to find the cycle of each watch of the conjunction module.
            // The final answer will be the LCM of these cycles.
            final Conjunction conjunctionToOutput = connections.entrySet().stream()
                .filter(e -> e.getValue().stream().anyMatch(module -> module instanceof Output))
                .map(e -> (Conjunction) e.getKey())
                .findFirst()
                .orElseThrow();
            final Map<Module, Long> conjunctionWatchCycles = new HashMap<>();
            final AtomicLong timesButtonPushed = new AtomicLong();
            while(conjunctionWatchCycles.size() < conjunctionToOutput.watches.size()) {
                timesButtonPushed.incrementAndGet();
                pushButton(parentCurrentPulse -> {
                    final Module parentModule = parentCurrentPulse.first();
                    final Module currentModule = parentCurrentPulse.second();
                    final Pulse pulse = parentCurrentPulse.third();
                    if (currentModule.equals(conjunctionToOutput) && pulse == Pulse.HIGH) {
                        if (!conjunctionWatchCycles.containsKey(parentModule)) {
                            conjunctionWatchCycles.put(parentModule, timesButtonPushed.get());
                        }
                    }
                });
            }

            return Maths.lcm(conjunctionWatchCycles.values().stream().toList());
        }

        @SuppressWarnings("SameParameterValue")
        private long pushButton(final int times) {
            final Pair<AtomicLong, AtomicLong> lowAndHighOutputPulses = Pair.of(new AtomicLong(), new AtomicLong());
            for (int i = 0; i < times; i++) {
                pushButton(parentCurrentPulse -> {
                    final Pulse pulse = parentCurrentPulse.third();
                    if (pulse == Pulse.LOW) {
                        lowAndHighOutputPulses.left().incrementAndGet();
                    } else if (pulse == Pulse.HIGH) {
                        lowAndHighOutputPulses.right().incrementAndGet();
                    }
                });
            }
            return lowAndHighOutputPulses.left().get() * lowAndHighOutputPulses.right().get();
        }

        private void pushButton(final Consumer<Triplet<Module, Module, Pulse>> parentCurrentPulseConsumer) {
            final Queue<Triplet<Module, Module, Pulse>> frontier = new ArrayDeque<>();
            frontier.add(Triplet.of(null, getBroadcaster(), Pulse.LOW));
            while (!frontier.isEmpty()) {
                final Triplet<Module, Module, Pulse> current = frontier.poll();
                parentCurrentPulseConsumer.accept(current);
                final Module parentModule = current.first();
                final Module currentModule = current.second();
                final Pulse inputPulse = current.third();
                final Pulse outputPulse = currentModule.handlePulse(parentModule, inputPulse);
                if (outputPulse == null) {
                    continue;
                }
                final List<Module> nextModules = new ArrayList<>(connections.get(currentModule));
                nextModules.forEach(nextModule -> frontier.add(Triplet.of(currentModule, nextModule, outputPulse)));
            }
        }
    }

    private enum Pulse {
        LOW,
        HIGH,
    }

    private abstract static class Module {
        protected final String name;

        private Module(final String name) {
            this.name = name;
        }

        protected abstract String moduleName();

        protected abstract Pulse handlePulse(final Module previous, final Pulse pulse);

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Module module = (Module) o;
            return name.equals(module.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static final class Broadcaster extends Module {
        private Broadcaster() {
            super("broadcaster");
        }

        @Override
        protected String moduleName() {
            return name;
        }

        @Override
        protected Pulse handlePulse(final Module previous, final Pulse pulse) {
            return pulse;
        }
    }

    private static final class Output extends Module {
        private Output(final String name) {
            super(name);
        }

        @Override
        protected String moduleName() {
            return name;
        }

        @Override
        protected Pulse handlePulse(final Module previous, final Pulse pulse) {
            return pulse;
        }
    }

    private static final class FlipFlop extends Module {
        private State state;

        private FlipFlop(final String name) {
            super(name);
            this.state = State.OFF;
        }

        @Override
        protected String moduleName() {
            return "%%%s".formatted(name);
        }

        @Override
        protected Pulse handlePulse(final Module previous, final Pulse pulse) {
            if (pulse == Pulse.HIGH) {
                return null;
            }
            if (state == State.OFF) {
                state = State.ON;
                return Pulse.HIGH;
            } else {
                state = State.OFF;
                return Pulse.LOW;
            }
        }

        private enum State {
            ON,
            OFF
        }
    }

    private static final class Conjunction extends Module {
        private final Set<Module> watches;
        private final Map<Module, Pulse> memory;

        private Conjunction(final String name) {
            super(name);
            watches = new HashSet<>();
            memory = new HashMap<>();
        }

        @Override
        protected String moduleName() {
            return "&%s".formatted(name);
        }

        @Override
        protected Pulse handlePulse(final Module previous, final Pulse pulse) {
            memory.put(previous, pulse);
            return memory.values().stream().allMatch(memPulse -> memPulse == Pulse.HIGH) ?
                Pulse.LOW :
                Pulse.HIGH;
        }

        private void addConjunction(final Module conjunction) {
            this.watches.add(conjunction);
            this.memory.put(conjunction, Pulse.LOW);
        }
    }

}