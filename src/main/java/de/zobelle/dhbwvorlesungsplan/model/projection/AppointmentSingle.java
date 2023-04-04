package de.zobelle.dhbwvorlesungsplan.model.projection;

import de.zobelle.dhbwvorlesungsplan.model.Appointment;
import de.zobelle.dhbwvorlesungsplan.model.Course;
import de.zobelle.dhbwvorlesungsplan.model.Person;
import de.zobelle.dhbwvorlesungsplan.model.Room;
import org.springframework.data.rest.core.config.Projection;

import java.time.Instant;
import java.util.Set;

@Projection(name = "appointment-single", types = Appointment.class)
public interface AppointmentSingle {
    String getName();

    Instant getStart();

    Instant getEnd();

    String getDuration();

    Set<Course> getCourses();

    Set<Person> getPersons();

    Set<Room> getRooms();
}
