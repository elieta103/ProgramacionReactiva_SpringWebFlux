package com.elhg.springboot.reactor.app;

import java.sql.Time;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.elhg.springboot.reactor.app.models.Comentarios;
import com.elhg.springboot.reactor.app.models.Usuario;
import com.elhg.springboot.reactor.app.models.UsuarioComentarios;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// simpleFlux();
		// simpleFluxConError();
		// simpleFluxConErrorThread();
		// simpleFluxMap();
		// simpleFluxFilter();
		// simpleFluxInmutable();
		// simpleFluxFromList();
		// simpleFluxFlatMap();
		// simpleFluxListAFluxString();
		// observableFluxAMonoList();
		// combinarDosFlujosConFlatMap();
		// combinarDosFlujosConZipWith();
		// combinarDosFlujosConZipWithForma2();
		// combinarDosFlujosConZipWithRangos();
		// ejemploInterval();
		// ejemploDelayElements();
		// ejemploIntervalInfinito();
		// ejemploIntervalDesdeCreate();
		ejemploContraPresion();
		
	}

	public void simpleFlux() {
		// Flux es un publisher, un observable
		Flux<String> nombres = Flux.just("Andres", "Pedro", "Diego", "Juan").doOnNext(System.out::println);
		// Suscribirse al observable
		nombres.subscribe(e -> log.info(e));
	}

	public void simpleFluxConError() {
		// Flux es un publisher, un observable
		Flux<String> nombres = Flux.just("Andres", "Pedro", "", "Diego", "Juan").doOnNext(elemento -> {
			if (elemento.isEmpty()) {
				throw new RuntimeException("No puede haber nombres vacios.");
			} else {
				System.out.println(elemento);
			}
		});
		// Suscribirse al observable
		nombres.subscribe(e -> log.info(e), error -> log.error(error.getMessage()));
	}

	public void simpleFluxConErrorThread() {
		// Flux es un publisher, un observable
		Flux<String> nombres = Flux.just("Andres", "Pedro", "Maria", "Diego", "Juan").doOnNext(elemento -> {
			if (elemento.isEmpty()) {
				throw new RuntimeException("No puede haber nombres vacios");
			} else {
				System.out.println(elemento);
			}
		});
		// Suscribirse al observable
		nombres.subscribe(e -> log.info(e), error -> log.error(error.getMessage()), new Runnable() {

			@Override
			public void run() {
				System.out.println("Ha finalizado la ejecución del observable.");
			}
		});
	}

	public void simpleFluxMap() {
		// Flux es un publisher, un observable
		Flux<Usuario> nombres = Flux.just("Andres", "Pedro", "Maria", "Diego", "Juan")
				.map(elementoStr -> new Usuario(elementoStr.toUpperCase(), null)).doOnNext(usuario -> {
					if (usuario == null) {
						throw new RuntimeException("Usuario no puede ser null");
					} else {
						System.out.println(usuario.getNombre());
					}
				}).map(usuario -> {
					String nombreLower = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombreLower);
					return usuario;
				});

		// Suscribirse al observable
		nombres.subscribe(e -> log.info(e.toString()), error -> log.error(error.getMessage()), new Runnable() {
			@Override
			public void run() {
				System.out.println("Ha finalizado la ejecución del observable con éxito.");
			}
		});
	}

	public void simpleFluxInmutable() {
		// Flux es un publisher, un observable
		Flux<String> nombres = Flux.just("Andres Guzman", "Pedro Fulano", "Maria Fulana", "Diego Sultano",
				"Juan Mengano", "Bruce Lee", "Bruce Willis");

		Flux<Usuario> usuarios = nombres
				.map(elemento -> new Usuario(elemento.split(" ")[0].toUpperCase(),
						elemento.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().equalsIgnoreCase("bruce")) // Evalua una expresion boleana
				.doOnNext(usuario -> {
					if (usuario == null) {
						throw new RuntimeException("Usuario no puede ser null");
					} else {
						System.out.println(usuario.toString());
					}
				}).map(usuario -> {
					String nombreLower = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombreLower);
					return usuario;
				});

		// Suscribirse al observable
		// Imprime el stream original, ya que son inmutables
		nombres.subscribe(e -> log.info(e.toString()), error -> log.error(error.getMessage()), new Runnable() {
			@Override
			public void run() {
				System.out.println("Ha finalizado la ejecución del observable [nombres] con éxito.");
			}
		});

		// Suscribirse al observable
		// Imprime el stream modificado
		usuarios.subscribe(e -> log.info(e.toString()), error -> log.error(error.getMessage()), new Runnable() {
			@Override
			public void run() {
				System.out.println("Ha finalizado la ejecución del observable [usuarios] con éxito.");
			}
		});
	}

	public void simpleFluxFilter() {
		// Flux es un publisher, un observable
		Flux<Usuario> nombres = Flux
				.just("Andres Guzman", "Pedro Fulano", "Maria Fulana", "Diego Sultano", "Juan Mengano", "Bruce Lee",
						"Bruce Willis")
				.map(elemento -> new Usuario(elemento.split(" ")[0].toUpperCase(),
						elemento.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().equalsIgnoreCase("bruce")) // Evalua una expresion boleana
				.doOnNext(usuario -> {
					if (usuario == null) {
						throw new RuntimeException("Usuario no puede ser null");
					} else {
						System.out.println(usuario.toString());
					}
				}).map(usuario -> {
					String nombreLower = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombreLower);
					return usuario;
				});

		// Suscribirse al observable
		nombres.subscribe(e -> log.info(e.toString()), error -> log.error(error.getMessage()), new Runnable() {
			@Override
			public void run() {
				System.out.println("Ha finalizado la ejecución del observable con éxito.");
			}
		});
	}

	public void simpleFluxFromList() {
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Andres Guzman");
		usuariosList.add("Pedro Fulano");
		usuariosList.add("Maria Fulana");
		usuariosList.add("Diego Sultano");
		usuariosList.add("Juan Mengano");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willis");

		// Flux es un publisher, un observable
		Flux<Usuario> nombres = Flux.fromIterable(usuariosList)
				.map(elemento -> new Usuario(elemento.split(" ")[0].toUpperCase(),
						elemento.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().equalsIgnoreCase("bruce")) // Evalua una expresion boleana
				.doOnNext(usuario -> {
					if (usuario == null) {
						throw new RuntimeException("Usuario no puede ser null");
					} else {
						System.out.println(usuario.toString());
					}
				}).map(usuario -> {
					String nombreLower = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombreLower);
					return usuario;
				});

		// Suscribirse al observable
		nombres.subscribe(e -> log.info(e.toString()), error -> log.error(error.getMessage()), new Runnable() {
			@Override
			public void run() {
				System.out.println("Ha finalizado la ejecución del observable con éxito.");
			}
		});
	}

	public void simpleFluxFlatMap() {
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Andres Guzman");
		usuariosList.add("Pedro Fulano");
		usuariosList.add("Maria Fulana");
		usuariosList.add("Diego Sultano");
		usuariosList.add("Juan Mengano");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willis");

		// Flux es un publisher, un observable
		// Map : convierte de un tipo de dato a otro, String -> Object
		// FlatMap : convierte de un tipo de dato a un tipo de flujo Mono,Flux
		Flux.fromIterable(usuariosList).map(
				elemento -> new Usuario(elemento.split(" ")[0].toUpperCase(), elemento.split(" ")[1].toUpperCase()))
				.flatMap(usuario -> { // Filtra los bruce y devuelve un Observable(Mono, Flux)
					if (usuario.getNombre().equalsIgnoreCase("bruce")) {
						return Mono.just(usuario);
					} else {
						return Mono.empty();
					}
				}).map(usuario -> {
					String nombreLower = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombreLower);
					return usuario;
				})
				// Suscribirse al observable Imprimir uno por uno
				.subscribe(user -> log.info(user.toString()));
	}

	public void simpleFluxListAFluxString() {
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Andres", "Guzman"));
		usuariosList.add(new Usuario("Pedro", "Fulano"));
		usuariosList.add(new Usuario("Maria", "Fulana"));
		usuariosList.add(new Usuario("Diego", "Sultano"));
		usuariosList.add(new Usuario("Juan", "Mengano"));
		usuariosList.add(new Usuario("Bruce", "Lee"));
		usuariosList.add(new Usuario("Bruce", "Willis"));

		// Flux es un publisher, un observable
		Flux.fromIterable(usuariosList)
				.map(usuario -> usuario.getNombre().toUpperCase().concat(" " + usuario.getApellido().toUpperCase())) // Convierte
																														// Usuario->
																														// String
				.flatMap(nombre -> { // Filtra los bruce y devuelve un Observable(Mono, Flux)
					if (nombre.contains("bruce".toUpperCase())) {
						return Mono.just(nombre);
					} else {
						return Mono.empty();
					}
				}).map(nombre -> { // Convierte String Upper -> String Lower
					return nombre.toLowerCase();
				})
				// Suscribirse al observable Imprimir uno por uno
				.subscribe(user -> log.info(user.toString()));
	}

	public void observableFluxAMonoList() {
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Andres", "Guzman"));
		usuariosList.add(new Usuario("Pedro", "Fulano"));
		usuariosList.add(new Usuario("Maria", "Fulana"));
		usuariosList.add(new Usuario("Diego", "Sultano"));
		usuariosList.add(new Usuario("Juan", "Mengano"));
		usuariosList.add(new Usuario("Bruce", "Lee"));
		usuariosList.add(new Usuario("Bruce", "Willis"));

		// Flux es un publisher, un observable
		Flux.fromIterable(usuariosList)
				// Convierte a un Mono<List<Usuario>>
				.collectList()
				// Suscribirse al observable Imprimir toda la lista
				.subscribe(list -> log.info(list.toString()));
	}

	public Usuario crearUsuario() {
		return new Usuario("John", "Doe");
	}

	public void combinarDosFlujosConFlatMap() {
		// Crear un Mono Stream del tipo Usuario
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> crearUsuario());

		// Crear un Mono Stream del tipo Comentarios
		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola Pepe, que tal !");
			comentarios.addComentario("Mañana voy a la playa !");
			comentarios.addComentario("Estoy en el curso de Spring con Reactor");
			return comentarios;
		});
		
		//Emite un usuario
		usuarioMono.flatMap(usuario -> 
			// Emite los comentarios
			comentariosMono.map(comentarios -> new UsuarioComentarios(usuario, comentarios)))
		//Imprime la combinacion Usuario -> Comentarios
		.subscribe(uc -> log.info(uc.toString()));

	}
	
	
	public void combinarDosFlujosConZipWith() {
		// Crear un Mono Stream del tipo Usuario
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> crearUsuario());

		// Crear un Mono Stream del tipo Comentarios
		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola Pepe, que tal !");
			comentarios.addComentario("Mañana voy a la playa !");
			comentarios.addComentario("Estoy en el curso de Spring con Reactor");
			return comentarios;
		});
		
		//Emite un usuario
		usuarioMono
			// Combina Usuario -> Comentarios
			.zipWith(comentariosMono, (usuario, comentarios) -> new UsuarioComentarios(usuario, comentarios))
			// Imprime Usuario -> Comentarios
			.subscribe(uc -> log.info(uc.toString()));
	}
	
	public void combinarDosFlujosConZipWithForma2() {
		// Crear un Mono Stream del tipo Usuario
		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> crearUsuario());

		// Crear un Mono Stream del tipo Comentarios
		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola Pepe, que tal !");
			comentarios.addComentario("Mañana voy a la playa !");
			comentarios.addComentario("Estoy en el curso de Spring con Reactor");
			return comentarios;
		});
		
		//Emite un usuarioComentarios
		  Mono<UsuarioComentarios> usuarioComentarios = 
			// Combina Usuario -> Comentarios
				  usuarioMono.zipWith(comentariosMono)
				  .map(tuple -> {
					 Usuario u = tuple.getT1();
					 Comentarios c = tuple.getT2();
					 return new UsuarioComentarios(u, c);
				  });
			
		  // Imprime Usuario -> Comentarios
		  usuarioComentarios.subscribe(uc -> log.info(uc.toString()));
	}


	public void combinarDosFlujosConZipWithRangos() {
		Flux<Integer> rangos = Flux.range(0, 4); //0,1,2,3
		Flux.just(1,2,3,4)
			.map(i -> i*2)
			.zipWith(rangos, (uno, dos) -> String.format("Primer Flux: %d, Segundo Flux: %d", uno, dos))
			.subscribe(text -> log.info(text));
	}


	public void ejemploInterval() {
		Flux<Integer> rango = Flux.range(1, 12); //0,1,2,3
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1)); 

		rango.zipWith(retraso, (rang, retr)-> rang)
			.doOnNext(i -> log.info(i.toString()))
			.blockLast();  // Se suscribe bloqueando, hasta que se haya emitido el ultmo elemento, para ver la impresion.
	}
	
	public void ejemploDelayElements() {
		//0,1,2,3
		Flux<Integer> rango = Flux.range(1, 12)
								  .delayElements(Duration.ofSeconds(1))
								  .doOnNext(i -> log.info(i.toString()));
		rango.blockLast(); // Se suscribe bloqueando, hasta que se haya emitido el ultmo elemento, para ver la impresion.
	}
	
	public void ejemploIntervalInfinito() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);
		
		
		//Emite un valor Integer cada segundo.
		Flux.interval(Duration.ofSeconds(1))
		.doOnTerminate(()-> latch.countDown())  //despues de emitirlo lo decrementa
		.flatMap(i->{
			if(i >= 5 ) {  // Solo hasta 5
				return Flux.error(new InterruptedException("Solo hasta 5!"));
			}else {
				return Flux.just(i);
			}
		})
		.map(i-> "Hola "+i.intValue()) // Convierte integer a String
		.retry(1) // En caso de error reintenta n veces mas, todo el proceso de contar con delay del 0-4
		.subscribe(s-> log.info(s), err -> log.error(err.getMessage()));
		
		latch.await();   //Esperar hasta que contador llegue a 0
	}
	
	
	public void ejemploIntervalDesdeCreate() {
		Flux.create(emitter -> {
			//Contiene el valor que vamos a emitir en el flujo
			Timer timer =  new Timer();
			timer.schedule(new TimerTask() {
				private Integer contador = 0;
				@Override
				public void run() { // Cada vez que se ejecute el metodo run emite un valor cada segundo
					emitter.next(++contador);
					//Finalizar con exito
					if(contador == 10) {
						timer.cancel();
						emitter.complete();
					}
					// Finalizar con error
					if(contador == 5) {
						emitter.error(new InterruptedException("Error, se ha detenido el flux  en 5."));
						timer.cancel();
					}
					
				}}, 1000, 1000);  //Inicio y delay
		})
		//3 Args, 1.-Cuando suscribe elementos, 2. Cuando hay error, 3. Cuando termina
		.subscribe(valorEmitido -> log.info(valorEmitido.toString()), 
					error -> log.error(error.getMessage()), 
					() -> log.info("Terminado !") ); 
	}
	
	public void ejemploContraPresion() {
		Flux.range(1, 10)
			.log()
			.limitRate(5)
			.subscribe(/*new Subscriber<Integer>() {

				private Subscription s;
				private Integer limite = 5;
				private Integer consumido = 0;
				
				@Override
				public void onSubscribe(Subscription s) {
					this.s = s;
					s.request(limite);
				}

				@Override
				public void onNext(Integer t) {
					log.info(t.toString());
					consumido ++;
					if(consumido == limite) {
						consumido = 0;
						s.request(limite);
					}
				}

				@Override
				public void onError(Throwable t) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onComplete() {
					// TODO Auto-generated method stub
					
				}
			}*/);
	}
}
