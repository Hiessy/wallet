package org.cyan.core.data;

import org.cyan.core.data.model.Alias;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AliasRepository extends JpaRepository<Alias, Long> {
    Alias findByName(String name);
}