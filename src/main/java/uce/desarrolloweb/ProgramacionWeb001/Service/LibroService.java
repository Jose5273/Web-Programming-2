package uce.desarrolloweb.ProgramacionWeb001.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uce.desarrolloweb.ProgramacionWeb001.Model.Libro;
import uce.desarrolloweb.ProgramacionWeb001.Repository.LibroRepository;

@Service
public class LibroService {

    @Autowired
    private LibroRepository libroRepository;

    public List<Libro> listar() {
        return libroRepository.findAll();
    }

    public Libro buscar(Long id) {
        return libroRepository.findById(id).orElse(null);
    }

    public Libro guardar(Libro libro) {
        return libroRepository.save(libro);
    }

    public void eliminar(Long id) {
        libroRepository.deleteById(id);
    }

    public List<Libro> buscarPorTitulo(String titulo) {
        return libroRepository.findByTituloContainingIgnoreCase(titulo);
    }

    public List<Libro> buscarPorCategoria(String categoria) {
        return libroRepository.findByCategoriaIgnoreCase(categoria);
    }

    // Portable random selection (works on PostgreSQL, H2, MySQL, any DB)
    // Since the table is small (86 rows), loading all + shuffling is fine.
    public List<Libro> listarAleatorios(int cantidad) {
        List<Libro> all = new ArrayList<>(libroRepository.findAll());
        Collections.shuffle(all);
        return all.stream().limit(cantidad).toList();
    }
}