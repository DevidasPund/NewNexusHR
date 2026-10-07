package com.nexushr.performance.dto;

/** A single point in a performance trend series. */
public record ScorePoint(String period, int score) {
}
