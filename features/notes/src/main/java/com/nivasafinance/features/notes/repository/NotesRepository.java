package com.nivasafinance.features.notes.repository;

import com.nivasafinance.features.notes.entity.Notes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotesRepository extends JpaRepository<Notes, Long> {
    Optional<Notes> findByIdentifier(UUID identifier);
}

