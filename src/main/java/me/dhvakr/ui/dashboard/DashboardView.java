package me.dhvakr.ui.dashboard;

import me.dhvakr.jpa.FoodCountHistory;
import me.dhvakr.jpa.entity.Groots;
import me.dhvakr.jpa.service.GrootService;
import me.dhvakr.security.AuthenticatedUser;
import me.dhvakr.ui.ApplicationLayout;
import me.dhvakr.config.RecurringComboBox;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import me.dhvakr.constants.Constants;
import me.dhvakr.constants.Dashboard;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@PermitAll
@PageTitle("Dashboard | Hungry Groot")
@Route(value = "dashboard", layout = ApplicationLayout.class)
@RouteAlias(value = "", layout = ApplicationLayout.class)
public class DashboardView extends VerticalLayout {

    //~ Static fields/initializers =========================================================================================================

    private static final String LOG_TAG = Constants.DEFAULT_LOG_TAG + "DASHBOARD: ";

    //~ Instance fields ====================================================================================================================

    private final H4 defaultText = new H4(Dashboard.DEFAULT_TEXT);
    private final H4 onLiveText = new H4(Dashboard.LIVE_TEXT);
    private Button markButton = new Button("Mark", VaadinIcon.BOOKMARK.create());
    private final RecurringComboBox dropDown = new RecurringComboBox();
    private final GrootService grootService;

    //~ Constructor ========================================================================================================================

    public DashboardView(GrootService grootService, AuthenticatedUser authenticatedUser) {
        this.grootService = grootService;
        Optional<Groots> loggedInUser = authenticatedUser.get();
        Groots hungryGroot = loggedInUser.orElseGet(loggedInUser::orElseThrow);

        addClassName("dashboard-view");

        Image img = new Image("img/food-scheduled.png", "food-placeholder image");
        img.setWidth("200px");
        add(img);

        onLiveText.addClassNames(LumoUtility.Background.PRIMARY_10, LumoUtility.TextAlignment.CENTER);
        onLiveText.addClassNames(LumoUtility.Padding.LARGE, LumoUtility.BorderRadius.LARGE,
                Margin.Top.SMALL, Margin.Bottom.NONE);

        if (dropDown.isVisible()) {
            add(onLiveText);
        }

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);

        HorizontalLayout layout = new HorizontalLayout();
        // To show the groot food preferred state in the UI for better UX
        boolean isSameState = hungryGroot.getFoodCountHistory().stream()
                .anyMatch(date -> date.getFoodCountDate().equals(LocalDate.now()));
        if (!isSameState)
            layout.add(dropDown, createButtonLayout());
        else {
            Set<String> selectedFoodState = hungryGroot.getFoodCountHistory().stream()
                    .filter(e -> e.getFoodPreference() != null)
                    .flatMap(e -> e.getFoodPreference().stream())
                    .collect(Collectors.toSet());
            if (dropDown.isVisible())
                dropDown.setValue(selectedFoodState);
            layout.add(dropDown, createButtonLayout());
        }

        // listen for changes in the dropdown and update a preference done by a groot
        markButton.addClickListener(buttonClickEvent -> updateGrootFoodPreference(hungryGroot, dropDown.getValue()));
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        add(layout);
    }

    //~ Methods ============================================================================================================================

    /**
     * Creates a button layout by recurring dropdown and the default text
     * only shows if dropdown configured time is crossed
     *
     * @return {@link Component}
     */
    private Component createButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        markButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        if (dropDown.isVisible()) {
            buttonLayout.add(markButton);
        } else {
            if (dropDown.isWeekday()) {
                onLiveText.setText(Dashboard.WEEKEND_TEXT);
                add(onLiveText);
            } else if (dropDown.isBetweenLunchTime()) {
                onLiveText.setText(Dashboard.NOT_BETWEEN_DINNER_TIME);
                add(onLiveText);
            } else {
                defaultText.addClassNames(LumoUtility.Background.PRIMARY_10, LumoUtility.TextAlignment.CENTER);
                defaultText.addClassNames(LumoUtility.Padding.LARGE, LumoUtility.BorderRadius.LARGE,
                        Margin.Top.SMALL, Margin.Bottom.NONE);
                add(defaultText);
            }
        }
        return buttonLayout;
    }

    //~ ====================================================================================================================================

    private void updateGrootFoodPreference(Groots hungryGroot, Set<String> foodState) {
        Groots groot = grootService.findByEmail(hungryGroot.getUsername());
        // Create new entity object if not already found by entry
        FoodCountHistory foodCountHistory = groot.getFoodCountHistory().stream()
                .filter(history -> history.getFoodCountDate().isEqual(LocalDate.now()))
                .findFirst()
                .orElse(new FoodCountHistory(LocalDate.now(),false));

        // doing this to avoid an unnecessary db call
        var formattedMealPreference = Collections.emptySet();
        if (foodCountHistory.getFoodPreference() != null) {
            formattedMealPreference = foodCountHistory.getFoodPreference().stream().filter(Objects::nonNull)
                    .map(preference -> preference.replaceAll("[\\[\\]]", ""))
                    .collect(Collectors.toSet());
        }
        boolean sameState = (foodCountHistory.getFoodCountDate().equals(LocalDate.now())
                && formattedMealPreference.equals(foodState));

        if (sameState)
            Notification.show("Nothing to change " + "U+1F612").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        else
            updateAndNotifyGroot(groot, foodCountHistory, foodState);
    }

    //~ ====================================================================================================================================

    private void updateAndNotifyGroot(Groots groot , FoodCountHistory foodCountHistory, Set<String> mealState) {
        String notifyMessage;
        NotificationVariant notificationVariant;

        if (!mealState.isEmpty()) {
            foodCountHistory.setCountable(true);
            foodCountHistory.setFoodPreference(mealState);
            groot.getFoodCountHistory().add(foodCountHistory);
            notifyMessage = Constants.NOTIFY_SUCCESS_RECORD_COUNT_MESSAGE;
            notificationVariant = NotificationVariant.LUMO_CONTRAST;
            log.info(LOG_TAG + "Successfully Updated Entry for " + groot.getName());
        } else {
            groot.getFoodCountHistory().remove(foodCountHistory);
            notifyMessage = Constants.NOTIFY_FAILURE_RECORD_COUNT_MESSAGE;
            notificationVariant = NotificationVariant.LUMO_ERROR;
            log.info(LOG_TAG + "Successfully Removed Entry for " + groot.getName());
        }
        grootService.saveAndFlush(groot);
        Notification.show(notifyMessage + ((groot.getDisplayName() != null) ? groot.getDisplayName() : groot.getName()))
                .addThemeVariants(notificationVariant);
    }
}
