package de.byjoker.myjfql.server.session;

import de.byjoker.myjfql.database.Database;
import de.byjoker.myjfql.database.DatabaseService;
import de.byjoker.myjfql.user.User;
import de.byjoker.myjfql.user.UserService;

import java.util.Arrays;
import java.util.Objects;

public class Session {

    private String token;
    private String userId;
    private String databaseId;
    private String address;
    private long open;
    private long expire;

    public Session(String token, String userId, String databaseId, String address, long open, long expire) {
        this.token = token;
        this.userId = userId;
        this.databaseId = databaseId;
        this.address = address;
        this.open = open;
        this.expire = expire;
    }

    public Session(String token, String userId, String databaseId, String address, long expire) {
        this.token = token;
        this.userId = userId;
        this.databaseId = databaseId;
        this.address = address;
        this.open = System.currentTimeMillis();
        this.expire = expire;
    }

    public Session(String token, String userId, String databaseId, String address) {
        this.token = token;
        this.userId = userId;
        this.databaseId = databaseId;
        this.address = address;
        this.open = System.currentTimeMillis();
        utilize();
    }

    public boolean validAddress(String address) {
        if (this.address.contains(",")) {
            return Arrays.stream(this.address.split(",")).anyMatch(s -> validateAddress(s, address));
        }

        return validateAddress(this.address, address);
    }

    private boolean validateAddress(String addr, String address) {
        if (addr.endsWith("*") && !addr.startsWith("*"))
            return address.startsWith(addr.replace("*", ""));

        return addr.equals("*") || addr.equals(address);
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    public long getOpen() {
        return open;
    }

    public void setOpen(long open) {
        this.open = open;
    }

    public boolean isExpired() {
        if (expire == -1)
            return false;

        return expire <= System.currentTimeMillis();
    }

    public void utilize() {
        if (expire == -1)
            return;

        expire = System.currentTimeMillis() + 60000 * 60 * 24 * 7;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Database getDatabase(DatabaseService service) {
        return service.getDatabase(databaseId);
    }

    public User getUser(UserService service) {
        return service.getUser(userId);
    }

    public String getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(String databaseId) {
        this.databaseId = databaseId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return Objects.equals(token, session.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }

    @Override
    public String toString() {
        return "Session{" +
                "token='" + token + '\'' +
                ", userId='" + userId + '\'' +
                ", databaseId='" + databaseId + '\'' +
                ", address='" + address + '\'' +
                ", open=" + open +
                ", expire=" + expire +
                '}';
    }
}
