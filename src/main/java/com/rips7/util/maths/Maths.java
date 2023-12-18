package com.rips7.util.maths;

import java.util.List;

import static com.rips7.util.Util.loop2D;
import static com.rips7.util.Util.newGeneric2DArray;

@SuppressWarnings("unused")
public class Maths {

  public record Vector2D<T>(T x, T y) {
    public static <T> Vector2D<T> of(final T x, final T y) {
      return new Vector2D<>(x, y);
    }
  }

  public record Vector3D<T>(T x, T y, T z) {}

  public static int ceil(final double d) {
    return (int) Math.ceil(d);
  }

  public static int floor(final double d) {
    return (int) Math.floor(d);
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
