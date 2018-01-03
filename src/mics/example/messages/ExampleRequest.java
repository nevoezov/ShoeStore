package mics.example.messages;

import mics.Request;

public class ExampleRequest implements Request<String>{

    private String senderName;

    public ExampleRequest(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderName() {
        return senderName;
    }
}
