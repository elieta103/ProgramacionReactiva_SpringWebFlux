package com.elhg.springboot.webflux.app.controller;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

import com.elhg.springboot.webflux.app.models.documents.Categoria;
import com.elhg.springboot.webflux.app.models.documents.Producto;
import com.elhg.springboot.webflux.app.models.services.ProductoService;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext.Empty;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class ProductoController {

	private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

	private ProductoService service;
	
	private static final String RUTA_DIRECTORIO_IMG = "C://Workspace//Code//cursosUdemy//ProgramacionReactiva_SpringWebFlux//uploads//";
		

	public ProductoController(ProductoService service) {
		this.service = service;
	}

	@ModelAttribute("categorias")
	public Flux<Categoria> categorias() {
		return service.findAllCategoria();
	}

	
	@GetMapping("/upload/img/{nombreFoto:.+}")  // :.+  Para agregar la extension
	public Mono<ResponseEntity<Resource>> verFoto (@PathVariable String nombreFoto) throws MalformedURLException{
		
		Path ruta = Paths.get(RUTA_DIRECTORIO_IMG).resolve(nombreFoto).toAbsolutePath();
		Resource imagen = new UrlResource(ruta.toUri());
		return Mono.just(
				ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imagen.getFilename() + "\"")
				.body(imagen)
				);
	}
	
	@GetMapping({ "/ver/{id}" })  // Detalle del producto No devuelve la Imagen
	public Mono<String> ver(Model model, @PathVariable String id){
		
		return service.findById(id)
				.doOnNext(prod -> {
					model.addAttribute("producto", prod);
					model.addAttribute("titulo", "Detalle de Producto");
				})
				//.defaultIfEmpty(new Producto())   // Alternativa a switchIfEmpty
				.switchIfEmpty(Mono.just(new Producto()))  //Si esta vacio devuelve un Mono<Producto>  Vacio
				.flatMap(p ->{
					if(p.getId()==null) {
						return Mono.error(new InterruptedException("No existe el producto"));
					}
					return Mono.just(p);
				})
				.then(Mono.just("ver")) //Vista en caso de exito
				.onErrorResume(ex -> Mono.just("redirect:/listar?error=No+existe+el+producto"));  //Vista en caso de error
	}
	
	@GetMapping({ "/listar" })
	public Mono<String> listar(Model model) {
		Flux<Producto> productos = service.findAllConNombreUpperCase();

		model.addAttribute("productos", productos);
		model.addAttribute("titulo", "Listado de productos");

		return Mono.just("listar");
	}

	@GetMapping({ "/form" }) // Mostrar el formulario
	public Mono<String> crear(Model model) {
		model.addAttribute("producto", new Producto());
		model.addAttribute("titulo", "Formulario  de producto");
		model.addAttribute("boton", "Crear");
		return Mono.just("form");
	}

	@GetMapping({ "/form-v2/{id}" }) // Editar
	public Mono<String> editarV2(@PathVariable String id, Model model) {

		return service.findById(id).doOnNext(p -> {
			log.info("Producto : " + p.getNombre());
			model.addAttribute("producto", p);
			model.addAttribute("titulo", "Editar producto");
			model.addAttribute("boton", "Editar");
		}).defaultIfEmpty(new Producto()).flatMap(p -> {
			if (p.getId() == null) {
				return Mono.error(new InterruptedException("No existe el producto"));
			}
			return Mono.just(p);
		}).then(Mono.just("form")).onErrorResume(ex -> Mono.just("redirect:/listar?error=No+existe+el+producto"));
	}

	@GetMapping({ "/form/{id}" }) // Editar
	public Mono<String> editar(@PathVariable String id, Model model) {
		Mono<Producto> productoMono = service.findById(id).doOnNext(p -> {
			log.info("Producto : " + p.getNombre());
		}).defaultIfEmpty(new Producto());

		model.addAttribute("producto", productoMono);
		model.addAttribute("titulo", "Editar producto");
		model.addAttribute("boton", "Editar");
		return Mono.just("form");
	}

	@PostMapping({ "/form" }) // Guardar el formulario
	public Mono<String> guardar(@Valid Producto producto, BindingResult result, Model model, @RequestPart FilePart file) {
		if (result.hasErrors()) {
			model.addAttribute("titulo", "Errores en formulario de producto");
			model.addAttribute("boton", "Guardar");
			return Mono.just("form");
		} else {
			log.info("file {}", file.filename().toString());
			Mono<Categoria> monoCategoria = service.findCategoriaById(producto.getCategoria().getId());

			return monoCategoria
					.flatMap(cat -> { // Recibe Mono<Categoria>, Devuelve un Mono<Producto> TRANSFORMA
						if (producto.getCreateAt() == null) {
							producto.setCreateAt(new Date());
						}
						if(!file.filename().isEmpty()) {
							producto.setFoto(UUID.randomUUID().toString()+"-"+file.filename()
							.replace(" ", "")
							.replace(":", "")
							.replace("\\", ""));
						}
							
						producto.setCategoria(cat);
						return service.save(producto);
						})
					.doOnNext(p -> { //Recibe MonoProducto, Devuelve un MonoProducto NO TRANSFORMA AGREGA FUNCIONALIDAD
						log.info("Categoria asignada.  Id: {}, Nombre: {}", p.getCategoria().getId(), p.getCategoria().getNombre());
						log.info("Producto guardado.  Id: {}, Nombre: {}", p.getId(), p.getNombre());
					})  //Devuelve un Mono<String>
					.flatMap(p -> { // Recibe Mono<Producto>, Devuelve un Mono<Void> TRANSFORMA
						if(!file.filename().isEmpty()) {
							return file.transferTo(new File(RUTA_DIRECTORIO_IMG+p.getFoto()));
						}
						return Mono.empty();
					})
					.then(Mono.just("redirect:/listar?success=Producto+guardado+con+exito"));  //Devuelve Mono<String>
		}

	}

	@GetMapping({ "/eliminar/{id}" })
	public Mono<String> eliminar(@PathVariable String id, Model model) {

		return service.findById(id).defaultIfEmpty(new Producto()).flatMap(p -> {
			if (p.getId() == null) {
				return Mono.error(new InterruptedException("No existe el producto"));
			}
			return Mono.just(p);
		}).flatMap(p -> {
			Mono<Void> monoProducto = service.delete(p);
			log.info("Eliminando producto Id : {} Nombre : {}", p.getId(), p.getNombre());
			return monoProducto;
		}).then(Mono.just("redirect:/listar?success=Producto+eliminado+con+exito"))
				.onErrorResume(ex -> Mono.just("redirect:/listar?error=No+existe+el+producto"));

	}

	@GetMapping({ "/listar-datadriver" })
	public String listarDataDrive(Model model) {
		Flux<Producto> productos = service.findAllConNombreUpperCase().delayElements(Duration.ofSeconds(1));

		productos.subscribe(p -> log.info(p.toString()));

		model.addAttribute("productos", new ReactiveDataDriverContextVariable(productos, 1));
		model.addAttribute("titulo", "Listado de productos");
		return "listar";
	}

	@GetMapping({ "/listar-full" })
	public String listarFull(Model model) {
		Flux<Producto> productos = service.findAllConNombreUpperCaseRepeat();

		model.addAttribute("productos", productos);
		model.addAttribute("titulo", "Listado de productos");

		return "listar";
	}

	@GetMapping({ "/listar-chunked" })
	public String listarChunked(Model model) {
		Flux<Producto> productos = service.findAllConNombreUpperCaseRepeat();

		model.addAttribute("productos", productos);
		model.addAttribute("titulo", "Listado de productos");

		return "listar-chunked";
	}

}
