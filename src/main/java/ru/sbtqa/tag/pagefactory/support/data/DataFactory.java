package ru.sbtqa.tag.pagefactory.support.data;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import ru.sbtqa.tag.datajack.TestDataProvider;
import ru.sbtqa.tag.datajack.exceptions.DataException;
import ru.sbtqa.tag.datajack.providers.ExcelDataProvider;
import ru.sbtqa.tag.datajack.providers.MongoDataProvider;
import ru.sbtqa.tag.datajack.providers.json.JsonDataProvider;
import ru.sbtqa.tag.datajack.providers.properties.PropertiesDataProvider;
import ru.sbtqa.tag.qautils.properties.Props;

public class DataFactory {

    private static TestDataProvider testDataProvider;
    private static String configCollection;

    public static TestDataProvider getDataProvider() throws DataException {
        if (testDataProvider == null) {
            configCollection = Props.get("data.initial.collection", null);
            String dataType = Props.get("data.type", "stash");

            switch (dataType) {
                case "json":
                    testDataProvider = new JsonDataProvider(
                            Props.get("data.folder"),
                            Props.get("data.initial.collection"),
                            Props.get("data.extension", "json")
                    );
                    break;
                case "properties":
                    testDataProvider = new PropertiesDataProvider(
                            Props.get("data.folder"),
                            Props.get("data.initial.collection"),
                            Props.get("data.extension", "properties")
                    );
                    break;
                case "excel":
                    testDataProvider = new ExcelDataProvider(
                            Props.get("data.folder"),
                            Props.get("data.initial.collection")
                    );
                    break;
                case "mongo":
                    testDataProvider = new MongoDataProvider(
                            new MongoClient(new MongoClientURI(Props.get("data.uri"))).getDB("data.db"),
                            Props.get("data.initial.collection")
                    );
                    break;
                default:
                    throw new DataException(String.format("Data adaptor %s isn't supported", dataType));
            }
        }
        return testDataProvider;
    }

    public static void updateCollection(TestDataProvider newObject) {
        testDataProvider = newObject;
    }

    public static String getConfigCollection() {
        return configCollection;
    }

}
