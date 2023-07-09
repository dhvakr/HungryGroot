package me.dhvakr.config;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecurringComboBox extends MultiSelectComboBox<String> {

    //~ private fields/initializers ========================================================================================================

    boolean showOnWeekends = false;

    boolean isWeekday;

    boolean isBetweenLunchTime;

    boolean isBetweenDinnerTime;

    //~ Constructor ========================================================================================================================

    public RecurringComboBox() {
        updateVisibility();
    }

    //~ Parameterized Constructor ==========================================================================================================

    public RecurringComboBox(boolean showOnWeekends) {
        this.showOnWeekends = showOnWeekends;
        updateVisibility();
    }

    //~ Methods ============================================================================================================================

    /**
     * <p> This method decides to show the checkbox by configured time,
     * By Default Its 9AM to 5Pm for Lunch, and here for instance and set the startTime to 9:00 AM, endTime to 5:00 PM,
     * and showOnWeekends to true. </P>
     *
     *  This means that the checkbox will be visible from 9:00 AM to 5:00 PM on weekdays,
     *  and all day on weekends.
     *  NOTE : showOnWeekends will be implemented in future builds
     */
    private void updateVisibility() {
        LocalDateTime localDateTime = java.time.LocalDateTime.now();
        DayOfWeek dayOfWeek = localDateTime.getDayOfWeek();
        LocalTime currentTime = localDateTime.toLocalTime();

        setWeekday(dayOfWeek.equals(DayOfWeek.SATURDAY) || dayOfWeek.equals(DayOfWeek.SUNDAY));
        setBetweenLunchTime(currentTime.isAfter(LocalTime.of(9, 0)) && currentTime.isBefore(LocalTime.of(17, 0))); // 9am to 17pm (i,e) 9am to 5pm
        setBetweenDinnerTime(currentTime.isAfter(LocalTime.of(17, 0)) && currentTime.isBefore(LocalTime.of(19, 0))); // 17pm to 19pm (i,e) 5pm to 7pm

//        if (!isWeekday()) {
//            if (isBetweenLunchTime())
//                setItems("Lunch");
//            else if (isBetweenDinnerTime())
//                setItems("Dinner");
//        }
//        setVisible(!isWeekday() && (isBetweenLunchTime() || isBetweenDinnerTime()));
        if (!isWeekday() && isBetweenDinnerTime())
            setItems("Dinner");
        setVisible(!isWeekday() && isBetweenDinnerTime());
    }
}
