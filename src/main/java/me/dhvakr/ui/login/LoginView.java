package me.dhvakr.ui.login;

import me.dhvakr.security.AuthenticatedUser;
import me.dhvakr.ui.account.AccountCreateOrUpdateView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import lombok.extern.slf4j.Slf4j;
import me.dhvakr.constants.Constants;
import me.dhvakr.constants.LoginPage;

@Slf4j
@AnonymousAllowed
@PageTitle("Login | Hungry Groot")
@Route(value = "login")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    //~ Static fields/initializers =========================================================================================================

    private static final String LOG_TAG = Constants.DEFAULT_LOG_TAG + "LOGIN_VIEW ";
    private LoginOverlay login = new LoginOverlay();
    private final AuthenticatedUser authenticatedUser;

    //~ Constructor ========================================================================================================================

    public LoginView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
        login.setAction(RouteUtil.getRoutePath(VaadinService.getCurrent().getContext(), getClass()));

        LoginI18n i18n = LoginI18n.createDefault();
        i18n.setHeader(new LoginI18n.Header());
        i18n.getErrorMessage().setTitle("Invalid Username/Password");
        i18n.getErrorMessage().setMessage("");

        LoginI18n.Header i18nHeader = i18n.getHeader();
        i18nHeader.setTitle(LoginPage.LOGIN_TITLE);
        i18nHeader.setDescription(LoginPage.LOGIN_DESCRIPTION);

        LoginI18n.Form i18nForm = i18n.getForm();
        i18nForm.setUsername(LoginPage.LOGIN_USER_NAME_TEXT);
        i18nForm.setPassword(LoginPage.LOGIN_PASSWORD_TEXT);
        i18nForm.setSubmit(LoginPage.LOGIN_SUBMIT_BUTTON_TEXT);
        i18nForm.setForgotPassword(LoginPage.SIGN_UP_FORGET_PASSWORD);
        i18n.setForm(i18nForm);

        i18n.setAdditionalInformation(LoginPage.LOGIN_HELP_TEXT);

        login.setI18n(i18n);

        login.setForgotPasswordButtonVisible(true);
        /*
         *  we're using forget password listener with combined for signUp and forgetPassword view
         *  since LoginOverlay doesn't design for signUp sadly, Also not ready to create a separate loginForm
         *  So going with a little tweak with addForgotPasswordListener() to add multiform with Tabs
         */
        login.addForgotPasswordListener(forgotPasswordEvent -> {
            login.close();
            UI.getCurrent().navigate(AccountCreateOrUpdateView.class);
        });
        login.setOpened(true);
    }

    //~ Methods ============================================================================================================================

    /**
     * Returns the user to dashboard if the user present in the session
     *
     * @param event {@link BeforeEnterEvent}
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticatedUser.get().isPresent()) {
            // Already logged in
            login.setOpened(false);
            event.forwardTo("");
        }
        login.setError(event.getLocation().getQueryParameters().getParameters().containsKey("error"));
    }
}
