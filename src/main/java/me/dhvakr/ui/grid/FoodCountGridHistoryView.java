package me.dhvakr.ui.grid;

import me.dhvakr.jpa.entity.Groots;
import me.dhvakr.jpa.service.GrootService;
import me.dhvakr.models.FoodHistoryGrid;
import me.dhvakr.ui.ApplicationLayout;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;
import me.dhvakr.constants.Dashboard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;

@PermitAll
@PageTitle("Groot-Food-Data | Hungry Groot")
@Route(value = "food-data", layout = ApplicationLayout.class)
public class FoodCountGridHistoryView extends HorizontalLayout {

    //~ Static fields/initializers =========================================================================================================

    private List<FoodHistoryGrid> totalFoodHistoryList; // Get Complete list
    private List<FoodHistoryGrid> foodHistorySortedByToday; // By current date
    private final GrootService grootService;
    private TextField filter;
    private Button countView = new Button("Count");
    private final CheckboxGroup<String> foodPreferenceCheckbox = new CheckboxGroup<>("Meals");

    //~ Instance fields ====================================================================================================================

    private final Grid<FoodHistoryGrid> foodHistoryGrid = new Grid<>(FoodHistoryGrid.class, false);
    private final DatePicker datePicker = new DatePicker();

    //~ Constructor ========================================================================================================================

    FoodCountGridHistoryView(GrootService grootService) {
        this.grootService = grootService;
        final HorizontalLayout topLayout = createTopSearchBar();
        addClassName("food-count-data-view");
        setSizeFull();

        foodHistoryGrid.addColumn(FoodHistoryGrid::grootName).setHeader("Groot Names")
                .setTextAlign(ColumnTextAlign.START)
                .setAutoWidth(true);

        foodHistoryGrid.addColumn(FoodHistoryGrid::foodPreference)
                .setHeader("Meal Preferred")
                .setTextAlign(ColumnTextAlign.CENTER)
                .setAutoWidth(true)
                .isSortable();

        final VerticalLayout barAndGridLayout = new VerticalLayout();
        // Complete Layout elements
        barAndGridLayout.add(topLayout);

        // Checkbox elements
        foodPreferenceCheckbox.getElement().getStyle().set("margin-top", "-18px");
        foodPreferenceCheckbox.setRenderer(new TextRenderer<>(String::toString));
        foodPreferenceCheckbox.setErrorMessage("At least one required to get count");
        foodPreferenceCheckbox.setItems(Dashboard.FOOD_PREFERENCE);
        foodPreferenceCheckbox.setValue(Collections.singleton(Dashboard.DINNER_FOOD_PREFERENCE_KEY));
        foodPreferenceCheckbox.setRequired(true);
        barAndGridLayout.add(foodPreferenceCheckbox);

        // Grid elements
        barAndGridLayout.add(foodHistoryGrid);
        barAndGridLayout.setFlexGrow(1, foodHistoryGrid);
        barAndGridLayout.setFlexGrow(0, topLayout);
        barAndGridLayout.setSizeFull();
        barAndGridLayout.expand(foodHistoryGrid);

        add(barAndGridLayout);

        Page<Groots> grootsPage = grootService.list(Pageable.ofSize(30));
        totalFoodHistoryList = grootService.getTotalFoodHistoryListForGrid(grootsPage);

        // by default the list shown in grid is sorted by current date with type both (Lunch | Dinner)
        foodHistorySortedByToday = totalFoodHistoryList.stream()
                .filter(foodHistory -> foodHistory.date().equals(LocalDate.now())).toList();

        foodHistoryGrid.setPageSize(20);
        foodHistoryGrid.setItems(foodHistorySortedByToday);
        foodHistoryGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
    }

    //~ Methods ============================================================================================================================

    /**
     * Root Top level method to hold all components to render in UI
     *
     * @return {@link HorizontalLayout}
     */
    private HorizontalLayout createTopSearchBar() {
        filter = new TextField();
        filter.setPlaceholder("Search name to ensure your food count");
        // Filter to search by groot name and adding the results to the grid
        filter.addValueChangeListener(
                    search -> foodHistoryGrid.setItems(findGrootBySearchValue(datePicker.getValue(), search.getValue())));
        filter.addFocusShortcut(Key.KEY_F, KeyModifier.CONTROL); // -> ctrl + f

        final HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.setWidth("100%");
        topLayout.add(filter);

        // Filter by date and adding the results to the grid
        datePicker.addValueChangeListener(date -> {
            countView.setText("Count"); // Just resetting text to old state
            countView.setEnabled(false);
            foodPreferenceCheckbox.clear();
            foodHistoryGrid.setItems(grootService.filterByDate(datePicker.getValue(), 30));
        });
        datePicker.setPlaceholder("Filter by date");
        datePicker.addFocusShortcut(Key.KEY_D, KeyModifier.CONTROL); // -> ctrl + d
        datePicker.setValue(LocalDate.now());
        datePicker.setWeekNumbersVisible(true);

        // Check for foodPreferenceCheckbox is empty and displace the count ability
        if (foodPreferenceCheckbox.isEmpty()) {
            countView.setEnabled(false);
        }

        // Filter by food preference and adding the results to the grid
        foodPreferenceCheckbox.addSelectionListener(multiSelectionEvent -> {
            if (!multiSelectionEvent.getValue().isEmpty()) {
                countView.setEnabled(true);
                updateCountByFoodPreference();
            }
            else if (multiSelectionEvent.getValue().isEmpty()) {
                countView.setText("Count"); // Just resetting text to old state
                countView.setEnabled(false);
            }
            foodHistoryGrid.setItems(grootService.filterByLunchOrDinner(datePicker.getValue(), multiSelectionEvent.getValue(), 40));
        });

        countView.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        countView.setIcon(VaadinIcon.USER_CLOCK.create());

        topLayout.add(datePicker);
        topLayout.add(countView);
        topLayout.setVerticalComponentAlignment(Alignment.START, filter);
        topLayout.expand(filter);
        return topLayout;
    }

    //~ ====================================================================================================================================

    /**
     * Update the total count by date based on foodPreferenceCheckbox state
     */
    private void updateCountByFoodPreference() {
        long count = (foodPreferenceCheckbox.getSelectedItems().containsAll(Arrays.asList(Dashboard.FOOD_PREFERENCE)))
                 ? foodHistorySortedByToday.stream()
                    .filter(d -> d.date().equals(datePicker.getValue()))
                    .filter(fp -> fp.foodPreference().isEmpty())
                    .count()
                : getTotalFoodCountByType(datePicker.getValue(), foodPreferenceCheckbox.getSelectedItems());
        countView.setText("Count: " + count);
    }

    //~ ====================================================================================================================================

    /**
     * gets a current date data by default if search key is empty
     *
     * @param searchValue grootName
     * @return list of values
     */
    private List<FoodHistoryGrid> findGrootBySearchValue(LocalDate dateValue, String searchValue) {
        return (searchValue.isEmpty()) ? foodHistorySortedByToday : grootService.filterByGrootName(dateValue, searchValue);
    }

    //~ ====================================================================================================================================

    private long getTotalFoodCountByType(LocalDate dateValue, Set<String> selectedMealPreferences) {
        return (totalFoodHistoryList != null)
                ? totalFoodHistoryList.stream()
                    .filter(foodHistory -> foodHistory.date().equals(dateValue))
                    .filter(foodHistory -> foodHistory.foodPreference().containsAll(selectedMealPreferences))
                    .count()
                : 0;
    }

    //~ ====================================================================================================================================
}
