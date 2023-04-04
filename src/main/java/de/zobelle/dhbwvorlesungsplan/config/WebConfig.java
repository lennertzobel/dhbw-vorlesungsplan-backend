package de.zobelle.dhbwvorlesungsplan.config;

import de.zobelle.dhbwvorlesungsplan.model.Appointment;
import de.zobelle.dhbwvorlesungsplan.model.Course;
import de.zobelle.dhbwvorlesungsplan.model.Person;
import de.zobelle.dhbwvorlesungsplan.model.Room;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class WebConfig implements WebMvcConfigurer, RepositoryRestConfigurer {
    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        config.disableDefaultExposure();
        config.exposeIdsFor(Appointment.class, Course.class, Person.class, Room.class);
    }
}
