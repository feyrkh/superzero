package com.liquidenthusiasm.webapp.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AbilityMapTest {
    AbilityMap map;
    String ability1 = "fire";
    String ability2 = "water";
    private int someStrength;

    @Before
    public void setUp() throws Exception {
        map = new AbilityMap();
        someStrength = (int) (Math.random() * 200 - 100);
        if (someStrength == 0) {
            someStrength = 1;
        }
        map.addRelationship(ability1, ability2, someStrength);
    }

    @Test
    public void canSerializeAndDeserialize() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String mapContents = mapper.writeValueAsString(map);
        System.out.println("Ability map: " + mapContents);
        AbilityMap deserialized = mapper.readValue(mapContents, AbilityMap.class);
        assertNotNull("deserialized ability map", deserialized);
        assertEquals("value of " + ability1 + " vs " + ability2 + " after deserialize", someStrength, map.getRelationship(ability1, ability2));
    }

    @Test
    public void addingOneAbilityPairGivesEntriesForBoth() {
        map = new AbilityMap();
        assertEquals("number of abilities with relations before add", 0, map.getAbilityList().size());
        map.addRelationship(ability1, ability2, someStrength);
        assertEquals("number of abilities with relations", 2, map.getAbilityList().size());
    }

    @Test
    public void addingOneAbilityPairInReverseOrderGivesEntriesForBoth() {
        map = new AbilityMap();
        assertEquals("number of abilities with relations before add", 0, map.getAbilityList().size());
        map.addRelationship(ability2, ability1, someStrength);
        assertEquals("number of abilities with relations", 2, map.getAbilityList().size());
    }

    @Test
    public void abilityRelationsAreInverted() {
        assertEquals("1 vs 2 with str=" + someStrength, someStrength, map.getRelationship(ability1, ability2));
        assertEquals("2 vs 1 with str=" + someStrength, -someStrength, map.getRelationship(ability2, ability1));
    }

    @Test
    public void nullAbilityReturns0Strength() {
        assertEquals("1 vs null", 0, map.getRelationship(ability1, null));
        assertEquals("null vs 1", 0, map.getRelationship(null, ability1));
    }

    @Test
    public void unknownAbilityReturns0Strength() {
        assertEquals("1 vs unknown", 0, map.getRelationship(ability1, "ze unknown"));
        assertEquals("unknown vs 1", 0, map.getRelationship("an unknown", ability1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void notAllowedToInsertNullAsFirst() {
        map.addRelationship(null, ability1, someStrength);
    }

    @Test(expected = IllegalArgumentException.class)
    public void notAllowedToInsertEmptyAsFirst() {
        map.addRelationship("", ability1, someStrength);
    }

    @Test(expected = IllegalArgumentException.class)
    public void notAllowedToInsertBlankAsFirst() {
        map.addRelationship(" ", ability1, someStrength);
    }

    @Test(expected = IllegalArgumentException.class)
    public void notAllowedToInsertNullAsSecond() {
        map.addRelationship(ability1, null, someStrength);
    }

    @Test(expected = IllegalArgumentException.class)
    public void notAllowedToInsertEmptyAsSecond() {
        map.addRelationship(ability1, "", someStrength);
    }

    @Test(expected = IllegalArgumentException.class)
    public void notAllowedToInsertBlankAsSecond() {
        map.addRelationship(ability1, " ", someStrength);
    }
}