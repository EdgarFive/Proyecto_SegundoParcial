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
            if (realizarCompra(productoSeleccionado)) {
                mostrarRecomendaciones();
                actualizarProductosDisponibles();
            }
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
                    .run("MATCH (p:Producto) WHERE p.stock > 0 RETURN p.nombre",
                            org.neo4j.driver.Values.parameters());
            
            List<String> productos = result.list(record -> record.get("p.nombre").asString());
            System.out.println("Productos con stock encontrados: " + productos.size());
            return productos;
        } catch (Exception e) {
            System.err.println("Error al obtener productos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private boolean realizarCompra(String producto) {
        try {
            // Primero verificamos solo el stock
            var stockResult = conexion.getSession().run(
                "MATCH (p:Producto {nombre: $producto}) " +
                "RETURN p.stock as stock",
                org.neo4j.driver.Values.parameters("producto", producto)
            );

            int stockActual = stockResult.single().get("stock").asInt();

            if (stockActual <= 0) {
                JOptionPane.showMessageDialog(this,
                    "No hay stock disponible para este producto.",
                    "Error en la compra",
                    JOptionPane.WARNING_MESSAGE);
                return false;
            }

            // Si hay stock, realizamos la compra
            var result = conexion.getSession().run(
                "MATCH (u:Usuario {nombre: $usuario}), (p:Producto {nombre: $producto}) " +
                "WHERE p.stock > 0 " +
                "CREATE (u)-[:COMPRÓ]->(p) " +
                "SET p.stock = p.stock - 1 " +
                "RETURN count(*) as compraRealizada",
                org.neo4j.driver.Values.parameters("usuario", usuario, "producto", producto)
            );

            boolean compraExitosa = result.single().get("compraRealizada").asInt() > 0;

            if (!compraExitosa) {
                JOptionPane.showMessageDialog(this,
                    "Error al procesar la compra.",
                    "Error en la compra",
                    JOptionPane.ERROR_MESSAGE);
            }
            
            return compraExitosa;
        } catch (Exception e) {
            System.err.println("Error al realizar la compra: " + e.getMessage());
            return false;
        }
    }

    private void actualizarProductosDisponibles() {
        comboProductos.removeAllItems();
        List<String> productos = obtenerProductosDisponibles();
        
        if (productos.isEmpty()) {
            comboProductos.addItem("No hay productos disponibles");
        } else {
            for (String producto : productos) {
                comboProductos.addItem(producto);
            }
        }
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
            // Consulta modificada para mostrar solo los nombres
            var result = conexion.getSession().run(
                "MATCH (u:Usuario {nombre: $usuario})-[:COMPRÓ]->(p:Producto)-[s:SIMILAR_A]->(reco:Producto) " +
                "WHERE NOT (u)-[:COMPRÓ]->(reco) " +
                "RETURN reco.nombre AS recomendado " +
                "ORDER BY s.peso DESC LIMIT 5",
                org.neo4j.driver.Values.parameters("usuario", usuario)
            );
        
            areaRecomendaciones.append("Recomendaciones para " + usuario + ":\n\n");
            boolean hayRecomendaciones = false;
        
            // Crear un MouseListener simplificado
            MouseListener mouseListener = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        int lineNumber = areaRecomendaciones.getLineOfOffset(areaRecomendaciones.getCaretPosition());
                        String line = areaRecomendaciones.getText().split("\n")[lineNumber];
                        if (line.startsWith("-")) {
                            String productoRecomendado = line.substring(2).trim();
                            comboProductos.setSelectedItem(productoRecomendado);
                        }
                    } catch (Exception ex) {
                        System.err.println("Error al seleccionar recomendación: " + ex.getMessage());
                    }
                }
            };
        
            areaRecomendaciones.addMouseListener(mouseListener);
            areaRecomendaciones.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
            // Mostrar solo los nombres de productos recomendados
            while (result.hasNext()) {
                hayRecomendaciones = true;
                var record = result.next();
                String recomendado = record.get("recomendado").asString();
                areaRecomendaciones.append("- " + recomendado + "\n");
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
