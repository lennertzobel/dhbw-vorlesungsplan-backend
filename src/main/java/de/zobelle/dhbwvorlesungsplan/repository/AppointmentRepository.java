package de.zobelle.dhbwvorlesungsplan.repository;

import de.zobelle.dhbwvorlesungsplan.model.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin("*")
@RepositoryRestResource
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    @Override
    @RestResource
    Page<Appointment> findAll(Pageable pageable);

    @Override
    @RestResource
    Optional<Appointment> findById(UUID uuid);

    @RestResource
    Page<Appointment> findAllByCoursesIdEqualsAndEndGreaterThanEqual(UUID uuid, Instant instant, Pageable pageable);

    @RestResource
    Page<Appointment> findAllByPersonsIdEqualsAndEndGreaterThanEqual(UUID uuid, Instant instant, Pageable pageable);

    @RestResource
    Page<Appointment> findAllByRoomsIdEqualsAndEndGreaterThanEqual(UUID uuid, Instant instant, Pageable pageable);

    @RestResource
    @Query(
            value = "SELECT DISTINCT a.* " +
                    "FROM appointment a " +
                    "LEFT JOIN (SELECT appointment_id FROM appointment_course WHERE course_id IN (:excludedIds)) exclude ON a.id = exclude.appointment_id " +
                    "JOIN (SELECT appointment_id FROM appointment_course WHERE course_id IN (:includedIds)) include ON a.id = include.appointment_id " +
                    "WHERE exclude.appointment_id IS NULL " +
                    "AND a.end >= :instant",
            countQuery = "SELECT COUNT(DISTINCT a.id) " +
                    "FROM appointment a " +
                    "LEFT JOIN (SELECT appointment_id FROM appointment_course WHERE course_id IN (:excludedIds)) exclude ON a.id = exclude.appointment_id " +
                    "JOIN (SELECT appointment_id FROM appointment_course WHERE course_id IN (:includedIds)) include ON a.id = include.appointment_id " +
                    "WHERE exclude.appointment_id IS NULL " +
                    "AND a.end >= :instant",
            nativeQuery = true)
    Page<Appointment> findDistinctByIncludedCoursesIdWithoutExcludedCoursesIdAndEndGreaterThanEqual(Collection<String> includedIds, Collection<String> excludedIds, Instant instant, Pageable pageable);

    @Query(
            value = "SELECT DISTINCT a.* " +
                    "FROM appointment a " +
                    "LEFT JOIN (SELECT appointment_id FROM appointment_course JOIN course ON course_id = id WHERE url_encoded_name IN (:excluded)) exclude ON a.id = exclude.appointment_id " +
                    "JOIN (SELECT appointment_id FROM appointment_course JOIN course ON course_id = id WHERE url_encoded_name IN (:included)) include ON a.id = include.appointment_id " +
                    "WHERE exclude.appointment_id IS NULL " +
                    "AND a.end >= :instant",
            countQuery = "SELECT COUNT(DISTINCT a.id) " +
                    "FROM appointment a " +
                    "LEFT JOIN (SELECT appointment_id FROM appointment_course JOIN course ON course_id = id WHERE url_encoded_name IN (:excluded)) exclude ON a.id = exclude.appointment_id " +
                    "JOIN (SELECT appointment_id FROM appointment_course JOIN course ON course_id = id WHERE url_encoded_name IN (:included)) include ON a.id = include.appointment_id " +
                    "WHERE exclude.appointment_id IS NULL " +
                    "AND a.end >= :instant",
            nativeQuery = true)
    List<Appointment> findDistinctByIncludedCoursesNameWithoutExcludedCoursesNameAndEndGreaterThanEqual(Collection<String> included, Collection<String> excluded, Instant instant);

    List<Appointment> findAllByCoursesUrlEncodedNameEqualsAndEndGreaterThanEqual(String courseName, Instant instant);

    List<Appointment> findAllByPersonsUrlEncodedNameEqualsAndEndGreaterThanEqual(String courseName, Instant instant);

    List<Appointment> findAllByRoomsUrlEncodedNameEqualsAndEndGreaterThanEqual(String courseName, Instant instant);

    void deleteAllByStartGreaterThanEqual(Instant instant);
}
