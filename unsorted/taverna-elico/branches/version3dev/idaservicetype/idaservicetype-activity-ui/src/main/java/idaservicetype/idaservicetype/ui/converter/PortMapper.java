package idaservicetype.idaservicetype.ui.converter;

public class PortMapper {
    
	String fileName;
    String userName;
    String userProperty;

    public PortMapper(String producerName, String producerType, String userType, String producerProperty, String fileName, String userName, String userProperty) {
        this.producerName = producerName;
        this.producerType = producerType;
        this.userType = userType;
        this.producerProperty = producerProperty;
        this.fileName = fileName;
        this.userName = userName;
        this.userProperty = userProperty;
    }

    String producerName;
    String producerType;

    public String getProducerType() {
        return producerType;
    }

    public void setProducerType(String producerType) {
        this.producerType = producerType;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    String userType;
    String producerProperty;

    public String getProducerName() {
        return producerName;
    }

    public void setProducerName(String producerName) {
        this.producerName = producerName;
    }

    public String getProducerProperty() {
        return producerProperty;
    }

    public void setProducerProperty(String producerProperty) {
        this.producerProperty = producerProperty;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserProperty() {
        return userProperty;
    }

    public void setUserProperty(String userProperty) {
        this.userProperty = userProperty;
    }

}
