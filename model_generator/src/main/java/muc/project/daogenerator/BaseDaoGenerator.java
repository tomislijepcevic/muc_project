package muc.project.daogenerator;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;
import de.greenrobot.daogenerator.ToMany;

public class BaseDaoGenerator {


    public static void main(String[] args) {
        Schema schema = new Schema(1, "muc.project.model");

        addEntities(schema);

        try {
            new DaoGenerator().generateAll(schema, "./app/src/main/java/");
        } catch (Exception e) {
            System.out.println("Could not generate model: " + e.getMessage());
        }
    }


    private static void addEntities(Schema schema){
        // Entities
        Entity client = schema.addEntity("Client");
        Entity history = schema.addEntity("History");
        Entity accessPoint = schema.addEntity("AccessPoint");
        Entity clientAccessPoint = schema.addEntity("Client_AccessPoint");
        clientAccessPoint.setTableName("CLIENT_AP");

        // Client
        client.addIdProperty();
        client.addStringProperty("mac").notNull().index();
        client.addStringProperty("manufacturer");
        client.addBooleanProperty("subscribed");
        client.addStringProperty("name");
        client.addIntProperty("counter").notNull();

        // History
        history.addIdProperty();
        Property timestamp = history.addDateProperty("timestamp").getProperty();
        history.addFloatProperty("lat");
        history.addFloatProperty("lng");
        Property clientId = history.addLongProperty("clientId").getProperty();
        history.addToOne(client, clientId);

        ToMany clientToHistory = client.addToMany(history, clientId);
        clientToHistory.setName("history");
        clientToHistory.orderAsc(timestamp);

        // AccessPoint
        accessPoint.addIdProperty();
        accessPoint.addStringProperty("mac").notNull().index();

        // Client_AccessPoint
        clientAccessPoint.addIdProperty();
        Property clientId2 = clientAccessPoint.addLongProperty("clientId").getProperty();
        clientAccessPoint.addToOne(client, clientId2);
        Property accessPointId = clientAccessPoint.addLongProperty("accessPointId").getProperty();
        clientAccessPoint.addToOne(accessPoint, accessPointId);

        ToMany clientToCAP = client.addToMany(clientAccessPoint, clientId2);
        clientToCAP.setName("clientAccessPoint");

        ToMany accessPointToCAP = accessPoint.addToMany(clientAccessPoint, accessPointId);
        accessPointToCAP.setName("clientAccessPoint");
    }

}