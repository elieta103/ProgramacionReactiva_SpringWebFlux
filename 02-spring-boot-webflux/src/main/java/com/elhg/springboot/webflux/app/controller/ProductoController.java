package com.elhg.springboot.webflux.app.controller;

import java.time.Duration;
import java.util.Date;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;

import com.elhg.springboot.webflux.app.models.documents.Producto;
import com.elhg.springboot.webflux.app.models.services.ProductoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class ProductoController {

	private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

	private ProductoService service;

	public ProductoController(ProductoService service) {
		this.service = service;
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
		}).defaultIfEmpty(new Producto())
		  .flatMap(p ->{
			  if(p.getId() == null) {
				  return Mono.error(new InterruptedException("No existe el producto"));
			  }
			  return Mono.just(p);
		  })
		  .then(Mono.just("form"))
		  .onErrorResume(ex ->Mono.just("redirect:/listar?error=No+existe+el+producto"));
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
	public Mono<String> guardar(@Valid Producto producto, BindingResult result, Model model) {
		if(result.hasErrors()) {
			model.addAttribute("titulo", "Errores en formulario de producto");
			model.addAttribute("boton", "Guardar");
			return Mono.just("form");
		}else {
			
			if(producto.getCreateAt() == null) {
				producto.setCreateAt(new Date());
			}
			
			return service.save(producto).doOnNext(p -> {
				log.info("Producto guardado.  Id: {}, Nombre: {}", p.getId(), p.getNombre());
			}).then(Mono.just("redirect:/listar?success=Producto+guardado+con+exito"));			
		}
		
	}
	
	
	@GetMapping({"/eliminar/{id}"})
	public Mono<String> eliminar(@PathVariable String id, Model model){
		
		return service.findById(id)
				.defaultIfEmpty(new Producto())
				.flatMap(p->{
					if(p.getId()== null) {
						return Mono.error(new InterruptedException("No existe el producto"));
					}
					return Mono.just(p);
				})
				.flatMap(p -> {
					Mono<Void> monoProducto = service.delete(p);
					log.info("Eliminando producto Id : {} Nombre : {}", p.getId(), p.getNombre());
					return monoProducto;
				})
				.then(Mono.just("redirect:/listar?success=Producto+eliminado+con+exito"))
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
