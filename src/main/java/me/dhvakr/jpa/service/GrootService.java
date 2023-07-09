package me.dhvakr.jpa.service;

import me.dhvakr.enums.Roles;
import me.dhvakr.jpa.entity.Groots;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import me.dhvakr.models.FoodHistoryGrid;
import me.dhvakr.models.ForgotPasswordModel;
import me.dhvakr.models.SignUpFormModel;
import lombok.extern.slf4j.Slf4j;
import me.dhvakr.constants.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GrootService implements UserDetailsService {

    //~ Static fields/initializers =========================================================================================================

    private static final String LOG_TAG = Constants.DEFAULT_LOG_TAG + "GROOT_SERVICE ";
    public static final List<String> RESERVED_USERNAMES_HANDLES = Arrays.asList("admin@dhvakr.me", "people@dhvakr.me", "groot@dhvakr.me");
    private final GrootRepository grootRepository;

    //~ Constructor ========================================================================================================================

    public GrootService(GrootRepository repository) {
        this.grootRepository = repository;
    }

    //~ Methods ============================================================================================================================

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Groots groot = grootRepository.findByUsername(username);
        if (groot == null) {
            throw new UsernameNotFoundException("No user present with username: " + username);
        } else {
            return new User(groot.getUsername(), groot.getPassword(),
                    getAuthorities(groot));
        }
    }

    //~ ====================================================================================================================================

    public Optional<Groots> findById(Long id) {
        return grootRepository.findById(id);
    }

    //~ ====================================================================================================================================

    public Groots findByEmail(String email) {
        return grootRepository.findByUsername(email);
    }

    //~ ====================================================================================================================================

    public boolean existsByEmail(String email) {
        return grootRepository.existsByUsername(email);
    }

    //~ ====================================================================================================================================

    public boolean existsByForgotPasswordKey(String forgotPasswordKey) {
        return grootRepository.existsByforgotPasswordKey(forgotPasswordKey);
    }

    //~ ====================================================================================================================================

    public Groots saveAndFlush(Groots entity) {
        return grootRepository.saveAndFlush(entity);
    }

    //~ ====================================================================================================================================

    public void delete(String id) {
        grootRepository.deleteById(Long.parseLong(id));
    }

    //~ ====================================================================================================================================

    public void deleteByFoodCountDateBefore(LocalDate date) {
       grootRepository.deleteByFoodCountDateBefore(date);
    }

    //~ ====================================================================================================================================

    public Page<Groots> list(Pageable pageable) {
        return grootRepository.findAll(pageable);
    }

    //~ ====================================================================================================================================

    public Page<Groots> list(Pageable pageable, Specification<Groots> filter) {
        return grootRepository.findAll(filter, pageable);
    }

    //~ ====================================================================================================================================

    private static List<GrantedAuthority> getAuthorities(Groots user) {
        return user.getRoles().stream().map(role -> new SimpleGrantedAuthority(Constants.ROLE_PREFIX + role))
                .collect(Collectors.toList());
    }

    //~ ====================================================================================================================================

    public int count() {
        return (int) grootRepository.count();
    }

    //~ ====================================================================================================================================
    /* Groot Account Registration */

    /**
     * 'Stores' the data from binder bean to database
     */
    public void createNewGroot(SignUpFormModel grootSignUpDetails) throws ServiceException {
        if (grootRepository.existsByUsername(grootSignUpDetails.getEmail().trim()))
            log.error(LOG_TAG + "Groot Already Exists : " + grootSignUpDetails.getEmail());

        Groots grootsEntry = new Groots();
        String email = grootSignUpDetails.getEmail();
        // Extract the name from email before (.) using a regular expression
        String dataPartBeforeAlias = email.replaceAll("^(.+)@.+$", "$1"); // for safety taking the string part before @ from email
        String getFirstNameFromEmail = email.replaceAll("^(.*?)\\..*$", "$1"); // if fails returns the same email
        String hashedPassword = BCrypt.hashpw(grootSignUpDetails.getPassword(), BCrypt.gensalt());

        grootsEntry.setUsername(email);
        grootsEntry.setName((getFirstNameFromEmail.equals(email)) ? dataPartBeforeAlias : getFirstNameFromEmail);
        grootsEntry.setForgotPasswordKey(grootSignUpDetails.getForgotPasswordKey().strip());
        grootsEntry.setPassword(hashedPassword);
        grootsEntry.setRoles(Collections.singleton(Roles.EMPLOYEE));
        grootRepository.saveAndFlush(grootsEntry);
        log.info(LOG_TAG + "Successfully Added " + grootSignUpDetails.getEmail());
    }

    //~ ====================================================================================================================================

    public void updateGrootPassword(ForgotPasswordModel grootPasswordResetDetails) {
        Groots grootsEntry = findByEmail(grootPasswordResetDetails.getValidateEmail().trim());
        String hashedPassword = BCrypt.hashpw(grootPasswordResetDetails.getNewPassword(), BCrypt.gensalt());
        grootsEntry.setPassword(hashedPassword);
        grootRepository.saveAndFlush(grootsEntry);
        log.info(LOG_TAG + "Password Updated for  " + grootPasswordResetDetails.getValidateEmail());
    }

    //~ ====================================================================================================================================
    /* Belongs to GRID View */

    /**
     * The method gives all groot data by joining with food history table data
     *
     * @return all Groot values with food history table
     */
    public List<Object> getAllRecordedFoodCount() {
        List<Object[]> recordedFoodCounts = grootRepository.getAllRecordedDataWithFoodHistory();
        return Collections.unmodifiableList(recordedFoodCounts);
    }

    //~ ====================================================================================================================================

    /**
     * The method gives all groot data required by Grid
     * Gives with a list if groot names with their counted date
     *
     * @param grootsPage pagination
     * @return with groot names by counted date
     */
    public List<FoodHistoryGrid> getTotalFoodHistoryListForGrid(Page<Groots> grootsPage) {
        return grootsPage.getContent().stream()
                .flatMap(groot -> groot.getFoodCountHistory()
                        .stream()
                        .map(foodCountHistory ->
                                new FoodHistoryGrid(groot.getName(), foodCountHistory.getFoodPreference(), foodCountHistory.getFoodCountDate())))
                .toList();
    }

    //~ ====================================================================================================================================

    /**
     * The method gives specific groot food data
     *
     * @return a list with requested groot value with food history table
     */
    public List<FoodHistoryGrid> filterByGrootName(LocalDate dateValue, String targetGrootName) {
        String filterText = targetGrootName.strip().toLowerCase(Locale.getDefault());
        return getTotalFoodHistoryListForGrid(list(Pageable.ofSize(10))).stream()
                .filter(foodHistoryGrid -> foodHistoryGrid.date().equals(dateValue))
                .filter(foodHistoryGrid -> foodHistoryGrid.grootName().toLowerCase(Locale.getDefault()).contains(filterText))
                .toList();
    }

    //~ ====================================================================================================================================

    /**
     * The method sort a groot data by date
     *
     * @return a list of values by given date
     */
    public List<FoodHistoryGrid> filterByDate(LocalDate dateValue, int pageSize) {
        return getTotalFoodHistoryListForGrid(list(Pageable.ofSize(pageSize))).stream()
                .filter(foodHistoryGrid -> foodHistoryGrid.date().equals(dateValue))
                .toList();
    }

    //~ ====================================================================================================================================

    /**
     * The method filtered the data by lunch or by dinner by selected date
     * to display in the grid layout
     *
     * @return a list of values by selected date based on foodPreference
     */
    public List<FoodHistoryGrid> filterByLunchOrDinner(LocalDate dateValue, Set<String> foodPreference, int pageSize) {
        return getTotalFoodHistoryListForGrid(list(Pageable.ofSize(pageSize))).stream()
                .filter(foodHistoryGrid -> foodHistoryGrid.date().equals(dateValue))
                .filter(foodHistoryGrid -> foodPreference.stream().anyMatch(foodHistoryGrid.foodPreference()::contains))
                .toList();
    }

    //~ Inner Classes ======================================================================================================================

    /**
     * Utility Exception class that we can use in the frontend to show that
     * something went wrong during some action.
     */
    public static class ServiceException extends Exception {

        //~ Method ============================================================================================================================

        public ServiceException(String msg) {
            super(msg);
        }
    }
}
