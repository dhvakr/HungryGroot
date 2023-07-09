package me.dhvakr.ui.account;

import me.dhvakr.jpa.service.GrootService;
import me.dhvakr.models.ForgotPasswordModel;
import me.dhvakr.models.SignUpFormModel;
import me.dhvakr.security.AuthenticatedUser;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import lombok.extern.slf4j.Slf4j;
import me.dhvakr.constants.Constants;

@Slf4j
@AnonymousAllowed
@PageTitle("Sign Up and Forgot Password | Hungry Groot")
@Route(value = "groot-account")
public class AccountCreateOrUpdateView extends VerticalLayout implements BeforeEnterObserver {

    //~ Static fields/initializers =========================================================================================================

    private static final String LOG_TAG = Constants.DEFAULT_LOG_TAG + "ACCOUNT_GETTING_STARTED_VIEW ";

    //~ Instance fields ====================================================================================================================

    private final AuthenticatedUser authenticatedUser;
    private final GrootService grootService;
    private boolean enablePasswordValidation;

    /*
     * Binder is a form utility class. Here, we use a specialized version to gain access to
     * automatic Bean Validation (JSR-303). We provide our
     * data class so that the Binder can read the validation definitions on that
     * class and create appropriate validators. The BeanValidationBinder can
     * automatically validate all JSR-303 definitions, meaning we can concentrate on
     * custom things such as the passwords in this class.
     */
    private BeanValidationBinder<SignUpFormModel> signUpFormBinder = new BeanValidationBinder<>(SignUpFormModel.class);
    private BeanValidationBinder<ForgotPasswordModel> forgetPasswordBinder = new BeanValidationBinder<>(ForgotPasswordModel.class);

    // SIGNUP::STARTS

    private final EmailField emailField = new EmailField("Groot Email");
    private final TextField forgotPasswordKey = new TextField("Forgot Password Key");
    private PasswordField passwordField = new PasswordField("Password");
    private PasswordField conformPasswordField = new PasswordField("Confirm Password");
    private Button signUpSubmitButton = new Button("Submit");

    // SIGNUP::ENDS

    //~ DIFFER =============================================================================================================================

    // FORGET::PASSWORD::STARTS

    private final EmailField forgetEmailCheckField = new EmailField("Your Email");
    private PasswordField forgetPasswordField = new PasswordField("New Password");
    private final TextField forgotPasswordKeyValidation = new TextField("Forgot Password Key");
    private Button forgotPasswordSubmitButton = new Button("Submit");

    // FORGET::PASSWORD::ENDS

    //~ Constructor ========================================================================================================================

    public AccountCreateOrUpdateView(AuthenticatedUser authenticatedUser, GrootService grootService) {
        this.authenticatedUser = authenticatedUser;
        this.grootService = grootService;

        // Dialog for getting model-form view
        var dialog = new Dialog();
        dialog.setResizable(true);
        dialog.setDraggable(true);
        dialog.setCloseOnOutsideClick(false);
        dialog.setCloseOnEsc(false);

        // Created tabs for different view for signUp and forgetPassword
        Tabs tabs = new Tabs();
        var signupFormTab = new Tab("Signup");
        var forgotPasswordFormTab = new Tab("Forgot Password");
        tabs.add(signupFormTab, forgotPasswordFormTab);

        // SIGNUP::STARTS
        Span errorMessage = new Span();

        // Add the form to the page
        dialog.add(tabs, createSignUpFormView(errorMessage));
        // Open a dialog without any interaction
        dialog.open();
        // Add the form to the dialog page
        add(dialog);

        // removing tab on change, just to make sure one or more tab don't get overridden
        tabs.addSelectedChangeListener(selectedChangeEvent -> {
            if (selectedChangeEvent.getSelectedTab().equals(signupFormTab)) {
                dialog.remove(createForgetPasswordFormView(errorMessage));
                dialog.add(createSignUpFormView(errorMessage));
            } else {
                dialog.remove(createSignUpFormView(errorMessage));
                dialog.add(createForgetPasswordFormView(errorMessage));
            }
        });
    }

    //~ Methods ============================================================================================================================

