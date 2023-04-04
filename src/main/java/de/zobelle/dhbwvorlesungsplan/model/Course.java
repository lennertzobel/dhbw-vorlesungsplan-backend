package de.zobelle.dhbwvorlesungsplan.model;

import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Course {
    @Id
    @Type(type = "uuid-char")
    @GeneratedValue
    private UUID id;
    private String name;
    private String urlEncodedName;
    @ManyToMany
    @JoinTable(name = "appointment_course", joinColumns = @JoinColumn(name = "course_id"), inverseJoinColumns = @JoinColumn(name = "appointment_id"))
    private Set<Appointment> appointments;
}
