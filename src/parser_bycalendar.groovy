@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7.1')

import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import groovy.transform.Field
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
def districtMap = ["294": "BBMP", "265": "Bangalore Urban", "276": "Bangalore Rural"]
//def districtMap = ["294": "BBMP", "265": "Bangalore Urban", "276": "Bangalore Rural", "145" : "East Delhi"]
@Field int[] preferredPIN = [560037, 560066, 560103, 560034, 560035]
def slackWebhook = System.getenv('SLACK_WEBHOOK') ?: 'none'
@Field def min_age = 45
@Field def doseType = 2
@Field String vaccine = "COVISHIELD"
@Field String fee_type = "Free" //not used currently in comparison
@Field int minimum_available_dose = 2
int sleepSeconds = 1800 //30 minutes

int chimeFreq = 4 // every 2 hours
int sendAliveMsg = chimeFreq * (3600 / sleepSeconds)
int iterationCount = 100 // Set to higher value so that chime gets tested. It will reset to 0 in the loop below which is fine.
String hourlyChime
def msgList = []
def mapCounter = 0

while (true) {
    msg = ""
    hourlyChime = ""
    iterationCount++
    today = new Date(); tz = TimeZone.getTimeZone("Asia/Calcutta")
    date = today.format("dd-MM", tz); time = today.format("HH:mm", tz)
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
                        if (doseType == 1)
                            dose_capacity = session.available_capacity_dose1
                        else
                            dose_capacity = session.available_capacity_dose2
                        if (CheckConditions(session.min_age_limit,
                                dose_capacity,
                                session.vaccine,
                                vaccination.centers[i].fee_type,
                                vaccination.centers[i].pincode)) {
                            String msgText = vaccination.centers[i].fee_type.toUpperCase()+" [" + dose_capacity + "] " +
                                    vaccination.centers[i].name + " " + vaccination.centers[i].pincode + "\n"
                            msgList.add(mapCounter++,[session.date,msgText]) //append to msg list, it will used later for sorting on date
                        }
                        if (iterationCount >= sendAliveMsg) {
                            iterationCount = 0
                            hourlyChime = "sending " + chimeFreq+ " hourly chime. Poll Freq is " + sleepSeconds / 60 + " minutes"
                        }
                    }
                }
            }
        }
    }
    def header = " " + min_age + "+: " + "Dose-" + doseType +" "
    timeStamp = date + " " + time + ": "

    message = "{\"text\":\"" + "For " + header + "\n" + msgList.sort{it[0]}.toString() + "\"}" //sort on first field
    if (msgList.size() <1) {
        println(timeStamp + "No center for next 7 days for" + header)
    } else {
        println(timeStamp + " slot found ")
        //Check msg length. slack notifications are disabled if message length exceeds 4000 chars
        if (msgList.toString().length() < 3900) {
            if (!slackWebhook.equalsIgnoreCase("NONE")) {
                PosttoSlack(message, slackWebhook)
            } else {
                println("Can't post to webhook since its set to NONE")
            }
        }
    }
    if (!hourlyChime.isEmpty()) {
        PosttoSlack("{\"text\":\"" + timeStamp+ "For " + header + " " + hourlyChime + "\"}", slackWebhook)
    }
    sleep(sleepSeconds * 1000)
    //Reset the list
    msgList=[]
    mapCounter=0
}

def PosttoSlack(String messageText, def webHook) {
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

boolean CheckConditions(int age, int doseCapacity, String vaccineName, String feeType, int pinCode) {
    if (age == min_age &&
            doseCapacity >= minimum_available_dose &&
            vaccineName.equalsIgnoreCase(vaccine) &&
            //feeType.equalsIgnoreCase(fee_type) &&
            preferredPIN.contains(pinCode)) {
        return true
    }
    return false
}