    /**
     * Signup Form view
     *
     * @param errorMessage to show error message in bean validation
     * @return Component of {@link FormLayout}
     */
    private Component createSignUpFormView(Span errorMessage) {
        var formLayout = new FormLayout(emailField, forgotPasswordKey,
                passwordField, conformPasswordField, errorMessage, signUpSubmitButton);

        // Restrict maximum width and center on page
        formLayout.setMaxWidth("500px");
        formLayout.getStyle().set("margin", "0 auto");

        // Allow the form layout to be responsive. On device widths 0-490px we have one
        // column, then we have two. Field labels are always on top of the fields.
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.ASIDE),
                new FormLayout.ResponsiveStep("490px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP));

        // These components take full width regardless if we use one column or two (it just looks better that way)
        formLayout.setColspan(errorMessage, 2);
        formLayout.setColspan(signUpSubmitButton, 2);

        // Add some styles to the error message to make it pop out
        errorMessage.getStyle().set("color", "var(--lumo-error-text-color)");
        errorMessage.getStyle().set("padding", "15px 0");
        signUpSubmitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // 1.) The handle has a custom validator, in addition to being required. Some values
        // are not allowed, such as 'admin@dhvakr.me, help@dhvakr.me'...; this is checked in the validator.
        emailField.setPlaceholder("groot.g@dhvakr.me");

        // 2.) EmailField uses a Validator that extends one of the built-in ones.
        signUpFormBinder.forField(emailField).asRequired(new GrootEmailChecker("Value is not a valid email address", grootService)).bind("email");

        // 3.) Forget Password Key use to validate the user on forget password scenario
        forgotPasswordKey.setHelperText("When you forgot your password, this information will be used to identify you. Be cautious when giving the key.");
        signUpFormBinder.forField(forgotPasswordKey).asRequired().bind("forgotPasswordKey");

        // 4.) Another custom validator, this time for passwords
        signUpFormBinder.forField(passwordField).asRequired().withValidator(this::passwordValidator).bind("password");

        // The second field is not connected to the Binder, but we want the binder to
        // re-check the password validator when the field value changes. The easiest way
        // is just to do that manually.
        conformPasswordField.setRequired(true);
        conformPasswordField.addValueChangeListener(e -> {
            // The groot has modified the second field, now we can validate and show errors.
            enablePasswordValidation = true;
            signUpFormBinder.validate();
        });
        // A label where bean-level error messages go
        signUpFormBinder.setStatusLabel(errorMessage);

        signUpSubmitButton.addClickShortcut(Key.ENTER);
        // And finally the submit button
        signUpSubmitButton.addClickListener(signUpSubmitEvent -> {
            try {
                var grootDetailsBean = new SignUpFormModel();
                // Run validators and write the values to the bean
                signUpFormBinder.writeBean(grootDetailsBean);
                // call to store the value
                grootService.createNewGroot(grootDetailsBean);
                showSuccess(grootDetailsBean);
            } catch (GrootService.ServiceException | ValidationException ex) {
                log.error(LOG_TAG + "Exception on createSignUpFormView() " + ex.getMessage());
            }
        });
        return formLayout;
    }

    //~ ====================================================================================================================================

    /**
     * Forget-password Form view
     *
     * @param errorMessage to show error message in bean validation
     * @return Component of {@link FormLayout}
     */
    private Component createForgetPasswordFormView(Span errorMessage) {
        var formLayout = new FormLayout(forgetEmailCheckField, forgotPasswordKeyValidation,
                forgetPasswordField, forgotPasswordSubmitButton);

        // Restrict maximum width and center on page
        formLayout.setMaxWidth("500px");
        formLayout.getStyle().set("margin", "0 auto");

        // These components take full width regardless if we use one column or two (it just looks better that way)
        formLayout.setColspan(errorMessage, 2);
        formLayout.setColspan(forgotPasswordSubmitButton, 2);

        forgotPasswordSubmitButton.getStyle().set("margin-top", "20px");
        forgotPasswordSubmitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        forgotPasswordKeyValidation.setHelperText("The key you used on signUp");

        // Binder section for forgetPassword tab view
        // 1.) Exists Email Check
        forgetPasswordBinder.forField(forgetEmailCheckField).asRequired(new GrootEmailChecker("No Such Email Address", true, grootService)).bind("validateEmail");

        // 2.) Password Key Verification Check
        forgetPasswordBinder.forField(forgotPasswordKeyValidation).asRequired().withValidator(this::passwordKeyValidator).bind("forgotPasswordKeyValidation");
        forgotPasswordKeyValidation.addValueChangeListener(event -> forgetPasswordBinder.validate());

        // 3.) New Password Length check
        forgetPasswordBinder.forField(forgetPasswordField).asRequired().bind("newPassword");

        // A label where bean-level error messages go
        forgetPasswordBinder.setStatusLabel(errorMessage);

        forgotPasswordSubmitButton.addClickShortcut(Key.ENTER);
        // And finally the submit form if no errors
        forgotPasswordSubmitButton.addClickListener(forgetPasswordSubmitEvent -> {
            try {
                var grootPasswordResetDetails = new ForgotPasswordModel();
                // Run validators and write the values to the bean
                forgetPasswordBinder.writeBean(grootPasswordResetDetails);
                // call to store the value
                grootService.updateGrootPassword(grootPasswordResetDetails);
                showSuccess(grootPasswordResetDetails);
            } catch (ValidationException ex) {
                log.error(LOG_TAG + "Exception on createForgetPasswordFormView() " + ex.getMessage());
            }
        });
        return formLayout;
    }

