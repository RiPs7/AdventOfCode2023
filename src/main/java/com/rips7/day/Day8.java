package com.rips7.day;

import com.rips7.util.maths.Combinatorics.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.rips7.util.maths.Maths.lcm;

public class Day8 implements Day<Long> {

  @Override
  public Long part1(String input) {
    final String instructions = input.split("\n")[0].trim();
    final Map<Node, Pair<Node, Node>> nodes = parseNodes(input.split("\n\n")[1].trim());

    final Node target = new Node("ZZZ");
    Node current = new Node("AAA");
    int ip = 0; // instruction pointer
    long steps = 0;
    while(!current.equals(target)) {
      final char instruction = instructions.charAt(ip % instructions.length());
      current = move(nodes, current, instruction);
      steps++;
      ip++;
    }

    return steps;
  }

  @Override
  public Long part2(String input) {
    final String instructions = input.split("\n")[0].trim();
    final Map<Node, Pair<Node, Node>> nodes = parseNodes(input.split("\n\n")[1].trim());

    final List<Node> starts = nodes.keySet().stream()
      .filter(node -> node.name.endsWith("A"))
      .collect(Collectors.toCollection(ArrayList::new));

    final List<Long> stepsToEnd = starts.stream()
      .map(start -> {
        Node current = start;
        int ip = 0; // instruction pointer
        long steps = 0;
        while(!current.name.endsWith("Z")) {
          final char instruction = instructions.charAt(ip % instructions.length());
          current = move(nodes, current, instruction);
          steps++;
          ip++;
        }
        return steps;
      })
      .toList();

    return lcm(stepsToEnd);
  }

  private static Map<Node, Pair<Node, Node>> parseNodes(final String input) {
    final Pattern nodeListPattern = Pattern.compile("\\((.+), (.+)\\)");
    return Arrays.stream(input.split("\n"))
      .map(line -> line.split(" = "))
      .collect(Collectors.toMap(
        parts -> Node.parse(parts[0]),
        parts -> {
          final Matcher matcher = nodeListPattern.matcher(parts[1]);
          if (matcher.matches()) {
            return Pair.of(Node.parse(matcher.group(1)), Node.parse(matcher.group(2)));
          }
          throw new RuntimeException("Cannot parse %s".formatted(input));
        }));
  }

  private static Node move(final Map<Node, Pair<Node, Node>> nodes, final Node current, final char instruction) {
    final Pair<Node, Node> nextSteps = nodes.get(current);
    return switch (instruction) {
      case 'L' -> nextSteps.left();
      case 'R' -> nextSteps.right();
      default -> throw new RuntimeException("Unrecognized instruction: %s".formatted(instruction));
    };
  }

  private record Node(String name) {
    private static Node parse(final String input) {
      return new Node(input);
    }
  }

}
