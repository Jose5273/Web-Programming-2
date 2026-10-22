package uce.desarrolloweb.ProgramacionWeb001.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import uce.desarrolloweb.ProgramacionWeb001.Model.Libro;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    // Search by title (case-insensitive, partial match)
    List<Libro> findByTituloContainingIgnoreCase(String titulo);

    // Find by category
    List<Libro> findByCategoriaIgnoreCase(String categoria);
}