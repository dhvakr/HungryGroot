package me.dhvakr.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Embeddable
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FoodCountHistory {

    //~ Constructor ========================================================================================================================

    public FoodCountHistory() {}

    //~ Parameterized Constructor ==========================================================================================================

    public FoodCountHistory(LocalDate foodCountDate, boolean isCountable) {
        this.foodCountDate = foodCountDate;
        this.isCountable = isCountable;
    }

    //~ ====================================================================================================================================

    public FoodCountHistory(LocalDate foodCountDate, Set<String> foodPreference, boolean isCountable) {
        this.foodCountDate = foodCountDate;
        this.foodPreference = foodPreference;
        this.isCountable = isCountable;
    }

    //~ initializers =======================================================================================================================

    @Column(name = "count_recorded_date")
    LocalDate foodCountDate;

    @Column(name = "meal_preference")
    Set<String> foodPreference;

    @Column(name = "countable")
    boolean isCountable;
}
