package de.zobelle.dhbwvorlesungsplan.repository;

import de.zobelle.dhbwvorlesungsplan.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@CrossOrigin("*")
@RepositoryRestResource
public interface RoomRepository extends JpaRepository<Room, UUID> {
    @Override
    @RestResource
    Page<Room> findAll(Pageable pageable);

    @Override
    @RestResource
    Optional<Room> findById(UUID uuid);

    List<Room> findByNameIn(Collection<String> names);
}
