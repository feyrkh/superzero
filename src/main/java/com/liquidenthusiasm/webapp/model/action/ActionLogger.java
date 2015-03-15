package com.liquidenthusiasm.webapp.model.action;

import com.liquidenthusiasm.webapp.model.Actor;

import java.util.*;

public class ActionLogger {
    public static ActionLogger logger = new ActionLogger();
    public boolean actionDebuggingOn = true;

    ActionLogger() {
    }

    final Map<String, Long> actions = new HashMap<>();
    final Map<String, Map<String, Long>> actorActions = new HashMap<>();

    public void log(Actor actor, Action action) {
        log(actor, action, null);
    }

    public void log(Actor actor, Action action, ActionProvider provider) {
        log(actor, action, provider, "");
    }

    public void log(Actor actor, Action action, ActionProvider provider, String msgSuffix) {
        if (msgSuffix == null) {
            msgSuffix = "";
        }
        String actorName = actor.getName();
        String actionName = action.getName();
        Long count = actions.get(actionName);
        if (count == null) count = 1l;
        else count += 1;
        actions.put(actionName, count);

        Map<String, Long> actorAction = actorActions.get(actorName);
        if (actorAction == null) {
            actorAction = new HashMap<>();
            actorActions.put(actorName, actorAction);
        }
        count = actorAction.get(actionName);
        if (count == null) count = 1l;
        else count += 1;
        actorAction.put(actionName, count);
        if (actionDebuggingOn) {
            if (provider != null && provider != actor) {
                System.out.printf("ACTION: %s %s %s%s%n", actorName, actionName, provider.getName(), msgSuffix);
            } else {
                System.out.printf("ACTION: %s %s%s%n", actorName, actionName, msgSuffix);
            }
        }

    }

    public String actionReport() {
        StringBuilder sb = new StringBuilder();
        Map<String, Long> sorted = sortByValue(actions);
        for (Map.Entry<String, Long> entry : sorted.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    public String actorReport() {
        StringBuilder sb = new StringBuilder();
        Map<String, Map<String, Long>> sortedActors = sortByKey(actorActions);
        for (Map.Entry<String, Map<String, Long>> entry : sortedActors.entrySet()) {
            sb.append(entry.getKey()).append("\n");
            Map<String, Long> sortedActions = sortByValue(entry.getValue());
            for (Map.Entry<String, Long> actionEntry : sortedActions.entrySet()) {
                sb.append("   ").append(actionEntry.getKey()).append(": ").append(actionEntry.getValue()).append("\n");
            }
        }
        return sb.toString();
    }

    public void clear() {
        actions.clear();
        actorActions.clear();
    }

    public Map<String, Long> actionsBy(Actor actor) {
        return actorActions.get(actor.getName());
    }

    public static <K extends Comparable<? super K>, V> Map<K, V>
    sortByKey(Map<K, V> map) {
        List<Map.Entry<K, V>> list =
                new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getKey()).compareTo(o2.getKey());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list =
                new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public long actorActionCount(Actor actor, String actionName) {
        Map<String, Long> myActions = actorActions.get(actor.getName());
        if (myActions == null) {
            return 0l;
        }
        if (!myActions.containsKey(actionName)) {
            return 0l;
        }
        return myActions.get(actionName);
    }
}
