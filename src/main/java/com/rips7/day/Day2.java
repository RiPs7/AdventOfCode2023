package com.rips7.day;

import java.util.Arrays;
import java.util.List;

public class Day2 implements Day<Integer> {

  @Override
  public Integer part1(String input) {
    return Arrays.stream(input.split("\n"))
      .map(Game::parseGame)
      .filter(game -> !game.isImpossible())
      .map(Game::id)
      .reduce(Integer::sum)
      .orElse(0);
  }

  @Override
  public Integer part2(String input) {
    return Arrays.stream(input.split("\n"))
      .map(Game::parseGame)
      .map(Game::getPossibleMax)
      .map(GameStage::getPower)
      .reduce(Integer::sum)
      .orElse(0);
  }

  private record Game(int id, List<GameStage> gameStages) {
    private static Game parseGame(final String line) {
      final String[] gameIdAndStages = line.split(": ");
      final int id = Integer.parseInt(gameIdAndStages[0].split(" ")[1]);
      final List<GameStage> gameStages = Arrays.stream(gameIdAndStages[1].split("; "))
        .map(GameStage::parseGameStage)
        .toList();
      return new Game(id, gameStages);
    }

    private boolean isImpossible() {
      return gameStages.stream().anyMatch(GameStage::isImpossible);
    }

    private GameStage getPossibleMax() {
      int maxRed = 0, maxGreen = 0, maxBlue = 0;
      for (GameStage stage : gameStages) {
        maxRed = Math.max(maxRed, stage.red);
        maxGreen = Math.max(maxGreen, stage.green);
        maxBlue = Math.max(maxBlue, stage.blue);
      }
      return new GameStage(maxRed, maxBlue, maxGreen);
    }
  }

  private record GameStage(int red, int green, int blue) {
    private static GameStage parseGameStage(final String line) {
      final String[] samples = line.split(", ");
      int red = 0;
      int green = 0;
      int blue = 0;
      for (String sample : samples) {
        final String[] numberAndColor = sample.split(" ");
        int number = Integer.parseInt(numberAndColor[0]);
        switch (numberAndColor[1]) {
          case "red" -> red = number;
          case "green" -> green = number;
          case "blue" -> blue = number;
        }
      }
      return new GameStage(red, green, blue);
    }

    private boolean isImpossible() {
      return red > 12 || green > 13 || blue > 14;
    }

    private int getPower() {
      return red * green * blue;
    }
  }
}
