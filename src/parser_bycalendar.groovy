@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7.1')

import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import java.sql.Time

Object vaccination
String base = 'https://cdn-api.co-vin.in'
def vaccinationOutput = new RESTClient(base)
vaccinationOutput.contentType = ContentType.JSON
//def districts = ["294", "265", "276", "145"] //294: BBMP, 265: Bangalore Urban: 276: Bangalore Rural: 145: East Delhi
def districts = ["294", "265", "276"] //294: BBMP, 265: Bangalore Urban: 276: Bangalore Rural: 145: East Delhi
// state = "16" //Karnataka
// state = "9" //Delhi
def today, tz, session
String date, time
String msg = ""
def districtMap = ["294": "BBMP", "265": "Bangalore Urban", "276": "Bangalore Rural"]
//def districtMap = ["294": "BBMP", "265": "Bangalore Urban", "276": "Bangalore Rural", "145" : "East Delhi"]
String slackWebhook="https://hooks.slack.com/services/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"
def min_age = 45
def dose = 2
int sleepSeconds = 180

while (true) {
    msg = ""
    today = new Date(); tz = TimeZone.getTimeZone("Asia/Calcutta")
    date = today.format("dd-MM-yyyy", tz); time = today.format("HH:mm", tz)
    if (districts != null && districts.size() > 1) {
        for (int j = 0; j < districts.size(); j++) {
            def params = [district_id: districts[j], date: date]
            vaccinationOutput.get(path: '/api/v2/appointment/sessions/public/calendarByDistrict', query: params) { response, json ->
                //println response.status
                // println json
                vaccination = json
                for (int i = 0; i < vaccination.centers.size(); i++) {
                    for (int k = 0; k < vaccination.centers[i].sessions.size(); k++) {
                        session = vaccination.centers[i].sessions[k]
                        if (dose == 1)
                            dose_capacity = session.available_capacity_dose1
                        else
                            dose_capacity = session.available_capacity_dose2
                        if (session.min_age_limit == min_age && dose_capacity != 0) {
                            msg = msg + "For " + min_age + "+: " + districtMap[districts[j]] + ": " + session.date + ": " +
                                    session.vaccine + ": available dose" + dose + ": [" + dose_capacity + "] : " +
                                    vaccination.centers[i].name + ": " + vaccination.centers[i].pincode + "\n"
                        }
                    }
                }
            }
        }
    }
    message = "{\"text\":\"" + msg + "\"}"
    if (msg.isEmpty()) {
        println(date + " " + time + ": no center for " + min_age+ "dose: "+ dose +" next 7 days: ")
    } else {
        println(date + " " + time + " : slot found ")
        println(message)
        PosttoSlack(message, slackWebhook)
    }
    sleep(sleepSeconds*1000)
}
def PosttoSlack(String messageText, String webHook) {
    def slackwebhook = new URL(webHook).openConnection();
    slackwebhook.setRequestMethod("POST")
    slackwebhook.setDoOutput(true)
    slackwebhook.setRequestProperty("Content-Type", "application/json")
    slackwebhook.getOutputStream().write(messageText.getBytes("UTF-8"));
    def postRC = slackwebhook.getResponseCode();
    // println(postRC);
    if (postRC.equals(200)) {
        println("Post OK " + slackwebhook.getInputStream().getText());
    } else if (!postRC.equals(200)) {
        println("Post Not OK " + slackwebhook.getInputStream().getText());
    }
}