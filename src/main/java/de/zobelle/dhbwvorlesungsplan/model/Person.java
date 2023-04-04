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
public class Person {
    @Id
    @Type(type = "uuid-char")
    @GeneratedValue
    private UUID id;
    private String firstName;
    private String lastName;
    private String urlEncodedName;
    @ManyToMany
    @JoinTable(name = "appointment_person", joinColumns = @JoinColumn(name = "person_id"), inverseJoinColumns = @JoinColumn(name = "appointment_id"))
    private Set<Appointment> appointments;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
