package org.example.GUI;

import org.example.ConexionNeo4j;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;  // Añadir esta importación

public class VentanaCompra extends JFrame {

    private String usuario;
    private JComboBox<String> comboProductos;
    private JTextArea areaRecomendaciones;
    private ConexionNeo4j conexion;

    public VentanaCompra(String usuario) {
        this.usuario = usuario;
        this.conexion = new ConexionNeo4j("bolt://localhost:7687", "usuario2", "neo4j5910");

        setTitle("Compra de productos - " + usuario);
        setSize(500, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new FlowLayout());
        setLocationRelativeTo(null);  // Centrar ventana

        // Agregar WindowListener para cerrar la conexión
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (conexion != null) {
                    conexion.close();
                }
            }
        });

        JLabel etiqueta = new JLabel("Selecciona un producto para comprar:");
        comboProductos = new JComboBox<>();

        // Cargar productos disponibles desde Neo4j
        List<String> productos = obtenerProductosDisponibles();
        System.out.println("Productos encontrados: " + productos.size());  // Debug
        
        if (productos.isEmpty()) {
            comboProductos.addItem("No hay productos disponibles");
        } else {
            for (String producto : productos) {
                comboProductos.addItem(producto);
                System.out.println("Agregando producto: " + producto);  // Debug
            }
        }

        JButton botonComprar = new JButton("Comprar");
        botonComprar.addActionListener(e -> {
            String productoSeleccionado = (String) comboProductos.getSelectedItem();
            realizarCompra(productoSeleccionado);
            mostrarRecomendaciones();
        });

        areaRecomendaciones = new JTextArea(10, 40);
        areaRecomendaciones.setEditable(false);
        JScrollPane scroll = new JScrollPane(areaRecomendaciones);

        add(etiqueta);
        add(comboProductos);
        add(botonComprar);
        add(scroll);

        setVisible(true);
    }

    private List<String> obtenerProductosDisponibles() {
        try {
            var result = conexion.getSession()
                    .run("MATCH (p:Producto) RETURN p.nombre",
                            org.neo4j.driver.Values.parameters("usuario", usuario));
            
            List<String> productos = result.list(record -> record.get("p.nombre").asString());
            System.out.println("Consulta ejecutada. Productos encontrados: " + productos.size());
            return productos;
        } catch (Exception e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void realizarCompra(String producto) {
        conexion.getSession().run(
                "MATCH (u:Usuario {nombre: $usuario}), (p:Producto {nombre: $producto}) " +
                        "CREATE (u)-[:COMPRÓ]->(p)",
                org.neo4j.driver.Values.parameters("usuario", usuario, "producto", producto)
        );
    }

    private void mostrarRecomendaciones() {
        areaRecomendaciones.setText("");
        var result = conexion.getSession().run(
                "MATCH (u:Usuario {nombre: $usuario})-[:COMPRÓ]->(p:Producto)-[s:SIMILAR_A]->(reco:Producto) " +
                        "WHERE NOT (u)-[:COMPRÓ]->(reco) " +
                        "RETURN reco.nombre AS recomendado, p.nombre AS basado_en, s.peso AS peso " +
                        "ORDER BY s.peso DESC LIMIT 5",
                org.neo4j.driver.Values.parameters("usuario", usuario)
        );

        areaRecomendaciones.append("Recomendaciones para " + usuario + ":\n\n");
        while (result.hasNext()) {
            var record = result.next();
            areaRecomendaciones.append("- " + record.get("recomendado").asString() +
                    " (basado en: " + record.get("basado_en").asString() +
                    ", peso: " + record.get("peso").asInt() + ")\n");
        }
    }
}
