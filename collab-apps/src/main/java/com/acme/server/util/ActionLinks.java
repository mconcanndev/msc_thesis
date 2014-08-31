package com.acme.server.util;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 *  The ActionLinks Util class will be used to include the next set of valid action in response
 *  to any given REST request so that a full HATEOUS implementation can be put in place.
 *
 *  [ { "href": "https://api.sandbox.paypal.com/v1/payments/payment/PAY-6RV70583SB702805EKEYSZ6Y",
 *      "rel": "self",
 *      "method": "GET"
 *  }]
 *
 */
public class ActionLinks {

    private String localHOSTURL = "http://localhost:8080";
    private String href;
    private String rel;
    private String method;
    private static Logger log = Logger.getLogger(ActionLinks.class);

    public ActionLinks(){

    }

    public ActionLinks(String href, String rel, String method) {
        log.info("Entering ActionLinks constructor");
        this.href = localHOSTURL + href;
        this.rel = rel;
        this.method = method;

        log.info("Link: " + localHOSTURL + href);
    }

    public String getHref() {
        return href;
    }

    public String getRel() {
        return rel;
    }

    public String getMethod() {
        return method;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public static List<ActionLinks> getGlobalActions(String chatRoomID){
        List<ActionLinks> globalActionLinks = new ArrayList<ActionLinks>();

        if (chatRoomID != null){
            ActionLinks nextActionLink = new ActionLinks("/notifications/chatroomID" +chatRoomID,"Poll","GET");
            //ActionLinks nextActionLink = new ActionLinks("/notifications/" + chatRoomID + ,"Poll","GET");
        }

        return globalActionLinks;
    }
}
