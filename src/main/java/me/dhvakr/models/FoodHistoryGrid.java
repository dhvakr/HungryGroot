package me.dhvakr.models;

import java.time.LocalDate;
import java.util.Set;

public record FoodHistoryGrid(String grootName, Set<String> foodPreference, LocalDate date) {
}
