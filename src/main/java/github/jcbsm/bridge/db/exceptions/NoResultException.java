package github.jcbsm.bridge.db.exceptions;

public class NoResultException extends  Exception {
    public NoResultException(String field, String value) {
        super("No value " + value + " found in field " + field);
    }

}
