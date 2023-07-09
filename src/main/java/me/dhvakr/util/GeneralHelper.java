package me.dhvakr.util;

import lombok.extern.slf4j.Slf4j;
import me.dhvakr.constants.Constants;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class GeneralHelper {

    //~ Static fields/initializers =========================================================================================================

    private static final String LOG_TAG = Constants.DEFAULT_LOG_TAG + "HELPER: ";

    //~ Constructor ========================================================================================================================

    private GeneralHelper() {}

    //~ Methods ============================================================================================================================

    public static GeneralHelper getInstance() {
        return GeneralHelper.InstanceHolder.INSTANCE;
    }

    //~ ====================================================================================================================================

    /**
     * Get a random value form a given list and spilt the absolute path with resource folder alone
     *
     * @param resource list of string
     * @return random value form a list
     */
    public String getRandomValueFromList(List<String> resource) {
        var path = resource.get(new Random().nextInt(resource.size()));
        String[] spitedPath = path.split("/resources");
        return Arrays.stream(spitedPath).toList().get(1);
    }

    //~ ====================================================================================================================================

    /**
     * Utility method to remove the [ ] brackets from a set type of strings
     *
     * @param foodPreference string that want to remove [ ]
     * @return value without [ ] in start and end of string
     */
    private Set<String> removeSquareBrackets(Set<String> foodPreference) {
        return foodPreference.stream()
                .map(str -> str.replaceAll("^\\[|\\]$", ""))
                .collect(Collectors.toSet());
    }

    //~ Inner Class ========================================================================================================================

    private static final class InstanceHolder {

        //~ Static fields/initializers =====================================================================================================

        private static final GeneralHelper INSTANCE = new GeneralHelper();
    }
}
