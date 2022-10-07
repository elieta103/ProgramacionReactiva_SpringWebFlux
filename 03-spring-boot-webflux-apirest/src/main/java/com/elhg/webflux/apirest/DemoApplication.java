package com.elhg.webflux.apirest;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.elhg.webflux.apirest.models.documents.Categoria;
import com.elhg.webflux.apirest.models.documents.Producto;
import com.elhg.webflux.apirest.models.service.ProductoService;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {
	private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

	private com.elhg.webflux.apirest.models.service.ProductoService service;
	private ReactiveMongoTemplate mongoTemplate;

	public DemoApplication(ProductoService service, ReactiveMongoTemplate mongoTemplate) {
		this.service = service;
		this.mongoTemplate = mongoTemplate;
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		mongoTemplate.dropCollection("productos").subscribe();
		mongoTemplate.dropCollection("categorias").subscribe();
		
		Categoria electronico = new Categoria("Electrónico");
		Categoria deporte = new Categoria("Deporte");
		Categoria computacion = new Categoria("Computación");
		Categoria muebles = new Categoria("Muebles");
		Categoria herramienta = new Categoria("Herramienta");
		
		
		Flux.just(electronico, deporte, computacion, muebles, herramienta)
			.flatMap(service::saveCategoria)
			.doOnNext(cat -> log.info("Insertando categoria {} ", cat.toString()))
			.thenMany(
					Flux.just(new Producto("TV Panasonic Pantalla LCD", 456.89, electronico),
							new Producto("Sony Camara HD Digital", 177.89, electronico),
							new Producto("Apple iPod", 46.89, computacion),
							new Producto("Sony Notebook", 846.89, computacion),
							new Producto("HP Multifuncional", 200.89, computacion),
							new Producto("Bianchi Bicicleta", 150.89, deporte),
							new Producto("Mica Cómoda 5 Cajones", 70.89, muebles),
							new Producto("HP Notebook Omen 17", 2500.89, computacion))
						.flatMap(prod -> {
							prod.setCreateAt(new Date());
							return service.save(prod);
							})   					
					)
			.subscribe(prod -> log.info("Insertando producto {} ", prod.toString()));
		 
		//flatMap Flux<Producto> accedo a props de Producto
		//FlatMap transforms the items emitted by an Observable into Observables.
		
		//map Mono<Producto> NO accedo a props de Producto:   
		//Map transforms the items emitted by an Observable by applying a function to each item.
		
	}

	
	
}
