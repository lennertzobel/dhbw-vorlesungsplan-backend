package de.zobelle.dhbwvorlesungsplan.model;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Appointment {
    @Id
    @Type(type = "uuid-char")
    @GeneratedValue
    private UUID id;
    private String name;
    private Instant start;
    private Instant end;
    private String duration;
    @ManyToMany
    @JoinTable(name = "appointment_course", joinColumns = @JoinColumn(name = "appointment_id"), inverseJoinColumns = @JoinColumn(name = "course_id"))
    private Set<Course> courses;
    @ManyToMany
    @JoinTable(name = "appointment_person", joinColumns = @JoinColumn(name = "appointment_id"), inverseJoinColumns = @JoinColumn(name = "person_id"))
    private Set<Person> persons;
    @ManyToMany
    @JoinTable(name = "appointment_room", joinColumns = @JoinColumn(name = "appointment_id"), inverseJoinColumns = @JoinColumn(name = "room_id"))
    private Set<Room> rooms;
}
