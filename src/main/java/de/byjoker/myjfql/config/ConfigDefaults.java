package de.byjoker.myjfql.config;

import org.json.JSONObject;

public class ConfigDefaults implements Config {
    @Override
    public int port() {
        return 2291;
    }

    @Override
    public boolean server() {
        return true;
    }

    @Override
    public boolean updates() {
        return true;
    }

    @Override
    public boolean autoUpdate() {
        return false;
    }

    @Override
    public String updateHost() {
        return "https://byjoker.de/dl/json/myjfql.json";
    }

    @Override
    public String encryption() {
        return "NONE";
    }

    @Override
    public boolean jline() {
        return true;
    }

    @Override
    public boolean showConnections() {
        return true;
    }

    @Override
    public boolean showQueries() {
        return true;
    }

    @Override
    public JSONObject asJson() {
        return new JSONObject()
                .put("server", new JSONObject().put("enabled", true).put("port", 2291))
                .put("security", new JSONObject().put("encryption", "NONE").put("jline", true).put("showConnections", true).put("showQueries", true))
                .put("updater", new JSONObject().put("enabled", true).put("autoUpdate", false).put("host", "https://byjoker.de/dl/json/myjfql.json"));
    }
}