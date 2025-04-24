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

        setTitle("Sistema de Recomendación - " + usuario);
        setSize(600, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel principal con color de fondo oscuro
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(40, 40, 40));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Panel superior para la selección de productos
        JPanel selectionPanel = new JPanel();
        selectionPanel.setLayout(new BoxLayout(selectionPanel, BoxLayout.Y_AXIS));
        selectionPanel.setBackground(new Color(40, 40, 40));
        selectionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Título
        JLabel titleLabel = new JLabel("Selecciona un producto");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Estilizar el combo
        comboProductos = new JComboBox<>();
        comboProductos.setPreferredSize(new Dimension(400, 40));
        comboProductos.setMaximumSize(new Dimension(400, 40));
        comboProductos.setBackground(new Color(60, 60, 60));
        comboProductos.setForeground(Color.WHITE);
        comboProductos.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Cargar productos
        List<String> productos = obtenerProductosDisponibles();
        if (productos.isEmpty()) {
            comboProductos.addItem("No hay productos disponibles");
        } else {
            for (String producto : productos) {
                comboProductos.addItem(producto);
            }
        }

        // Botón de comprar
        JButton botonComprar = new JButton("Comprar");
        botonComprar.setPreferredSize(new Dimension(200, 40));
        botonComprar.setMaximumSize(new Dimension(200, 40));
        botonComprar.setBackground(new Color(255, 69, 69));
        botonComprar.setForeground(Color.WHITE);
        botonComprar.setBorderPainted(false);
        botonComprar.setFocusPainted(false);
        botonComprar.setFont(new Font("Arial", Font.BOLD, 14));
        botonComprar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botonComprar.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Área de recomendaciones
        JLabel recoLabel = new JLabel("Recomendaciones");
        recoLabel.setFont(new Font("Arial", Font.BOLD, 20));
        recoLabel.setForeground(Color.WHITE);
        recoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        areaRecomendaciones = new JTextArea(10, 40);
        areaRecomendaciones.setEditable(false);
        areaRecomendaciones.setBackground(new Color(60, 60, 60));
        areaRecomendaciones.setForeground(Color.WHITE);
        areaRecomendaciones.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(areaRecomendaciones);
        scroll.setPreferredSize(new Dimension(400, 200));
        scroll.setMaximumSize(new Dimension(400, 200));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));

        // Mantener el ActionListener existente
        botonComprar.addActionListener(e -> {
            String productoSeleccionado = (String) comboProductos.getSelectedItem();
            if (realizarCompra(productoSeleccionado)) {
                mostrarRecomendaciones();
                actualizarProductosDisponibles();
            }
        });

        // Agregar componentes al panel de selección
        selectionPanel.add(titleLabel);
        selectionPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        selectionPanel.add(comboProductos);
        selectionPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        selectionPanel.add(botonComprar);
        selectionPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        selectionPanel.add(recoLabel);
        selectionPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        selectionPanel.add(scroll);

        // Agregar el panel de selección al panel principal
        mainPanel.add(selectionPanel);

        // Agregar WindowListener existente
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (conexion != null) {
                    conexion.close();
                }
            }
        });

        // Agregar panel principal al frame
        add(mainPanel);
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
