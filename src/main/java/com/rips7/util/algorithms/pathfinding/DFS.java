package com.rips7.util.algorithms.pathfinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

@SuppressWarnings("unused")
public class DFS<T> {

  public List<T> run(final T start, final T end, final Function<T, List<T>> neighborsGetter) {
    return run(start, end, neighborsGetter, false);
  }

  public List<T> run(final T start, final T end, final Function<T, List<T>> neighborsGetter, final boolean isStartSameAsEnd) {
    final Stack<DFSNode<T>> frontier = new Stack<>();
    final Set<DFSNode<T>> closed = new HashSet<>();

    final DFSNode<T> startNode = node(start);
    final DFSNode<T> endNode = node(end);

    if (isStartSameAsEnd) {
      neighborsGetter.apply(start).stream().map(DFS::node).peek(n -> n.parent = startNode).forEach(frontier::push);
    } else {
      frontier.add(startNode);
    }
    while (!frontier.isEmpty()) {
      final DFSNode<T> current = frontier.pop();
      if (current.equals(endNode)) {
        return backtrack(current);
      }
      if (closed.contains(current)) {
        continue;
      }
      neighborsGetter.apply(current.data()).stream()
          .map(DFS::node)
          .filter(n -> !Objects.equals(n, current.parent))
          .peek(n -> n.parent = current)
          .forEach(frontier::add);
      closed.add(current);
    }
    throw new RuntimeException("No solution found");
  }

  public static <T> DFSNode<T> node(final T data) {
    return new DFSNode<>(data);
  }

  private List<T> backtrack(final DFSNode<T> end) {
    List<DFSNode<T>> path = new ArrayList<>();
    DFSNode<T> current = end;
    while (current != null) {
      path.add(current);
      current = current.parent;
    }
    Collections.reverse(path);
    return path.stream().map(DFSNode::data).toList();
  }

  public static final class DFSNode<T> {
    private final T data;
    private DFSNode<T> parent;

    private DFSNode(final T data) {
      this.data = data;
    }

    public T data() {
      return data;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(data);
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof DFS.DFSNode<?> other)) return false;
      return Objects.equals(this.data, other.data);
    }

    @Override
    public String toString() {
      return "[%s]".formatted(data);
    }
  }

}
