package com.elhg.springboot.webflux.app.models.documents;

import javax.validation.constraints.NotEmpty;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "categorias")
public class Categoria {

	@Id
	@NotEmpty
	private String id;
	private String nombre;

	public Categoria(String nombre) {
		this.nombre = nombre;
	}

	public Categoria() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	@Override
	public String toString() {
		return "Categoria [nombre=" + nombre + "]";
	}

}
