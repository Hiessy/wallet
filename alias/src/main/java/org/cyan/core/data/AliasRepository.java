package org.cyan.core.data;

import org.cyan.core.data.model.Alias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AliasRepository extends JpaRepository<Alias, Long> {
    Optional<Alias> findByName(String name);
    boolean existsByName(String name);
}