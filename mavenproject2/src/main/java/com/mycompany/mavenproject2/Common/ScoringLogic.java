/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject2.Common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author ggunes
 */
public class ScoringLogic {
    // Upper section categories 
    private static final String[] UPPER_SECTION_CATEGORIES = {
        "Ones", "Twos", "Threes", "Fours", "Fives", "Sixes"
    };

      // Bonus rules for upper section
    private static final int UPPER_SECTION_BONUS_LIMIT = 63;
    private static final int UPPER_SECTION_BONUS = 35;
    
    // Main method to calculate score based on selected category
    public static int calculateScore(String category, Dice[] dices) {
        int[] values = new int[5];
        for (int i = 0; i < 5; i++) {
            values[i] = dices[i].getNumber();
        }
        Arrays.sort(values);

        switch (category) {
            case "Ones": return countSpecificValue(values, 1);
            case "Twos": return countSpecificValue(values, 2);
            case "Threes": return countSpecificValue(values, 3);
            case "Fours": return countSpecificValue(values, 4);
            case "Fives": return countSpecificValue(values, 5);
            case "Sixes": return countSpecificValue(values, 6);
            case "Three of a Kind": return checkNOfAKind(values, 3) ? Arrays.stream(values).sum() : 0;
            case "Four of a Kind": return checkNOfAKind(values, 4) ? Arrays.stream(values).sum() : 0;
            case "Full House": return isFullHouse(values) ? 25 : 0;
            case "Small Straight": return isStraight(values, 4) ? 30 : 0;
            case "Large Straight": return isStraight(values, 5) ? 40 : 0;
            case "Yahtzee": return checkNOfAKind(values, 5) ? 50 : 0;
            case "Chance": return Arrays.stream(values).sum();
            default: return 0;
        }
    }

    // Calculate total score of upper section
    public static int calculateUpperSectionTotal(Map<String, Integer> scoreMap) {
        return sumCategories(scoreMap, UPPER_SECTION_CATEGORIES);
    }

    // Check if bonus is earned in upper section
    public static int calculateUpperSectionBonus(Map<String, Integer> scoreMap) {
        return calculateUpperSectionTotal(scoreMap) >= UPPER_SECTION_BONUS_LIMIT
                ? UPPER_SECTION_BONUS
                : 0;
    }

    // Calculate final score including bonus
    public static int calculateFinalScore(Map<String, Integer> scoreMap) {
        int total = calculateUpperSectionBonus(scoreMap);
        for (int score : scoreMap.values()) {
            if (score != -1) {
                total += score;
            }
        }
        return total;
    }

    // Sum selected categories from score map
    private static int sumCategories(Map<String, Integer> scoreMap, String[] categories) {
        int total = 0;
        for (String category : categories) {
            int score = scoreMap.getOrDefault(category, -1);
            if (score != -1) {
                total += score;
            }
        }
        return total;
    }

    // Count and sum specific dice values
    private static int countSpecificValue(int[] values, int target) {
        int sum = 0;
        for (int v : values) if (v == target) sum += v;
        return sum;
    }

    // Check if there are at least N same dice values
  private static boolean checkNOfAKind(int[] values, int n) {

    Map<Integer, Integer> counts = new HashMap<>();

    for (int value : values) {
        counts.put(value, counts.getOrDefault(value, 0) + 1);

        if (counts.get(value) >= n) {
            return true;
        }
    }

    return false;
}

  // Check if dice form a full house
    private static boolean isFullHouse(int[] v) {
        return (v[0] == v[1] && v[2] == v[4] && v[1] != v[2]) || 
               (v[0] == v[2] && v[3] == v[4] && v[2] != v[3]);
    }

    // Check if there is a straight of given length
    private static boolean isStraight(int[] v, int length) {
        int continuous = 1;
        int maxContinuous = 1;
        for (int i = 0; i < v.length - 1; i++) {
            if (v[i + 1] == v[i] + 1) {
                continuous++;
                maxContinuous = Math.max(maxContinuous, continuous);
            } else if (v[i + 1] != v[i]) {
                continuous = 1;
            }
        }
        return maxContinuous >= length;
    }
    
}
