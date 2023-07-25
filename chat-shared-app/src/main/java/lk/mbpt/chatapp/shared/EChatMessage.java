package lk.mbpt.chatapp.shared;

import java.io.Serializable;

public class EChatMessage implements Serializable {
    private EChatHeaders header;
    private Object body;
    private static final long serialVersionUID = 1L;

    public EChatMessage() {
    }

    public EChatMessage(EChatHeaders header, Object body) {
        this.header = header;
        this.body = body;
    }

    public EChatHeaders getHeader() {
        return header;
    }

    public void setHeader(EChatHeaders header) {
        this.header = header;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}
