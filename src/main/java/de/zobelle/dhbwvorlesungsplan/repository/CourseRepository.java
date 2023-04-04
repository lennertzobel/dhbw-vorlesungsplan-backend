package de.zobelle.dhbwvorlesungsplan.repository;

import de.zobelle.dhbwvorlesungsplan.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin("*")
@RepositoryRestResource
public interface CourseRepository extends JpaRepository<Course, UUID> {
    @RestResource
    @Query("SELECT c FROM Course c WHERE SUBSTRING(c.name, 1, 3) = :location AND SUBSTRING(c.name, 5, 1) = :faculty AND c.name LIKE %:year%")
    List<Course> findAllByLocationEqualsAndFacultyEqualsAndYearLike(String location, String faculty, String year, Sort sort);

    @Override
    @RestResource
    Page<Course> findAll(Pageable pageable);

    @Override
    @RestResource
    Optional<Course> findById(UUID uuid);

    @RestResource
    List<Course> findByIdIn(Collection<UUID> uuids);

    @RestResource
    @Query(
            value = "SELECT DISTINCT c2.* " +
                    "FROM course c1 JOIN course c2 ON c2.name LIKE CONCAT(LEFT(c1.name, (REGEXP_INSTR(c1.name, '[0-9]')+1)), '%') " +
                    "WHERE c1.name <> c2.name AND c1.name REGEXP '[0-9]' AND c1.id = :uuid",
            nativeQuery = true
    )
    List<Course> findSimilarById(String uuid);

    List<Course> findByNameIn(Collection<String> names);

    List<Course> findByUrlEncodedNameIn(Collection<String> urlEncodedNames);
}
