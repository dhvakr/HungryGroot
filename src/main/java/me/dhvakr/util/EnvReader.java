package me.dhvakr.util;

public class EnvReader {

    //~ Methods ============================================================================================================================

    public static EnvReader getInstance() {
        return EnvReader.InstanceHolder.INSTANCE;
    }

    //~ ====================================================================================================================================

    public String getValue(String key) {
        return System.getenv(key);
    }

    public String getValue(String key, String defaultKey){
        return System.getenv().getOrDefault(key, defaultKey);
    }

    //~ Inner Classes ======================================================================================================================

    private static final class InstanceHolder {

        //~ Static fields/initializers =====================================================================================================

        private static final EnvReader INSTANCE = new EnvReader();
    }
}
