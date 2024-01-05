package com.rips7.util.maths;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import static com.rips7.util.Util.loop2D;
import static com.rips7.util.Util.newGeneric2DArray;

@SuppressWarnings("unused")
public class Maths {

  public record Vector2D<T>(T x, T y) {
    public static <T> Vector2D<T> of(final T x, final T y) {
      return new Vector2D<>(x, y);
    }

    public static <T> Vector2D<T> add(final Vector2D<T> v1, final Vector2D<T> v2, final BiFunction<T, T, T> adder) {
      return Vector2D.of(adder.apply(v1.x, v2.x), adder.apply(v1.y, v2.y));
    }
  }

  public record Vector3D<T>(T x, T y, T z) {
    public static <T> Vector3D<T> of(final T x, final T y, final T z) {
      return new Vector3D<>(x, y, z);
    }

    public static <T> Vector3D<T> add(final Vector3D<T> v1, final Vector3D<T> v2, final BiFunction<T, T, T> adder) {
      return Vector3D.of(adder.apply(v1.x, v2.x), adder.apply(v1.y, v2.y), adder.apply(v1.z, v2.z));
    }
  }

  public static Float min(final Float... numbers) {
    return Arrays.stream(numbers)
        .reduce(Math::min)
        .orElse(0.0f);
  }

  public static Float max(final Float... numbers) {
    return Arrays.stream(numbers)
        .reduce(Math::max)
        .orElse(0.0f);
  }

  public static int ceil(final double d) {
    return (int) Math.ceil(d);
  }

  public static int floor(final double d) {
    return (int) Math.floor(d);
  }

  public static boolean inBetween(final double toCheck, final double start, final double end) {
    return start <= toCheck && toCheck <= end;
  }

  public static boolean inBetweenStrict(final double toCheck, final double start, final double end) {
    return start < toCheck && toCheck < end;
  }

  public static long lcm(final List<Long> numbers) {
    return numbers.stream().reduce(1L, (accum, curr) -> (accum * curr) / gcd(accum, curr));
  }

  public static long gcd(final List<Long> numbers) {
    return numbers.stream().reduce(0L, Maths::gcd);
  }

  public static long gcd(final long a, final long b) {
    if (b == 0)
      return a;
    return gcd(b, a % b);
  }

  public static <T> T[][] transpose(final Class<T> clazz, final T[][] arr) {
    final T[][] result = newGeneric2DArray(clazz, arr[0].length, arr.length);
    loop2D(result.length, result[0].length, (r, c) -> result[r][c] = arr[c][r]);
    return result;
  }

}
