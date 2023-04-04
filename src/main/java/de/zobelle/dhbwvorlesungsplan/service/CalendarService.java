package de.zobelle.dhbwvorlesungsplan.service;

import de.zobelle.dhbwvorlesungsplan.model.Appointment;
import de.zobelle.dhbwvorlesungsplan.model.Person;
import de.zobelle.dhbwvorlesungsplan.model.Room;
import de.zobelle.dhbwvorlesungsplan.repository.AppointmentRepository;
import de.zobelle.dhbwvorlesungsplan.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.util.RandomUidGenerator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CalendarService {
    private final AppointmentRepository appointmentRepository;
    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public String createCalendarForCourse(String courseName) {
        log.info("createCalendarForCourse: " + courseName);
        List<Appointment> appointments = appointmentRepository.findAllByCoursesUrlEncodedNameEqualsAndEndGreaterThanEqual(courseName,
                Instant.now().minus(30, ChronoUnit.DAYS).atZone(ZoneId.of("Europe/Berlin")).truncatedTo(ChronoUnit.DAYS).toInstant());
        return createCalendar(appointments);
    }

    @Transactional(readOnly = true)
    public String createCalendarForIncludedCoursesWithoutExcludedCourses(Collection<String> includedCourseNames, Collection<String> excludedCourseNames) {
        log.info("createCalendarForCourse: " + includedCourseNames + " excluding: " + excludedCourseNames);
        List<Appointment> appointments = appointmentRepository.findDistinctByIncludedCoursesNameWithoutExcludedCoursesNameAndEndGreaterThanEqual(includedCourseNames, excludedCourseNames,
                Instant.now().minus(30, ChronoUnit.DAYS).atZone(ZoneId.of("Europe/Berlin")).truncatedTo(ChronoUnit.DAYS).toInstant());
        return createCalendar(appointments);
    }

    @Transactional(readOnly = true)
    public String createCalendarForInstructor(String instructorName) {
        log.info("createCalendarForInstructor: " + instructorName);
        List<Appointment> appointments = appointmentRepository.findAllByPersonsUrlEncodedNameEqualsAndEndGreaterThanEqual(instructorName,
                Instant.now().minus(30, ChronoUnit.DAYS).atZone(ZoneId.of("Europe/Berlin")).truncatedTo(ChronoUnit.DAYS).toInstant());
        return createCalendar(appointments);
    }

    @Transactional(readOnly = true)
    public String createCalendarForRoom(String roomName) {
        log.info("createCalendarForRoom: " + roomName);
        List<Appointment> appointments = appointmentRepository.findAllByRoomsUrlEncodedNameEqualsAndEndGreaterThanEqual(roomName,
                Instant.now().minus(30, ChronoUnit.DAYS).atZone(ZoneId.of("Europe/Berlin")).truncatedTo(ChronoUnit.DAYS).toInstant());
        return createCalendar(appointments);
    }

    private String createCalendar(List<Appointment> appointments) {
        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//DHBW Vorlesungsplan//DE"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);

        RandomUidGenerator uidGenerator = new RandomUidGenerator();

        for (Appointment appointment : appointments) {
            // TODO Verify date locale
            VEvent vEvent = new VEvent(
                    new DateTime(DateTime.from(appointment.getStart())),
                    new DateTime(DateTime.from(appointment.getEnd())),
                    appointment.getName());

            // TODO Replace with UUID from appointment
            vEvent.getProperties().add(uidGenerator.generateUid());
            vEvent.getProperties().add(new Location(StringUtils.join(appointment.getRooms().stream().map(Room::getName).collect(Collectors.toSet()), "\n")));
            vEvent.getProperties().add(new Description(StringUtils.join(appointment.getPersons().stream().map(Person::getFullName).collect(Collectors.toSet()), ", ")));

            calendar.getComponents().add(vEvent);
        }
        return calendar.toString();
    }
}
