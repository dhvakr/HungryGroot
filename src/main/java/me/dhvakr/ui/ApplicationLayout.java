package me.dhvakr.ui;

import me.dhvakr.components.appnav.AppNav;
import me.dhvakr.components.appnav.AppNavItem;
import me.dhvakr.security.AuthenticatedUser;
import me.dhvakr.util.GeneralHelper;
import me.dhvakr.ui.dashboard.DashboardView;
import me.dhvakr.ui.grid.FoodCountGridHistoryView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.Locale;

import lombok.extern.slf4j.Slf4j;
import me.dhvakr.MainApplication;
import me.dhvakr.constants.Constants;
import org.vaadin.lineawesome.LineAwesomeIcon;

/**
 * The main view is a top-level placeholder for other views.
 */
@Slf4j
public class ApplicationLayout extends AppLayout {

    //~ Static fields/initializers =========================================================================================================

    private AuthenticatedUser authenticatedUser;
    private AccessAnnotationChecker accessChecker;
    private H2 viewTitle;

    //~ Instance fields ====================================================================================================================

    private GeneralHelper helper = GeneralHelper.getInstance();

    //~ Constructor ========================================================================================================================

    public ApplicationLayout(AuthenticatedUser authenticatedUser, AccessAnnotationChecker accessChecker) {
        this.authenticatedUser = authenticatedUser;
        this.accessChecker = accessChecker;

        setPrimarySection(Section.NAVBAR);
        addHeaderContent();
        addDrawerContent();
    }

    //~ Methods ============================================================================================================================

    /**
     * Header Values,
     */
    private void addHeaderContent() {
        final DrawerToggle toggle = new DrawerToggle();
        toggle.setIcon(new Icon(VaadinIcon.QRCODE));

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.MEDIUM, LumoUtility.Margin.AUTO);

        addToNavbar(true, toggle, viewTitle, addHeaderViewContent());
    }

    //~ ====================================================================================================================================

    private Component addHeaderViewContent() {
        var header = new Header();
        header.addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN, LumoUtility.Width.FULL);

        var layout = new Div();
        layout.addClassNames(LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER, LumoUtility.Padding.Horizontal.LARGE);

        var appName = new H1();
        appName.addClassNames(LumoUtility.Margin.Vertical.NONE, LumoUtility.Margin.End.AUTO, LumoUtility.FontSize.MEDIUM);
        layout.add(appName);

        var user = authenticatedUser.get();
        if (user.isPresent()) {
            var groot = user.get();
            Avatar avatar = new Avatar(groot.getName().toUpperCase(Locale.ENGLISH));
            avatar.setImage(helper.getRandomValueFromList(MainApplication.defaultUserAvatarPaths));
            avatar.setThemeName("xsmall");
            avatar.getElement().setAttribute("tabindex", "-1");

            MenuBar userMenu = new MenuBar();
            userMenu.setThemeName("tertiary-inline contrast");

            MenuItem userSection = userMenu.addItem("");
            Div div = new Div();
            div.add(avatar);
            div.add(avatar.getName());
            div.add(new Icon("lumo", "dropdown"));
            div.getElement().getStyle().set("display", "flex");
            div.getElement().getStyle().set("align-items", "center");
            div.getElement().getStyle().set("gap", "var(--lumo-space-s)");

            userSection.add(div);
            userSection.getSubMenu().addItem("Sign out", e -> authenticatedUser.logout());

            layout.add(userMenu);
        } else {
            var loginLink = new Anchor("login", "Sign in");
            layout.add(loginLink);
        }

        header.add(layout);
        return header;
    }

    //~ ====================================================================================================================================

    private void addDrawerContent() {
        var appName = new H1(Constants.COMPANY_NAME);
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        Header header = new Header(appName);
        Scroller scroller = new Scroller(createNavigation());
        addToDrawer(header, scroller);
    }

    //~ ====================================================================================================================================

    private AppNav createNavigation() {
        var nav = new AppNav();

        if (accessChecker.hasAccess(DashboardView.class)) {
            nav.addItem(new AppNavItem("Dashboard", DashboardView.class, LineAwesomeIcon.ELLO.create()));
        }
        if (accessChecker.hasAccess(FoodCountGridHistoryView.class)) {
            nav.addItem(new AppNavItem("Data", FoodCountGridHistoryView.class, LineAwesomeIcon.ACCUSOFT.create()));
        }
        return nav;
    }

    //~ ====================================================================================================================================

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        // viewTitle.setText("diva");
    }
}
