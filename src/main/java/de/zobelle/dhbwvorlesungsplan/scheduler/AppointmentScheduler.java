package de.zobelle.dhbwvorlesungsplan.scheduler;

import de.zobelle.dhbwvorlesungsplan.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppointmentScheduler {
    private final AppointmentService appointmentService;

    @Scheduled(cron = "0 5 * * * *")
    private void fetchAppointments() {
        appointmentService.fetchAppointments();
    }
}
