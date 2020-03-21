package application;

import core.EntityType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.util.List;

public class DatabaseConnection {
    private static DatabaseConnection connection = null;
    private SessionFactory sessionFactory;

    private DatabaseConnection() {
        setUp();
    }

    public static DatabaseConnection getConnection() {
        if (connection == null)
            connection = new DatabaseConnection();
        return connection;
    }

    private void setUp() {
        // A SessionFactory is set up once for an application!
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure() // configures settings from hibernate.cfg.xml
                .build();
        try {
            sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
        } catch (Exception e) {
            // The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
            // so destroy it manually.
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    public void tearDown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    public void createNewEntities(List<Object> entities) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        for (Object o : entities)
            session.save(o);
        session.getTransaction().commit();
        session.close();
    }

    public void createNewEntity(Object entity) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.save(entity);
        session.getTransaction().commit();
        session.close();
    }

    public Session getSession() {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        return session;
    }

    public List<Object> getEntitiesOfType(EntityType type) {
        Session session = getSession();
        List<Object> entities = session.createQuery(String.format(" from %s", type.toString())).list();
        session.getTransaction().commit();
        session.close();
        return entities;
    }
}
