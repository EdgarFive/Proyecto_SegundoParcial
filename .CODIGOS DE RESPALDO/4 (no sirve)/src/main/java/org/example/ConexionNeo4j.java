package org.example;

import org.neo4j.driver.*;

public class ConexionNeo4j implements AutoCloseable {
    private static ConexionNeo4j instance;
    private final Driver driver;
    
    private static final String URI = "bolt://localhost:7687";
    private static final String USER = "neo4j";
    private static final String PASSWORD = "neo4j123";

    private ConexionNeo4j() {
        driver = GraphDatabase.driver(URI, AuthTokens.basic(USER, PASSWORD));
    }

    public static ConexionNeo4j getInstance() {
        if (instance == null) {
            instance = new ConexionNeo4j();
        }
        return instance;
    }

    public Session getSession() {
        return driver.session();
    }

    @Override
    public void close() {
        if (driver != null) {
            driver.close();
        }
    }
}
