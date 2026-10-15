package com.votingbooth.representative.job.utils;

import com.votingbooth.representative.job.model.amo.Acteur;
import com.votingbooth.representative.job.model.amo.Deport;
import com.votingbooth.representative.job.model.amo.Organe;
import org.springframework.stereotype.Component;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;

@Component
public class AmoParser {

    private final JsonMapper mapper = JsonMapper.builder()
            // The XML→JSON conversion turns a one-element list into a plain object.
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    public Acteur parseActeur(byte[] json) {
        return parse(json, "acteur", Acteur.class);
    }

    public Organe parseOrgane(byte[] json) {
        return parse(json, "organe", Organe.class);
    }

    public Deport parseDeport(byte[] json) {
        return parse(json, "deport", Deport.class);
    }

    private <T> T parse(byte[] json, String root, Class<T> type) {
        JsonNode node = mapper.readTree(json).get(root);
        if (node == null || !node.isObject()) {
            throw new IllegalArgumentException("Not an AMO " + root + " file");
        }
        return mapper.treeToValue(normalize(node), type);
    }

    // Undoes the other XML→JSON quirks before binding: {"@xsi:nil": "true"} is an empty value,
    // and {"#text": ...} is an element that had XML attributes (e.g. acteur.uid), whose value is the text.
    private JsonNode normalize(JsonNode node) {
        if (node.isArray()) {
            ArrayNode array = mapper.createArrayNode();
            for (JsonNode element : node.values()) {
                array.add(normalize(element));
            }
            return array;
        }
        if (!node.isObject()) {
            return node;
        }
        JsonNode nil = node.get("@xsi:nil");
        if (nil != null && "true".equals(nil.asString())) {
            return NullNode.getInstance();
        }
        JsonNode text = node.get("#text");
        if (text != null) {
            return text;
        }
        ObjectNode object = mapper.createObjectNode();
        for (String name : node.propertyNames()) {
            object.set(name, normalize(node.get(name)));
        }
        return object;
    }

}
