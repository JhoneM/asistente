package com.uade.vidification.repository;

import com.uade.vidification.model.Habito;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HabitoRepository extends JpaRepository<Habito, Long> {

    List<Habito> findByUsuarioId(Long usuarioId);
}
