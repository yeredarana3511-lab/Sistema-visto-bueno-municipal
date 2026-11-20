package com.proyecto.vistobueno.model;

import com.proyecto.vistobueno.config.JsonListConverter;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String clave;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Convert(converter = JsonListConverter.class)
    @Column(name = "permisos_json", columnDefinition = "json")
    private List<String> permisos = new ArrayList<>();

    public Rol() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public List<String> getPermisos() { return permisos; }
    public void setPermisos(List<String> permisos) { this.permisos = permisos; }
}
