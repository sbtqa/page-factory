package ru.sbtqa.tag.pagefactory.support;

import ru.sbtqa.tag.datajack.TestDataObject;
import ru.sbtqa.tag.datajack.adaptors.JsonDataObjectAdaptor;
import ru.sbtqa.tag.datajack.exceptions.DataException;
import ru.sbtqa.tag.qautils.properties.Props;

/**
 *
 * @author sbt-sidochenko-vv
 */
public class DataProvider {

    private static TestDataObject dataContainer;

    public static TestDataObject getInstance() throws DataException {
        if (dataContainer == null) {
            String dataType = Props.get("data.type", "stash");
            switch (dataType) {
                case "json":
                    dataContainer = new JsonDataObjectAdaptor(Props.get("data.folder"), Props.get("data.initial.collection"));
                    break;
                default:
                    throw new DataException(String.format("Data adaptor %s isn't supported", dataType));
            }
        }
        return dataContainer;
    }

    public static void updateCollection(TestDataObject newObject) {
        dataContainer = newObject;
    }

}
