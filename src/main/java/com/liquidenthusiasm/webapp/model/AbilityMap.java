package com.liquidenthusiasm.webapp.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.logging.log4j.util.Strings;

import java.util.*;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class AbilityMap {
    Map<String, Map<String, Integer>> relationMap = new HashMap<>();
    @JsonIgnore
    Set<String> abilityList = new HashSet<>();

    public void addRelationship(String ability1, String ability2, int relativePower) {
        relationship(ability1, ability2, relativePower);
    }

    private void relationship(String ability1, String ability2, int relativePower) {
        if (Strings.isBlank(ability1) || Strings.isBlank(ability2)) {
            throw new IllegalArgumentException(String.format("Relationships may not involve null/blank relationships: %s vs %s", ability1, ability2));
        }
        if (ability1.compareTo(ability2) > 0) {
            String tmp = ability1;
            ability1 = ability2;
            ability2 = tmp;
        }
        if (!relationMap.containsKey(ability1)) {
            relationMap.put(ability1, new HashMap<String, Integer>());
        }
        relationMap.get(ability1).put(ability2, relativePower);
        abilityList.add(ability1);
        abilityList.add(ability2);
    }

    @JsonIgnore
    public Collection<String> getAbilityList() {
        return abilityList;
    }

    public int getRelationship(String ability1, String ability2) {
        int multiplier = 1;
        if (ability1 == null || ability2 == null) {
            return 0;
        }
        if (ability1.compareTo(ability2) > 0) {
            String tmp = ability1;
            ability1 = ability2;
            ability2 = tmp;
            multiplier = -1;
        }
        if (!relationMap.containsKey(ability1)) {
            return 0;
        }
        Map<String, Integer> ability1Map = relationMap.get(ability1);
        if (!ability1Map.containsKey(ability2)) {
            return 0;
        }
        return ability1Map.get(ability2) * multiplier;
    }
}
