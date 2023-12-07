package com.rips7.day;

import com.rips7.util.maths.Combinatorics;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Day5 implements Day<Long> {

  @Override
  public Long part1(String input) {
    return Almanac.parse(input).processSeeds().stream()
      .min(Long::compareTo)
      .orElse(0L);
  }

  @Override
  public Long part2(String input) {
    return SmartAlmanac.parse(input).processSeedRanges().stream()
      .filter(range -> range.start != 0)
      .map(AlmanacRange::start)
      .min(Long::compareTo)
      .orElse(0L);
  }

  private record Almanac(List<Long> seeds, List<AlmanacMap> maps) {
    private static Almanac parse(final String input) {
      final String[] sections = input.split("\n\n");
      final List<Long> seeds = Arrays.stream(sections[0].replace("seeds: ", "").split(" "))
        .map(Long::parseLong)
        .toList();
      final List<AlmanacMap> maps = Arrays.stream(sections, 1, sections.length)
        .map(AlmanacMap::parse)
        .toList();
      return new Almanac(seeds, maps);
    }

    private List<Long> processSeeds() {
      return seeds.stream().map(this::processSeed).toList();
    }

    private long processSeed(final long seed) {
      final AtomicLong seedRef = new AtomicLong(seed);
      maps.forEach(map -> map.findMatchingEntry(seedRef.get())
        .ifPresentOrElse(entry -> seedRef.set(entry.processSeed(seedRef.get())), () -> seedRef.set(seedRef.get())));
      return seedRef.get();
    }
  }

  private record SmartAlmanac(List<AlmanacRange> seeds, List<AlmanacMap> maps) {
    private static SmartAlmanac parse(final String input) {
      final String[] sections = input.split("\n\n");
      final Long[] seedRanges = Arrays.stream(sections[0].replace("seeds: ", "").split(" "))
        .map(Long::parseLong)
        .toArray(Long[]::new);
      final List<AlmanacRange> seeds = Combinatorics.consecutivePairs(seedRanges).stream()
        .map(pair -> new AlmanacRange(pair.left(), pair.left() + pair.right() - 1))
        .toList();
      final List<AlmanacMap> maps = Arrays.stream(sections, 1, sections.length)
        .map(AlmanacMap::parse)
        .toList();
      return new SmartAlmanac(seeds, maps);
    }

    private Set<AlmanacRange> processSeedRanges() {
      final AtomicReference<Set<AlmanacRange>> seedRangesRef = new AtomicReference<>(new HashSet<>(seeds));
      maps.forEach(map -> {
        final Set<AlmanacRange> mapRes = map.processSeedRanges(seedRangesRef.get());
        seedRangesRef.set(mapRes);
      });
      return seedRangesRef.get();
    }
  }

  private record AlmanacMap(String name, List<AlmanacEntry> entries) {
    private static AlmanacMap parse(final String input) {
      final String[] lines = input.split("\n");
      final String name = lines[0];
      final List<AlmanacEntry> entries = Arrays.stream(lines, 1, lines.length)
        .map(AlmanacEntry::parse)
        .toList();
      return new AlmanacMap(name, entries);
    }

    private Optional<AlmanacEntry> findMatchingEntry(final long seed) {
      return entries.stream()
        .filter(entry -> entry.canProcessSeed(seed))
        .findFirst();
    }

    private Set<AlmanacRange> processSeedRanges(final Set<AlmanacRange> ranges) {
      final Set<AlmanacRange> resultRanges = new HashSet<>();
      for (final AlmanacRange range : ranges) {
        final Set<AlmanacRange> rangesToRecheck = new HashSet<>();
        final boolean processed = entries.stream()
          .anyMatch(entry -> entry.processSeedRanges(range, resultRanges, rangesToRecheck));
        if (!processed) {
          resultRanges.add(range);
        }
        // Some ranges might need to be rechecked if they were split after an entry tried to process them
        if (!rangesToRecheck.isEmpty()) {
          resultRanges.addAll(processSeedRanges(rangesToRecheck));
        }
      }
      return resultRanges.stream().filter(range -> range.start != 0).collect(Collectors.toSet());
    }
  }

  private record AlmanacEntry(AlmanacRange sourceRange, AlmanacRange destRange) implements Comparable<AlmanacEntry> {
    private static AlmanacEntry parse(final String input) {
      final String[] parts = input.split(" ");
      final long sourceStart = Long.parseLong(parts[1]);
      final long destStart = Long.parseLong(parts[0]);
      final long range = Long.parseLong(parts[2]);
      return new AlmanacEntry(
        new AlmanacRange(sourceStart, sourceStart + range),
        new AlmanacRange(destStart, destStart + range));
    }

    @Override
    public int compareTo(AlmanacEntry other) {
      return Long.compare(this.sourceRange.start, other.sourceRange.start);
    }

    private boolean canProcessSeed(final long seed) {
      return sourceRange.contains(seed);
    }

    private long processSeed(final long seed) {
      return seed + destRange.start - sourceRange.start;
    }

    private boolean processSeedRanges(final AlmanacRange range, final Set<AlmanacRange> resultRanges,
                                      final Set<AlmanacRange> rangesToRecheck) {
      // Range not contained by the entry
      if (range.end < sourceRange.start || range.start > sourceRange.end) {
        return false;
      }
      // Range fully contained by the entry
      if (range.start >= sourceRange.start && range.end <= sourceRange.end) {
        resultRanges.add(new AlmanacRange(processSeed(range.start), processSeed(range.end)));
      }
      // Range overlaps with entry; calculate boundaries and non-overlapping regions
      final AlmanacRange leftNonOverlapping = new AlmanacRange(range.start, sourceRange.start - 1);
      final AlmanacRange rightNonOverlapping = new AlmanacRange(sourceRange.end + 1, range.end);
      final long largestStart = Math.max(range.start, sourceRange.start);
      final long smalledEnd = Math.min(range.end, sourceRange.end);

      if (range.start <= sourceRange.start && range.end <= sourceRange.end) { // Left side overlap
        rangesToRecheck.add(leftNonOverlapping);
        resultRanges.add(new AlmanacRange(
          processSeed(largestStart),
          processSeed(range.end)));
      } else if (range.start >= sourceRange.start && range.end >= sourceRange.end) { // Right side overlap
        resultRanges.add(new AlmanacRange(
          processSeed(range.start),
          processSeed(smalledEnd)));
        rangesToRecheck.add(rightNonOverlapping);
      } else if (range.start <= sourceRange.start && range.end >= sourceRange.end) { // Left & Right side overlap
        rangesToRecheck.add(leftNonOverlapping);
        resultRanges.add(new AlmanacRange(
          processSeed(largestStart),
          processSeed(smalledEnd)));
        rangesToRecheck.add(rightNonOverlapping);
      }
      return true;
    }
  }

  private record AlmanacRange(long start, long end) implements Comparable<AlmanacRange> {
    @Override
    public int compareTo(AlmanacRange other) {
      return Long.compare(this.start, other.start);
    }

    private boolean contains(final long x) {
      return start <= x && x <= end;
    }
  }
}
