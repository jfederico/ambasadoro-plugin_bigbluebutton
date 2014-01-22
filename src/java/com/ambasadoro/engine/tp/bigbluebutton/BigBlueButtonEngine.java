package com.ambasadoro.engine.tp.bigbluebutton;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.HashMap;

import com.ambasadoro.Ambasadoro;
import com.ambasadoro.engine.EngineBase;
import com.ambasadoro.engine.VendorCodes;

import org.apache.log4j.Logger;
import org.bigbluebutton.api.BBBCommand;
import org.bigbluebutton.api.BBBException;
import org.bigbluebutton.api.BBBStore;
import org.bigbluebutton.api.BBBProxy;
import org.bigbluebutton.impl.BBBStoreImpl;
import org.bigbluebutton.impl.BBBCreateMeeting;
import org.lti.api.LTIRoles;
import org.apache.commons.codec.digest.DigestUtils;

public class BigBlueButtonEngine extends EngineBase{

    private static final Logger log = Logger.getLogger(BigBlueButtonEngine.class);

    public static final String TP_VENDOR_CODE = VendorCodes.TP_CODE_BIGBLUEBUTTON;
    public static final String TP_VENDOR_NAME = "BigBlueButton";
    public static final String TP_VENDOR_DESCRIPTION = "Open source web conferencing system for distance learning.";
    public static final String TP_VENDOR_URL = "http://www.bigbluebutton.org/";
    public static final String TP_VENDOR_CONTACT_EMAIL = "bigbluebutton-users@googlegroups.com";
    
    BBBStore bbbStore = BBBStoreImpl.getInstance();
    BBBProxy bbbProxy;

    public BigBlueButtonEngine(Ambasadoro ambasadoro, Map<String, String> params, String endpoint) throws Exception {
        super(ambasadoro, params, endpoint);
        bbbProxy = bbbStore.createProxy(ambasadoro.getTpEndpoint(), ambasadoro.getTpSecret());
    }

    public String getToolVendorCode(){
        return TP_VENDOR_CODE;
    }

    //Command implementation for SSO
    public String getSSOURL() throws Exception {
        String ssoURL;
        try{
            BBBCommand cmd = new BBBCreateMeeting(bbbProxy, meetingParams());
            cmd.execute();
            log.info("Meeting created");
            ssoURL = bbbProxy.getJoinURL(sessionParams());
            log.info("Joining [" + ssoURL + "]");
        } catch ( BBBException e){
            throw new Exception("Error executing SSO", e.getCause());
        }
        return ssoURL;
    }

    private Map<String, String> meetingParams(){
        Map<String, String> params = tp.getParameters();
        Map<String, String> meetingParams = new HashMap<String, String>();
        // Map ToolProvider parameters with Meeting parameters
        meetingParams.put("name", getValidatedMeetingName(params.get("resource_link_title")));
        meetingParams.put("meetingID", getValidatedMeetingId(params.get("resource_link_id"), params.get("oauth_consumer_key")));
        meetingParams.put("attendeePW", DigestUtils.shaHex("ap" + params.get("resource_link_id") + params.get("oauth_consumer_key")));
        meetingParams.put("moderatorPW", DigestUtils.shaHex("mp" + params.get("resource_link_id") + params.get("oauth_consumer_key")));
        try {
            meetingParams.put("welcome", URLEncoder.encode(params.containsKey("extra_welcome")? params.get("extra_welcome"): "Welcome to <b>" + params.get("name") + "</b>", "UTF-8") );
        } catch (UnsupportedEncodingException e) {
            log.debug("Error encoding meetingName: " + e.getMessage());
            meetingParams.put("welcome", "");
        }
        meetingParams.put("voiceBridge", params.containsKey("extra_voicebridge")? params.get("extra_voicebridge"): "0");
        if(params.containsKey("extra_recording")){
            meetingParams.put("record", Boolean.valueOf(params.get("extra_recording")).toString());
            try {
                meetingParams.put("welcome", meetingParams.get("welcome") + URLEncoder.encode("<br><br>This meeting is being recorded", "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                log.debug("Error encoding meetingName: " + e.getMessage());
            }
        }
        if(params.containsKey("extra_duration")){
            meetingParams.put("duration", params.get("extra_duration"));
            try {
                meetingParams.put("welcome", meetingParams.get("welcome") + URLEncoder.encode("<br><br>The maximum duration for this meeting is ", "UTF-8") + params.get("extra_duration") + URLEncoder.encode(" minutes.", "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                log.debug("Error encoding meetingName: " + e.getMessage());
            }
        }
        if(params.containsKey("launch_presentation_return_url"))
            meetingParams.put("logoutURL", params.get("launch_presentation_return_url"));

        return meetingParams;
    }

    private Map<String, String> sessionParams(){
        Map<String, String> params = tp.getParameters();
        Map<String, String> sessionParams = new HashMap<String, String>();
        // Map LtiUser parameters with Session parameters
        sessionParams.put("fullName", getValidatedUserFullName(params));
        sessionParams.put("meetingID", getValidatedMeetingId(params.get("resource_link_id"), params.get("oauth_consumer_key")));
        if( LTIRoles.isStudent(params.get("roles")) || LTIRoles.isLearner(params.get("roles")) )
            sessionParams.put("password", DigestUtils.shaHex("ap" + params.get("resource_link_id") + params.get("oauth_consumer_key")));
        else
            sessionParams.put("password", DigestUtils.shaHex("mp" + params.get("resource_link_id") + params.get("oauth_consumer_key")));
        ////sessionParams.put("createTime", "");
        sessionParams.put("userID", DigestUtils.shaHex( params.get("user_id") + params.get("oauth_consumer_key")));

        return sessionParams;
    }

    private String getValidatedMeetingName(String _meetingName){
        String meetingName;
        meetingName = (_meetingName == null || _meetingName == "")? "Meeting": _meetingName; 
        try {
            meetingName = URLEncoder.encode(meetingName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.debug("Error encoding meetingName: " + e.getMessage());
            meetingName = "Meeting";
        }
        return meetingName;
    }

    private String getValidatedMeetingId(String resourceId, String consumerId){
        return DigestUtils.shaHex(resourceId + consumerId);
    }

    private String getValidatedLogoutURL(String logoutURL){
        return (logoutURL == null)? "": logoutURL;
    }

    private String getValidatedUserFullName(Map<String, String> params){
        String userFullName = params.get("lis_person_name_full");
        String userFirstName = params.get("lis_person_name_given");
        String userLastName = params.get("lis_person_name_family");
        if( userFullName == null || userFullName == "" ){
            if( userFirstName != null && userFirstName != "" ){
                userFullName = userFirstName;
            }
            if( userLastName != null && userLastName != "" ){
                userFullName += userFullName.length() > 0? " ": "";
                userFullName += userLastName;
            }
            if( userFullName == null || userFullName == "" ){
                userFullName = ( LTIRoles.isStudent(params.get("roles"), true) || LTIRoles.isLearner(params.get("roles"), true) )? "Viewer" : "Moderator";
            }
        }
        try {
            userFullName = URLEncoder.encode(userFullName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.debug("Error encoding userFullName: " + e.getMessage());
            userFullName = "User";
        }
        return userFullName;
    }
}
