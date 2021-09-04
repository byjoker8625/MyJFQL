package org.jokergames.myjfql.util;

import org.jokergames.myjfql.core.MyJFQL;
import org.jokergames.myjfql.exception.NetworkException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Updater {

    private JSONObject serverConfiguration = null;
    private final String version;

    public Updater(String version) {
        this.version = version;
    }

    public void fetch(final String url) throws Exception {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(url).openStream(), StandardCharsets.UTF_8));
        final StringBuilder builder = new StringBuilder();

        int i;

        while ((i = reader.read()) != -1) {
            builder.append((char) i);
        }

        this.serverConfiguration = new JSONObject(builder.toString());
    }

    public VersionCompatibilityStatus getCompatibilityStatus() {
        final JSONObject compatibility = serverConfiguration.getJSONObject("compatibility");

        if (serverConfiguration.getString("version").equals(version))
            return VersionCompatibilityStatus.SAME;

        for (final String status : compatibility.keySet()) {
            final JSONObject compatibilityStatus = compatibility.getJSONObject(status);

            String newer = null;
            String older = null;

            if (!compatibilityStatus.isNull("newer"))
                newer = compatibilityStatus.getString("newer");

            if (!compatibilityStatus.isNull("older"))
                older = compatibilityStatus.getString("older");

            switch (VersionCompatibilityStatus.valueOf(status)) {
                case JUST_FINE: {
                    if (compareVersion(version, newer))
                        return VersionCompatibilityStatus.JUST_FINE;

                    break;
                }
                case SOME_CHANGES: {
                    if (compareVersion(version, newer) && !compareVersion(version, older))
                        return VersionCompatibilityStatus.SOME_CHANGES;

                    break;
                }
            }
        }

        return VersionCompatibilityStatus.PENSIONER;
    }

    private boolean compareVersion(final String versionA, final String versionB) {
        return versionA.compareTo(versionB) > 0;
    }

    public String getLatestVersion() {
        return serverConfiguration.getString("version");
    }

    public List<String> getVersions() {
        return new ArrayList<>(getDownloads().keySet());
    }

    public Map<String, String> getDownloads() {
        final Map<String, Object> raw = serverConfiguration.getJSONObject("downloads").toMap();
        return raw.keySet().stream().collect(Collectors.toMap(key -> key, key -> raw.get(key).toString(), (a, b) -> b));
    }

    public enum VersionCompatibilityStatus {
        PENSIONER,
        SOME_CHANGES,
        JUST_FINE,
        SAME
    }

    public JSONObject getServerConfiguration() {
        return serverConfiguration;
    }

    public static class Downloader {

        private final Updater updater;

        public Downloader(final Updater updater) {
            this.updater = updater;
        }

        public void downloadLatestVersion() {
            downloadByVersion(updater.getLatestVersion());
        }

        public void downloadByVersion(final String version) {
            downloadByURL(updater.getDownloads().get(version));
        }

        public void downloadByURL(final String download) {
            final int j = 1024;

            try {
                final URL url = new URL(download);
                final File file = new File("MyJFQL.jar");

                final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                final BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());
                final FileOutputStream fileOutputStream = new FileOutputStream(file);
                final BufferedOutputStream outputStream = new BufferedOutputStream(fileOutputStream, j);

                MyJFQL.getInstance().getConsole().logInfo("Starting download...");

                final byte[] bytes = new byte[j];
                int read;

                while ((read = inputStream.read(bytes, 0, j)) >= 0) {
                    outputStream.write(bytes, 0, read);
                }

                outputStream.close();
                inputStream.close();

                MyJFQL.getInstance().getConsole().logInfo("Download completed.");
                MyJFQL.getInstance().shutdown();
            } catch (Exception ex) {
                throw new NetworkException("Download failed!");
            }
        }

    }

}
