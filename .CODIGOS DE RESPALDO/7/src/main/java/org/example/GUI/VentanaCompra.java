package org.example.GUI;

import org.example.ConexionNeo4j;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;  // Añadir esta importación
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class VentanaCompra extends JFrame {

    private String usuario;
    private JComboBox<String> comboProductos;
    private JTextArea areaRecomendaciones;
    private ConexionNeo4j conexion;

    public VentanaCompra(String usuario) {
        this.usuario = usuario;
        this.conexion = new ConexionNeo4j("bolt://localhost:7687", "neo4j", "neo4j123");

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
        try {
            System.out.println("Buscando recomendaciones para usuario: " + usuario);
            
            // Primero verificamos si el usuario tiene compras
            var comprasResult = conexion.getSession().run(
                "MATCH (u:Usuario {nombre: $usuario})-[:COMPRÓ]->(p:Producto) RETURN count(p) as compras",
                org.neo4j.driver.Values.parameters("usuario", usuario)
            );
            int compras = comprasResult.single().get("compras").asInt();
            System.out.println("El usuario tiene " + compras + " compras");
        
            // Verificamos las relaciones SIMILAR_A
            var similaresResult = conexion.getSession().run(
                "MATCH (p:Producto)-[s:SIMILAR_A]->(reco:Producto) RETURN count(s) as similares",
                org.neo4j.driver.Values.parameters()
            );
            int similares = similaresResult.single().get("similares").asInt();
            System.out.println("Existen " + similares + " relaciones SIMILAR_A en total");
        
            // Ahora ejecutamos la consulta original
            var result = conexion.getSession().run(
                "MATCH (u:Usuario {nombre: $usuario})-[:COMPRÓ]->(p:Producto)-[s:SIMILAR_A]->(reco:Producto) " +
                "WHERE NOT (u)-[:COMPRÓ]->(reco) " +
                "RETURN reco.nombre AS recomendado, p.nombre AS basado_en, s.peso AS peso " +
                "ORDER BY s.peso DESC LIMIT 5",
                org.neo4j.driver.Values.parameters("usuario", usuario)
            );
        
            areaRecomendaciones.append("Recomendaciones para " + usuario + ":\n\n");
            boolean hayRecomendaciones = false;
            
            // Crear un MouseListener para las recomendaciones
            MouseListener mouseListener = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        int lineNumber = areaRecomendaciones.getLineOfOffset(areaRecomendaciones.getCaretPosition());
                        String line = areaRecomendaciones.getText().split("\n")[lineNumber];
                        if (line.startsWith("-")) {
                            String productoRecomendado = line.substring(line.indexOf("-") + 2, line.indexOf(" (basado"));
                            comboProductos.setSelectedItem(productoRecomendado);
                        }
                    } catch (Exception ex) {
                        System.err.println("Error al seleccionar recomendación: " + ex.getMessage());
                    }
                }
            };
            
            areaRecomendaciones.addMouseListener(mouseListener);
            areaRecomendaciones.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            while (result.hasNext()) {
                hayRecomendaciones = true;
                var record = result.next();
                String recomendado = record.get("recomendado").asString();
                areaRecomendaciones.append("- " + recomendado +
                    " (basado en: " + record.get("basado_en").asString() +
                    ", peso: " + record.get("peso").asInt() + ")\n");
            }
        
            if (!hayRecomendaciones) {
                areaRecomendaciones.append("No hay recomendaciones disponibles.\n" +
                    "Esto puede deberse a:\n" +
                    "- No has realizado compras aún\n" +
                    "- No hay productos similares registrados\n" +
                    "- Ya has comprado todos los productos similares");
            }
        } catch (Exception e) {
            System.err.println("Error al obtener recomendaciones: " + e.getMessage());
            areaRecomendaciones.append("Error al obtener recomendaciones: " + e.getMessage());
        }
    }
}
