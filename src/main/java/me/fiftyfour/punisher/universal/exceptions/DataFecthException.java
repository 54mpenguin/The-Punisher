package me.fiftyfour.punisher.universal.exceptions;

public class DataFecthException extends Exception {

    private String reason, user, requestedInfo, causingClass;
    private Throwable cause;

    public DataFecthException(String reason, String user, String requestedInfo, String causingClass, Throwable cause){
        super(cause);
        this.reason = reason;
        this.user = user;
        this.requestedInfo = requestedInfo;
        this.causingClass = causingClass;
        this.cause = cause;
    }

    @Override
    public String getMessage() {
        return causingClass + ".class has failed to fetch " + requestedInfo + " on user " + user + ", this was caused by " + cause + ". Reason for data request: " + reason +
                ". Error message from exception cause: " + cause.getMessage();
    }
}
