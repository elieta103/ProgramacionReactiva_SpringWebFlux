package com.elhg.webflux.apirest.controllers;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.elhg.webflux.apirest.models.documents.Producto;
import com.elhg.webflux.apirest.models.service.ProductoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

	private static final String PATH = "C://Workspace//Code//cursosUdemy//ProgramacionReactiva_SpringWebFlux//uploads//";

	private ProductoService service;
	
	public ProductoController(ProductoService service) {
		this.service = service;
	}

	
	@PostMapping("/v2") //Crea producto con su respectiva foto
	public Mono<ResponseEntity<Producto>> crearConFoto(Producto producto, @RequestPart FilePart file){
		
		if(producto.getCreateAt()==null) {
			producto.setCreateAt(new Date());
		}
		
		producto.setFoto(UUID.randomUUID().toString() + "-" + file.filename()
		.replace(" ", "")
		.replace(":", "")
		.replace("\\", ""));
		
		return file.transferTo(new File(PATH + producto.getFoto())).then(service.save(producto))
				//map recibe Producto transforma y devuelve ResponseEntity<Producto>
			    // map, Toma el item emitido por el Mono y lo transforma Mono(Producto pasa a ResponseEntity<Producto>)
				.map(p-> ResponseEntity
				.created(URI.create("/api/productos/".concat(p.getId())))
				.contentType(MediaType.APPLICATION_JSON)
				.body(p)
				);
		
	}
	
	@PostMapping("/upload/{id}")    // Solamente Guarda la foto del producto, ya debe existir producto
	public Mono<ResponseEntity<Producto>> upload(@PathVariable String id, @RequestPart FilePart file){
		//flatMap, recibe Mono<Producto>  emite un Mono<Producto>
		//then, ejecuta un flujo una vez que termina el primero
		//map recibe Producto transforma y devuelve ResponseEntity<Producto>
	    // map, Toma el item emitido por el Mono y lo transforma Mono(Producto pasa a ResponseEntity<Producto>)
		return service.findById(id).flatMap(p -> {
			p.setFoto(UUID.randomUUID().toString() + "-" + file.filename()
			.replace(" ", "")
			.replace(":", "")
			.replace("\\", ""));
			
			return file.transferTo(new File(PATH + p.getFoto())).then(service.save(p));
		}).map(p -> ResponseEntity.ok(p))
		.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@GetMapping
	public Mono<ResponseEntity<Flux<Producto>>> lista(){
		return Mono.just(
				ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(service.findAll())
				);
	}
	
	@GetMapping("/{id}")
	public Mono<ResponseEntity<Producto>> ver(@PathVariable String id){
		// Mono<Producto> se le aplica el map, para obtener el Mono<ResponseEntity<Producto>>
		// map, Toma el item emitido por el Mono y lo transforma Mono(Producto pasa a ResponseEntity<Producto>)
		// Se le aplica el map, porque ResponseEntity<Producto> no es rectivo, ni Flux, ni Mono
		return service.findById(id).map(p -> ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(p))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@PostMapping   // Se puede probar con un {}, para ver los errores
	public Mono<ResponseEntity<Map<String, Object>>> crear(@Valid @RequestBody Mono<Producto> monoProducto){
		Map<String, Object> response = new HashMap<String, Object>();
		
		// flatMap, Toma el item emitido por el Mono y devuelve otro Mono probablemente modificado
		// Toma Mono(Producto se modifica a ResponseEntity<Producto>)
		return monoProducto.flatMap(producto ->{
			if(producto.getCreateAt()==null) {
				producto.setCreateAt(new Date());
			}
			// map, Toma el item emitido por el Mono y lo transforma Mono(Producto pasa a ResponseEntity<Producto>)
			// Se le aplica el map, porque ResponseEntity<Producto> no es rectivo, ni Flux, ni Mono		
			return service.save(producto).map(p-> {
				response.put("producto", p);
				response.put("mensaje", "Producto creado con éxito.");
				response.put("timestamp", new java.util.Date());
				
				return ResponseEntity  //Devuelve un Mono<ResponseEntity<Map<String,Object>>>
							.created(URI.create("/api/productos/".concat(p.getId()))) // Agrega un header: Location : /api/productos/633f18f8ded65256e6b3a589
							.contentType(MediaType.APPLICATION_JSON)
							.body(response);
			});	
		// Manejo del error, lanza un Throwable, el mas generico.
		}).onErrorResume(tr ->{
			return Mono.just(tr)	//Con Mono.just, tr se convierte a un tipo reactivo
					   .cast(WebExchangeBindException.class)  //Convierte a a una exception concreta
					   .flatMap(ex -> Mono.just(ex.getFieldErrors()) )  // Toma la exception, la convierte a una lista de errores(FieldError) y vuelve a emitir un tipo reactivo Mono<Object>
					   .flatMapMany(list -> Flux.fromIterable(list)) //Convierte Mono<List> a Flux<List>, para poder iterarla
					   .map(fieldError -> "El campo "+fieldError.getField()+" "+fieldError.getDefaultMessage()) //Cada item de la lista que es un FieldError, se convierte a un String
					   .collectList()  //Convierte el Flux<String> a Mono<List<String>>
					   .flatMap(list -> { //Convierte Mono<List<String>>  a un Mono<ResponseEntity<List<String>>>
						   response.put("errores", list);
						   response.put("timestamp", new java.util.Date());
						   response.put("status", HttpStatus.BAD_REQUEST.value());
							
						   return Mono.just(ResponseEntity.badRequest().body(response)); //Devuelve un Mono<ResponseEntity<Map<String,Object>>>
					   });
					   
		});
				
	}
	
	@PutMapping("/{id}")
	public Mono<ResponseEntity<Producto>> editar(@RequestBody Producto producto, @PathVariable String id) {
		// Se utiliza flatMap para que vuelva a emitir un Mono<Producto>. Lee un
		// Mono<Producto> modifica y emite un Mono<Producto>
		return service.findById(id).flatMap(p -> {
			p.setNombre(producto.getNombre());
			p.setPrecio(producto.getPrecio());
			p.setCategoria(producto.getCategoria());
			return service.save(p);
			// Mono<Producto> se le aplica el map, para obtener el Mono<ResponseEntity<Producto>>
			// map, Toma el item emitido por el Mono y lo transforma Mono(Producto pasa a ResponseEntity<Producto>)
			// Se le aplica el map, porque ResponseEntity<Producto> no es rectivo, ni Flux, ni Mono		
		}).map(p -> ResponseEntity
				.created(URI.create("/api/productos/".concat(p.getId()))) // Agrega un header: Location : /api/productos/633f18f8ded65256e6b3a589
				.contentType(MediaType.APPLICATION_JSON)  
				.body(p))
				.defaultIfEmpty(ResponseEntity.notFound().build());  //ResponseEntity Vacio, si no encuentra nada
	}
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> eliminar(@PathVariable String id){
		// Se utiliza flatMap para que vuelva a emitir un Mono<Void>. Lee un
		// Mono<Producto> modifica y emite un Mono<Void>
		// Se aplica el then a Mono<Void>, para obtener  Mono<ResponseEntity<Void>>
		return service.findById(id).flatMap(p ->{
			return service.delete(p).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
		}).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));  //ResponseEntity Vacio, si no encuentra nada
	}

}
