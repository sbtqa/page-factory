package ru.sbtqa.tag.pagefactory.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import ru.sbtqa.tag.pagefactory.PageFactory;
import static ru.sbtqa.tag.pagefactory.PageFactory.getBrowserName;
import ru.sbtqa.tag.qautils.properties.Props;

public class DesiredCapabilitiesParser {

    /**
     * Parses desired capabilities from config
     *
     * @return built capabilities
     */
    public DesiredCapabilities parse() {
        DesiredCapabilities capabilities = new DesiredCapabilities();

        capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.IGNORE);

        final String capsPrefix = "webdriver." + PageFactory.getBrowserName().toLowerCase() + ".capability.";
        Set<String> propKeys = Props.getProps().stringPropertyNames();

        List<String> capabilitiesFromProps = propKeys.stream().filter(prop -> prop.startsWith(capsPrefix))
                .collect(Collectors.toList());

        Map<String, Object> options = new HashMap<>();
        capabilitiesFromProps.forEach(rawCapabilityKey -> {
            String capability = rawCapabilityKey.substring(capsPrefix.length());

            if (capability.startsWith("options") && "Chrome".equals(getBrowserName())) {
                // For Chrome options must be parsed and specified as a data structure.
                // For non-chrome browsers options could be passed as string
                String optionsCapability = capability.substring("options.".length());
                switch (optionsCapability) {
                    case "args":
                    case "extensions":
                    case "excludeSwitches":
                    case "windowTypes":
                        String[] arrayOfStrings = Props.get(rawCapabilityKey).split(",");
                        List<String> listOfStrings = new ArrayList<>();
                        Arrays.stream(arrayOfStrings).forEach(item -> listOfStrings.add(item.trim()));
                        if (!listOfStrings.isEmpty()) {
                            options.put(optionsCapability, listOfStrings.toArray());
                        }
                        break;
                    case "prefs":
                    case "mobileEmulation":
                    case "perfLoggingPrefs":
                        Map<String, Object> dictionary = new HashMap<>();
                        String[] dictRows = Props.get(rawCapabilityKey).split(",");
                        Arrays.stream(dictRows).forEach(row -> {
                            String[] keyVal = row.split("=>");
                            dictionary.put(keyVal[0], keyVal[1].trim());
                        });
                        if (!dictionary.isEmpty()) {
                            options.put(optionsCapability, dictionary);
                        }
                        break;
                    default:
                        options.put(optionsCapability, Props.get(rawCapabilityKey));
                        break;
                }
                if (!options.isEmpty()) {
                    capabilities.setCapability(ChromeOptions.CAPABILITY, options);
                }
            } else {
                capabilities.setCapability(capability, Props.get(rawCapabilityKey));
            }
        });
        return capabilities;
    }
}
