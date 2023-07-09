package me.dhvakr;

import me.dhvakr.jpa.service.GrootService;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;
import me.dhvakr.constants.Constants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationProperties;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * The entry point of the Spring Boot application.
 * <p>
 * The @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 * </p>
 */
@Slf4j
@Theme(value = "groot")
@PWA(name = "Hungry Groot", shortName = "Hungry Groot", description = "dhvakr-Groot")
@SpringBootApplication
public class MainApplication implements AppShellConfigurator {

    //~ Static fields/initializers =========================================================================================================

    private static final String LOG_TAG = Constants.DEFAULT_LOG_TAG + "MAIN_APPLICATION ";
    public static final List<String> defaultUserAvatarPaths = new ArrayList<>();

    //~ Methods ============================================================================================================================

    public static void main(String... groot) {
        SpringApplication.run(MainApplication.class, groot);
    }

    //~ ====================================================================================================================================

    /**
     *  Listener that loads all default groot avatar from resource folder on application startUp
     *  to load into user profile picture when logged In
     */
    @EventListener(ApplicationStartedEvent.class)
    public void loadResourceProperties() {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("classpath:/META-INF/resources/img/default/*");
            for (Resource resource : resources) {
                defaultUserAvatarPaths.add(resource.getURL().toString());
            }
        } catch (Exception e) {
            log.warn(LOG_TAG + "NUll in loadResourceProperties() while getting default assets from resource folder");
        }
    }

    //~ ====================================================================================================================================

    /** This bean ensures the data.sql is only initialized when database is empty **/
    @Bean
    SqlDataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer(DataSource dataSource,
            SqlInitializationProperties properties, GrootService repository) {
        return new SqlDataSourceScriptDatabaseInitializer(dataSource, properties) {
            @Override
            public boolean initializeDatabase() {
                if (repository.count() == 0L) {
                    return super.initializeDatabase();
                }
                return false;
            }
        };
    }
}
