/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package security;

/**
 *
 * @author fazo
 */
public class Message {

    private String certificateHash, text;
    private boolean encrypted;

    public Message(String text, String certificateHash) {
        this.certificateHash = certificateHash;
        this.text = text;
        encrypted = true;
    }

}
