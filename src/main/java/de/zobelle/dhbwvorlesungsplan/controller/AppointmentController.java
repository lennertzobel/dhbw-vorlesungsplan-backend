package de.zobelle.dhbwvorlesungsplan.controller;

import de.zobelle.dhbwvorlesungsplan.service.AppointmentService;
import de.zobelle.dhbwvorlesungsplan.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final CalendarService calendarService;

    @GetMapping(value = "ical/course/{courseName}", produces = "text/calendar;charset=UTF-8")
    public String getCalendarForCourse(@PathVariable String courseName) {
        return calendarService.createCalendarForCourse(courseName);
    }

    @GetMapping(value = "ical/course/multi", produces = "text/calendar;charset=UTF-8")
    public String getCalendarForIncludedCoursesWithoutExcludedCourses(@RequestParam Collection<String> includedCourseNames, @RequestParam Collection<String> excludedCourseNames) {
        return calendarService.createCalendarForIncludedCoursesWithoutExcludedCourses(includedCourseNames, excludedCourseNames);
    }

    @GetMapping(value = "ical/instructor/{instructorName}", produces = "text/calendar;charset=UTF-8")
    public String getCalendarForInstructor(@PathVariable String instructorName) {
        return calendarService.createCalendarForInstructor(instructorName);
    }

    @GetMapping(value = "ical/room/{roomName}", produces = "text/calendar;charset=UTF-8")
    public String getCalendarForRoom(@PathVariable String roomName) {
        return calendarService.createCalendarForRoom(roomName);
    }

    @GetMapping("start")
    public void fetchAppointments() {
        appointmentService.fetchAppointments();
    }
}
