package lk.mbpt.chatapp.shared;

import java.io.Serializable;

public class EchatMessage implements Serializable {
    private EchatHeaders header;
    private Object body;
    private static final long serialVersionUID = 1L;

    public EchatMessage() {
    }

    public EchatMessage(EchatHeaders header, Object body) {
        this.header = header;
        this.body = body;
    }

    public EchatHeaders getHeader() {
        return header;
    }

    public void setHeader(EchatHeaders header) {
        this.header = header;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}
