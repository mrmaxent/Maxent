package density;

public class TerminalException extends RuntimeException {
    String msg;
    Exception e;
    public TerminalException(String s, Exception e) {
	msg = s;
	this.e = e;
    }
}
