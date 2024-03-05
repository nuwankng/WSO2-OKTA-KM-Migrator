package model;

public class Application {
    private int id;
    private String name;
    private String clinetID;
    private String createdBy;
    private String keyManager;

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    private String accessToken;
    private String applicationInfo;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClinetID() {
        return clinetID;
    }

    public void setClinetID(String clinetID) {
        this.clinetID = clinetID;
    }

    public String getKeyManager() {
        return keyManager;
    }

    public void setKeyManager(String keyManager) {
        this.keyManager = keyManager;
    }

    public String getApplicationInfo() {
        return applicationInfo;
    }

    public void setApplicationInfo(String applicationInfo) {
        this.applicationInfo = applicationInfo;
    }
}
