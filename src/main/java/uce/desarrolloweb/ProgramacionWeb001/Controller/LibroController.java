package uce.desarrolloweb.ProgramacionWeb001.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uce.desarrolloweb.ProgramacionWeb001.Model.Libro;
import uce.desarrolloweb.ProgramacionWeb001.Service.LibroService;

@Controller
public class LibroController {

    @Autowired
    private LibroService libroService;

    // ============================================================
    // SERVER-SIDE ROUTES (Thymeleaf views)
    // ============================================================

    // ---------- HOME PAGE: 6 random books ----------
    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("listaLibro", libroService.listarAleatorios(6));
        return "index";
    }

    // ---------- List all books ----------
    @GetMapping("/libros/lista")
    public String lista(Model model) {
        model.addAttribute("listaLibro", libroService.listar());
        return "libro/lista";
    }

    // ---------- Show create form ----------
    @GetMapping("/libros/formulario")
    public String formulario(Model model) {
        model.addAttribute("libro", new Libro());
        model.addAttribute("modo", "crear");
        return "libro/formulario";
    }

    // ---------- Save (create or update) via form submit ----------
    @PostMapping("/libros/guardar")
    public String guardar(@ModelAttribute("libro") Libro libro) {
        libroService.guardar(libro);
        return "redirect:/libros/lista";
    }

    // ---------- Show edit form ----------
    @GetMapping("/libros/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Libro libro = libroService.buscar(id);
        if (libro == null) {
            return "redirect:/libros/lista";
        }
        model.addAttribute("libro", libro);
        model.addAttribute("modo", "editar");
        return "libro/formulario";
    }

    // ---------- Delete via link ----------
    @GetMapping("/libros/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        libroService.eliminar(id);
        return "redirect:/libros/lista";
    }

    // ---------- Search (server-side, returns list view) ----------
    @GetMapping("/libros/buscar")
    public String buscar(@RequestParam(value = "q", required = false) String q, Model model) {
        if (q == null || q.isBlank()) {
            model.addAttribute("listaLibro", libroService.listar());
        } else {
            model.addAttribute("listaLibro", libroService.buscarPorTitulo(q));
        }
        model.addAttribute("query", q);
        return "libro/lista";
    }


    // ============================================================
    // REST ENDPOINTS (JSON) — used by the interactive index.html
    // ============================================================

    // ---------- GET one book as JSON ----------
    @GetMapping("/api/libros/{id}")
    @ResponseBody
    public ResponseEntity<Libro> apiBuscar(@PathVariable Long id) {
        Libro libro = libroService.buscar(id);
        if (libro == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(libro);
    }

    // ---------- GET all books as JSON ----------
    @GetMapping("/api/libros")
    @ResponseBody
    public ResponseEntity<java.util.List<Libro>> apiListar() {
        return ResponseEntity.ok(libroService.listar());
    }

    // ---------- CREATE a book (JSON in / JSON out) ----------
    @PostMapping("/api/libros")
    @ResponseBody
    public ResponseEntity<Libro> apiCrear(@RequestBody Libro libro) {
        // Force INSERT: ignore any id sent by the client
        libro.setId(null);
        Libro saved = libroService.guardar(libro);
        return ResponseEntity.ok(saved);
    }

    // ---------- UPDATE a book (JSON in / JSON out) ----------
    @PutMapping("/api/libros/{id}")
    @ResponseBody
    public ResponseEntity<Libro> apiActualizar(@PathVariable Long id,
                                               @RequestBody Libro libro) {
        Libro existente = libroService.buscar(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }

        // Copy fields from request onto the managed entity
        existente.setTitulo(libro.getTitulo());
        existente.setAutor(libro.getAutor());
        existente.setEditorial(libro.getEditorial());
        existente.setAnioPublicacion(libro.getAnioPublicacion());
        existente.setCategoria(libro.getCategoria());
        existente.setUbicacionEstante(libro.getUbicacionEstante());
        existente.setDisponible(libro.getDisponible());
        existente.setDescripcion(libro.getDescripcion());
        existente.setImagenUrl(libro.getImagenUrl());
        existente.setIsbn(libro.getIsbn());

        Libro saved = libroService.guardar(existente);
        return ResponseEntity.ok(saved);
    }

    // ---------- DELETE a book ----------
    @DeleteMapping("/api/libros/{id}")
    @ResponseBody
    public ResponseEntity<Void> apiEliminar(@PathVariable Long id) {
        Libro existente = libroService.buscar(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }
        libroService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}