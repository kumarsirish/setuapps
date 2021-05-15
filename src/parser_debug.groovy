import groovy.json.JsonSlurper
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient

/*
System.getProperties().setProperty("http.proxyHost", "www-proxy-idc.in.oracle.com")
System.getProperties().setProperty("http.proxyPort", "80")

println System.getProperties().getProperty("proxy")
println System.getProperties().getProperty("proxyHost")
println System.getProperties().getProperty("http.proxyHost")
*/
@Grab('org.codehaus.groovy.modules.http-builder:http-builder:0.7.1')

def tomorrow = new Date() + 1
String datePart = tomorrow.format("dd-MM-yyyy")

String base = 'http://api.icndb.com'
def chuck = new RESTClient(base)
def params = [firstName: "Dan", lastName: "Vega"]
chuck.contentType = ContentType.JSON

chuck.get( path: '/jokes/random', query: params) { response, json ->
    println response.status
    println json

}
System.exit(0)
String jsonString = '''{"menu": {
"id": "file",
"tools": {
"actions": [
{"id": "new", "title": "New File"},
{"id": "open", "title": "Open File"},
{"id": "close", "title": "Close File"}
],
"errors": []
}}}'''

def jsonSlurper = new JsonSlurper()
def parsedJson = jsonSlurper.parseText(jsonString)
println(parsedJson.menu.id)
def actionsArray = parsedJson.menu.tools.actions
for (def i : actionsArray) {
    if (i.id == 'open')
        println i.title
}


URL url = new URL("https://api.carbonintensity.org.uk/intensity")
Object result = jsonSlurper.parse(url)
String indexString = result.data.intensity.index
println(indexString)
if (indexString.equals("[moderate]")) {
    println(indexString + ": Moderate")
} else
    println(indexString + "Rough weather")

def fileContents = jsonSlurper.parse(new File("test_560037.json"))
/*for (def i : fileContents.centers) {
    println(i.name + ":" + i.sessions.available_capacity)
}*/

String msg="";
for (def i in fileContents.centers) {
    String available_slot = i.sessions.available_capacity
    String center = i.name
    if (center.contains("MANIPAL") || center.contains("APOLLO") || center.contains("Marathahalli") || center.contains("YAMLURU") || center.contains("COMMAND")) {
        if (available_slot.equals("[0]")) {
            msg = msg + "center: " + i.name + "  " + "sessions available: " + "No slots" + "\n"
        }
    }
}
message = "{\"text\":\"" + msg + "\"}"

if (msg.isEmpty()) {
    println("no center")
} else {
    println(message)
}
//Post
/*
def slackwebhook = new URL("https://hooks.slack.com/services/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").openConnection();
slackwebhook.setRequestMethod("POST")
slackwebhook.setDoOutput(true)
slackwebhook.setRequestProperty("Content-Type", "application/json")
slackwebhook.getOutputStream().write(message.getBytes("UTF-8"));
def postRC = slackwebhook.getResponseCode();
println(postRC);
if (postRC.equals(200)) {
    println(slackwebhook.getInputStream().getText());
}

 */
