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
def today = new Date()
def tz = TimeZone.getTimeZone("Asia/Calcutta")
String date = today.format("dd-MM-yyyy", tz)
String time = today.format("HH:mm", tz)
def session
String msg = ""
def districtMap = ["294": "BBMP", "265": "Bangalore Urban", "276": "Bangalore Rural"]
//def districtMap = ["294": "BBMP", "265": "Bangalore Urban", "276": "Bangalore Rural", "145" : "East Delhi"]

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
                    if (session.min_age_limit == 18 && session.available_capacity_dose1 != 0) {
                        msg = msg + "18+ vaccines: " + districtMap[districts[j]] + ": " + session.date + ": " + session.vaccine + ": capacity: " + session.available_capacity + " center: " + vaccination.centers[i].name + "\n"
                    }
                }
            }
        }
    }
}
message = "{\"text\":\"" + msg + "\"}"
if (msg.isEmpty()) {
    println(date + " " + time + ": no center found for date for next 7 days: ")
} else {
    println(date + " " + time + " : slot found ")
    PosttoSlack(message)
}

def PosttoSlack(String messageText) {
    def slackwebhook = new URL("https://hooks.slack.com/services/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").openConnection();
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