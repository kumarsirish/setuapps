import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient

/*
System.getProperties().put("proxySet", "true")
System.getProperties().put("http.proxyHost", "www-proxy-idc.in.oracle.com")
System.getProperties().put("https.proxyHost", "www-proxy-idc.in.oracle.com")
System.getProperties().put("http.proxyPort", "80")
System.getProperties().put("https.proxyPort", "80")
*/
@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7.1')

Object vaccination
String msg = "";
Boolean slotFound = false
String base = 'https://cdn-api.co-vin.in'
def vaccinationOutput = new RESTClient(base)
vaccinationOutput.contentType = ContentType.JSON
pincode = ["560037", "560066", "560007", "560008", "110091"]
def date = "15-05-2021"

String[] favouriteCenters = [
        "MANIPAL",
        "MARATHAHALLI",
        "YAMLURU",
        "COMMAND",
        "SIDDAPURA",
        "KODIHALLI UPHC",
        "APOLLO",
        "DGD MAYUR VIHAR"
]

for (int j = 0; j < pincode.size(); j++) {

    def params = [pincode: pincode[j], date: date]
    vaccinationOutput.get(path: '/api/v2/appointment/sessions/public/calendarByPin', query: params) { response, json ->
        //println response.status
        //println json
        vaccination = json

    }
//def jsonSlurper = new JsonSlurper()
//Object vaccination = jsonSlurper.parse(url)
//def vaccination = jsonSlurper.parse(new File("test_560037.json"))
    for (def i in vaccination.centers) {
        String available_slot = i.sessions.available_capacity
        String center = i.name
        if (CheckCenter(center.toUpperCase(), favouriteCenters) == true) {
            if (available_slot.equals("[0]") || available_slot.equals("[0, 0, 0, 0]")) {
                msg = msg + "pincode: " + pincode[j] + " : " + i.name + " : " + "No slots" + "\n"
            } else {
                msg = msg + "pincode: " + pincode[j] + " : " + i.name + " : " + available_slot + " slots" + "\n"
                slotFound = true
            }
        }

    }

}
message = "{\"text\":\"" + msg + "\"}"

if (msg.isEmpty()) {
    println("no center found for date: " + date)
} else if (slotFound) {
   // println(message)
    PosttoSlack(message)
}

def PosttoSlack(String messageText) {
    def slackwebhook = new URL("https://hooks.slack.com/services/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").openConnection();
    slackwebhook.setRequestMethod("POST")
    slackwebhook.setDoOutput(true)
    slackwebhook.setRequestProperty("Content-Type", "application/json")
    slackwebhook.getOutputStream().write(messageText.getBytes("UTF-8"));
    def postRC = slackwebhook.getResponseCode();
    println(postRC);
    if (postRC.equals(200)) {
        println(slackwebhook.getInputStream().getText());
    }
}

Boolean CheckCenter(String center, String[] favCenter) {
    available = false
    for (int i; i < favCenter.length; i++) {
        if (center.contains(favCenter[i])) {
            //  println(center+" "+ favCenter[i])
            available = true
        }

    }
    return available
}