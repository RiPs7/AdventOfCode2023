package com.rips7.util.maths;

import java.util.List;

@SuppressWarnings("unused")
public class Maths {

  public static long lcm(List<Long> numbers) {
    return numbers.stream().reduce(1L, (accum, curr) -> (accum * curr) / gcd(accum, curr));
  }

  public static long gcd(List<Long> numbers) {
    return numbers.stream().reduce(0L, Maths::gcd);
  }

  public static long gcd(long a, long b) {
    if (b == 0)
      return a;
    return gcd(b, a % b);
  }

}
