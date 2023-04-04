package de.zobelle.dhbwvorlesungsplan.service;

import de.zobelle.dhbwvorlesungsplan.model.Appointment;
import de.zobelle.dhbwvorlesungsplan.model.Course;
import de.zobelle.dhbwvorlesungsplan.model.Person;
import de.zobelle.dhbwvorlesungsplan.model.Room;
import de.zobelle.dhbwvorlesungsplan.repository.AppointmentRepository;
import de.zobelle.dhbwvorlesungsplan.repository.CourseRepository;
import de.zobelle.dhbwvorlesungsplan.repository.PersonRepository;
import de.zobelle.dhbwvorlesungsplan.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppointmentService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String APPOINTMENT_URL = "https://rapla.dhbw.de/rapla/calendar.csv?key=2a9Bq7PTVcTMvCSNwYoQRrO0GK9bccz-i39YnTK__wfzLf7zoDljz6ez6o-rF2Fssfs0IzT-9go2MmAnqMJiPibv24empchmyLIBVY3XjvdFhXxrSKQFBqE30JCP6y_6&salt=11988947&allocatable_id=";
    private static final String[] UMLAUTS = new String[]{"Ä", "Ö", "Ü", "ä", "ö", "ü", "ß"};
    private static final String[] UMLAUTS_REPLACEMENT = new String[]{"AE", "OE", "UE", "ae", "oe", "ue", "ss"};
    private static final CSVFormat CSV_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader("Name", "Beginn", "Ende", "Kurs", "Person", "Raum", "Dauer")
            .setQuote(null).setDelimiter(';').setSkipHeaderRecord(true).build();

    private final AppointmentRepository appointmentRepository;
    private final CourseRepository courseRepository;
    private final PersonRepository personRepository;
    private final RoomRepository roomRepository;

    @Retryable(backoff = @Backoff(delay = 10000))
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.SERIALIZABLE)
    public void fetchAppointments() {
        log.info("fetchAppointments started");

        try (CSVParser csvParser = CSVParser.parse(new URL(APPOINTMENT_URL), Charset.forName("windows-1252"), CSV_FORMAT)) {
            List<CSVRecord> csvRecords = csvParser.getRecords();

            Map<String, Course> existingCourses = saveNewCourses(csvRecords);
            Map<String, Person> existingPersons = saveNewPersons(csvRecords);
            Map<String, Room> existingRooms = saveNewRooms(csvRecords);

            Collection<Appointment> appointments = new ArrayList<>();
            for (CSVRecord csvRecord : csvRecords) {

                String name = StringUtils.trim(csvRecord.get("Name"));

                Instant start = LocalDateTime
                        .parse(csvRecord.get("Beginn"), DATE_TIME_FORMATTER)
                        .atZone(ZoneId.of("Europe/Berlin"))
                        .toInstant();

                Instant end = LocalDateTime
                        .parse(csvRecord.get("Ende"), DATE_TIME_FORMATTER)
                        .atZone(ZoneId.of("Europe/Berlin"))
                        .toInstant();

                Set<Course> associatedCourses = getAssociatedCourses(csvRecord, existingCourses);

                Set<Person> associatedPersons = getAssociatedPersons(csvRecord, existingPersons);

                Set<Room> associatedRooms = getAssociatedRooms(csvRecord, existingRooms);

                String duration = csvRecord.get("Dauer");

                appointments.add(Appointment
                        .builder()
                        .name(name)
                        .start(start)
                        .end(end)
                        .courses(associatedCourses)
                        .persons(associatedPersons)
                        .rooms(associatedRooms)
                        .duration(duration)
                        .build());
            }

            appointmentRepository.deleteAll();

            appointmentRepository.saveAll(appointments);

            log.info("fetchAppointments finished");
        } catch (IOException e) {
            log.error("fetchAppointments finished with error", e);
        }
    }

    private Set<Person> getAssociatedPersons(CSVRecord csvRecord, Map<String, Person> existingPersons) {
        Set<Person> associatedPersons = new HashSet<>();
        Iterator<String> personIterator = Arrays.asList(StringUtils.split(csvRecord.get("Person"), ',')).iterator();
        while (personIterator.hasNext()) {
            Person toBeAssociatedPerson = createPerson(personIterator.next(), personIterator.next());
            associatedPersons.add(existingPersons.get(toBeAssociatedPerson.getFullName()));
        }
        return associatedPersons;
    }

    private Person createPerson(String lastName, String firstName) {
        String normalizedLastName = StringUtils.normalizeSpace(lastName);
        String normalizedFirstName = StringUtils.normalizeSpace(firstName);
        String urlEncodedName = StringUtils.lowerCase(UriUtils.encode(StringUtils.stripAccents(StringUtils.replaceEach(StringUtils.replace(normalizedFirstName + "-" + normalizedLastName, " ", "-"), UMLAUTS, UMLAUTS_REPLACEMENT)), StandardCharsets.UTF_8));
        return Person.builder()
                .firstName(normalizedFirstName)
                .lastName(normalizedLastName)
                .urlEncodedName(urlEncodedName)
                .build();
    }

    private Map<String, Person> saveNewPersons(List<CSVRecord> csvRecords) {
        Map<String, Person> csvPersons = new HashMap<>();
        for (CSVRecord csvRecord : csvRecords) {
            Iterator<String> personIterator = Arrays.asList(StringUtils.split(csvRecord.get("Person"), ',')).iterator();
            while (personIterator.hasNext()) {
                Person person = createPerson(personIterator.next(), personIterator.next());
                csvPersons.putIfAbsent(person.getFullName(), person);
            }
        }
        List<Person> existingCsvPersons = personRepository.findByLastNameInAndFirstNameIn(
                csvPersons.values().stream().map(Person::getLastName).collect(Collectors.toList()),
                csvPersons.values().stream().map(Person::getFirstName).collect(Collectors.toList())
        );
        Map<String, Person> newPersons = new HashMap<>(csvPersons);
        newPersons.keySet().removeAll(existingCsvPersons.stream().map(Person::getFullName).collect(Collectors.toList()));
        existingCsvPersons.addAll(personRepository.saveAll(newPersons.values()));
        return existingCsvPersons.stream().collect(Collectors.toMap(Person::getFullName, Function.identity()));
    }

    private Set<Room> getAssociatedRooms(CSVRecord csvRecord, Map<String, Room> existingRooms) {
        Set<Room> associatedRooms = new HashSet<>();
        Iterator<String> roomIterator = Arrays.stream(StringUtils.split(csvRecord.get("Raum"), ',')).iterator();
        while (roomIterator.hasNext()) {
            Room toBeAssociatedRoom = createRoom(roomIterator.next());
            associatedRooms.add(existingRooms.get(toBeAssociatedRoom.getName()));
        }
        return associatedRooms;
    }

    private Room createRoom(String name) {
        String normalizedName = StringUtils.normalizeSpace(name);
        String urlEncodedName = StringUtils.lowerCase(UriUtils.encode(normalizedName, StandardCharsets.UTF_8));
        return Room.builder()
                .name(normalizedName)
                .urlEncodedName(urlEncodedName)
                .build();
    }

    private Map<String, Room> saveNewRooms(List<CSVRecord> csvRecords) {
        Map<String, Room> csvRooms = new HashMap<>();
        for (CSVRecord csvRecord : csvRecords) {
            Iterator<String> roomIterator = Arrays.stream(StringUtils.split(csvRecord.get("Raum"), ',')).iterator();
            while (roomIterator.hasNext()) {
                Room room = createRoom(roomIterator.next());
                csvRooms.putIfAbsent(room.getName(), room);
            }
        }
        List<Room> existingCsvRooms = roomRepository.findByNameIn(
                csvRooms.values().stream().map(Room::getName).collect(Collectors.toList())
        );
        Map<String, Room> newRooms = new HashMap<>(csvRooms);
        newRooms.keySet().removeAll(existingCsvRooms.stream().map(Room::getName).collect(Collectors.toList()));
        existingCsvRooms.addAll(roomRepository.saveAll(newRooms.values()));
        return existingCsvRooms.stream().collect(Collectors.toMap(Room::getName, Function.identity()));
    }

    private Set<Course> getAssociatedCourses(CSVRecord csvRecord, Map<String, Course> existingCourses) {
        Set<Course> associatedCources = new HashSet<>();
        Iterator<String> courseIterator = Arrays.stream(StringUtils.split(csvRecord.get("Kurs"), ',')).iterator();
        while (courseIterator.hasNext()) {
            Course toBeAssociatedCourse = createCourse(courseIterator.next());
            associatedCources.add(existingCourses.get(toBeAssociatedCourse.getName()));
        }
        return associatedCources;
    }

    private Course createCourse(String name) {
        String normalizedName = StringUtils.normalizeSpace(name);
        String urlEncodedName = StringUtils.lowerCase(UriUtils.encode(StringUtils.replaceEach(StringUtils.replace(normalizedName, " ", "-"), UMLAUTS, UMLAUTS_REPLACEMENT), StandardCharsets.UTF_8));
        return Course.builder()
                .name(normalizedName)
                .urlEncodedName(urlEncodedName)
                .build();
    }

    private Map<String, Course> saveNewCourses(List<CSVRecord> csvRecords) {
        Map<String, Course> csvCourses = new HashMap<>();
        for (CSVRecord csvRecord : csvRecords) {
            Iterator<String> courseIterator = Arrays.stream(StringUtils.split(csvRecord.get("Kurs"), ',')).iterator();
            while (courseIterator.hasNext()) {
                Course course = createCourse(courseIterator.next());
                csvCourses.putIfAbsent(course.getName(), course);
            }
        }
        List<Course> existingCsvCourses = courseRepository.findByNameIn(
                csvCourses.values().stream().map(Course::getName).collect(Collectors.toList())
        );
        Map<String, Course> newCourses = new HashMap<>(csvCourses);
        newCourses.keySet().removeAll(existingCsvCourses.stream().map(Course::getName).collect(Collectors.toList()));
        existingCsvCourses.addAll(courseRepository.saveAll(newCourses.values()));
        return existingCsvCourses.stream().collect(Collectors.toMap(Course::getName, Function.identity()));
    }
}