    //~ ====================================================================================================================================

    /**
     * To show success notify and redirect to dashboard when sign-up form submission has succeeded
     */
    private void showSuccess(SignUpFormModel grootDetailsBean) {
        Notification.show("You made it " + grootDetailsBean.getEmail(),
                7000, Notification.Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        UI.getCurrent().navigate("/login");
    }

    //~ ====================================================================================================================================

    /**
     * To show success notify and redirect to dashboard when forgot-password form submission has succeeded
     */
    private void showSuccess(ForgotPasswordModel grootDetailsBean) {
        Notification.show("You password is reset " + grootDetailsBean.getValidateEmail(),
                7000, Notification.Position.BOTTOM_START).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        UI.getCurrent().navigate("/login");
    }

    //~ ====================================================================================================================================

    /**
     * Method to validate that:
     * </p>
     * 1) Password is at least 4 characters long
     * 2) Values in both password fields match each other
     */
    private ValidationResult passwordValidator(String password, ValueContext valueContext) {
        if (password == null || password.length() < 4)
            return ValidationResult.error("Password should be at least 4 characters long");
        if (enablePasswordValidation && !password.equals(conformPasswordField.getValue()))
            return ValidationResult.error("Passwords do not match");
        else
            return ValidationResult.ok();
    }

    //~ ====================================================================================================================================

    /**
     * Validated by forgetPasswordKey set's by each groot
     *
     * @param forgotPasswordKey key to identify the groot
     * @param valueContext {@link ValueContext}
     * @return {@link ValidationResult} result
     */
    private ValidationResult passwordKeyValidator(String forgotPasswordKey, ValueContext valueContext) {
        if (forgotPasswordKey == null || forgotPasswordKey.isEmpty())
            return ValidationResult.error("The key should not be empty");
        else if (!grootService.existsByForgotPasswordKey(forgotPasswordKey.strip())) {
            forgotPasswordKeyValidation.setHelperText("");
            return ValidationResult.error("The key does not match an identified user");
        }
        else
            return ValidationResult.ok();
    }

    //~ ====================================================================================================================================

    /**
     * Returns the user to dashboard if the user present in the session
     *
     * @param event {@link BeforeEnterEvent}
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticatedUser.get().isPresent()) {
            // Already logged in
            event.forwardTo("/dashboard");
        }
    }

    //~ Inner Classes ======================================================================================================================

    /**
     * Custom validator class that extends the built-in email validator.
     * </P>
     * Ths validator checks if the field is visible before performing the
     * validation. This way, the validation is only performed when the user has told
     * us they want marketing emails.
     */
    public static class GrootEmailChecker extends EmailValidator {

        //~ Instance fields ====================================================================================================================

        private GrootService grootService;
        private boolean forgotPasswordCheckEnable;

        //~ Constructor ========================================================================================================================

        public GrootEmailChecker(String errorMessage, GrootService grootService) {
            super(errorMessage);
            this.grootService = grootService;
        }

        //~ ====================================================================================================================================

        public GrootEmailChecker(String errorMessage, boolean forgotPasswordCheckEnable, GrootService grootService) {
            super(errorMessage);
            this.forgotPasswordCheckEnable = forgotPasswordCheckEnable;
            this.grootService = grootService;
        }

        //~ Method ============================================================================================================================

        @Override
        public ValidationResult apply(String value, ValueContext context) {
            if (forgotPasswordCheckEnable) {
                if (!value.endsWith("dhvakr.me"))
                    return ValidationResult.error("not a valid dhvakr mail");
                else if (!grootService.existsByEmail(value.trim()))
                    return ValidationResult.error("No such email exits in the record 😐");
            }
            else {
                if (!value.endsWith("dhvakr.me"))
                    return ValidationResult.error("not a valid dhvakr mail");
                else if (GrootService.RESERVED_USERNAMES_HANDLES.contains(value))
                    return ValidationResult.error("Reserved dhvakr mail");
                else if (grootService.existsByEmail(value.trim()))
                    return ValidationResult.error("Email already exits, Try logging in");
            }
            return super.apply(value, context);
        }
    }
}
