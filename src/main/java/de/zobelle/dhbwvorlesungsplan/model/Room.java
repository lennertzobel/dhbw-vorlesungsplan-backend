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
public class Room {
    @Id
    @Type(type = "uuid-char")
    @GeneratedValue
    private UUID id;
    private String name;
    private String urlEncodedName;
    @ManyToMany
    @JoinTable(name = "appointment_room", joinColumns = @JoinColumn(name = "room_id"), inverseJoinColumns = @JoinColumn(name = "appointment_id"))
    private Set<Appointment> appointments;
}
