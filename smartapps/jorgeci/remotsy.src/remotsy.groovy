/**
 *  Remotsy
 *
 *  Copyright 2016 Jorge Cisneros
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

// Automatically generated. Make future change here.
definition(
    name: "Remotsy",
    namespace: "jorgeci",
    author: "jorgecis@gmail.com",
    description: "Manager the controls from Remotsy",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png",
    oauth: true,
    singleInstance: true)

preferences {
    page(name: "Credentials", title: "Remotsy", content: "authPage", install: false)
	page(name: "listDevices", title: "Remotsy Controls", content: "listDevices", install: false)  
}

mappings {
    path("/oauth/initialize") {action: [GET: "oauthInitUrl"]}
    path("/oauth/callback") {action: [GET: "callback"]}
}

private getVendorName() 	{ "Remotsy" }
private getVendorAuthPath()	{ "https://www.remotsy.com:8443/oauth/authorize?" }
private getVendorTokenPath(){ "https://www.remotsy.com:8443/oauth/token" }
private getClientId() 		{ "smartthings" } 
private getClientSecret() 	{ "kjgsdfslskudfgj" }
private getVendorIcon()		{ "https://www.remotsy.com/static/img/remotsy_o.png" }
private apiUrl() 			{ "https://remotsy.com/rest/" }


def authPage() {
	log.debug "In authPage"
    def description = null  
    if (state.vendorAccessToken == null) {   
        log.debug "About to create access token."
        createAccessToken()
        description = "Tap to enter Credentials."
        def redirectUrl = "https://graph.api.smartthings.com/oauth/initialize?appId=${app.id}&access_token=${state.accessToken}&apiServerUrl=${getApiServerUrl()}"
        return dynamicPage(name: "Credentials", title: "Authorize Connection", nextPage: "listDevices", uninstall: false, install:false) {
               section { href url:redirectUrl, style:"embedded", required:false, title:"Connect to ${getVendorName()}:", description:description }
        }
    } else {
        return dynamicPage(name: "Credentials", title: "Authorize Connection", nextPage: "listDevices", uninstall: true, install:false) {
               section { href url:redirectUrl, style:"embedded", required:false, title:"Connect to ${getVendorName()}:", description:description }
        }    }
}


def installed() {
	log.debug "Installed with settings: ${settings}"
	listDevices()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
    initialize()
	listDevices()
}

def listDevices()
{
	log.debug "In listDevices"

	def devices = getDeviceList()
	log.debug "Device List = ${devices}"
    log.debug "Settings List = ${settings}"

	dynamicPage(name: "listDevices", title: "Choose devices", install: true) {
		section("Devices") {
			input "devices", "enum", title: "Select Device(s)", required: false, multiple: true, options: devices
		}
	}
}



def initialize()
{
	log.debug "Initialized with settings: ${settings}"

	settings.devices.each {
		def deviceId = it
        log.info it
		state.deviceDataArr.each {
        
        }
    }
}
def getDeviceList()
{
	log.debug "In getDeviceList"

	def deviceList = [:]
	state.deviceDataArr = []

	apiPost("controls/list") { response ->
        response.data.data.controls.each() {
				deviceList["${it._id}"] = it.name
				state.deviceDataArr.push(['name'    : it.name,
										  'id'      : it._id,
                                          'iddev'   : it.iddev
										  
				])
			}
            
        
     }
     return deviceList

}

def oauthInitUrl() {
    state.oauthInitState = UUID.randomUUID().toString()
    def oauthParams = [
        response_type: "code",
        scope: "email",
        client_id: getClientId(),
        client_secret: getClientSecret(),
        state: state.oauthInitState,
        redirect_uri: "https://graph.api.smartthings.com/oauth/callback"
    ]
    redirect(location: getVendorAuthPath() + toQueryString(oauthParams))
}


def callback() {
    def oauthParams = [ client_id: getClientId(),
                        client_secret: getClientSecret(),
    				    grant_type: "authorization_code", 
                        code: params.code,
                        redirect_uri: "https://graph.api.smartthings.com/oauth/callback"]                        
	httpPost(getVendorTokenPath(), toQueryString(oauthParams)) { response -> 
    	state.vendorRefreshToken = response.data.refresh_token
        state.vendorAccessToken = response.data.access_token
	}
     
    if ( !state.vendorAccessToken ) {  
    	return
    }
          
    def html = """
        <!DOCTYPE html>
        <html>
        <head>
        <meta name=viewport content="width=300px, height=100%">
        <title>${getVendorName()} Connection</title>
        <style type="text/css">
            @font-face {
                font-family: 'Swiss 721 W01 Thin';
                src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.eot');
                src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.eot?#iefix') format('embedded-opentype'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.woff') format('woff'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.ttf') format('truetype'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-thin-webfont.svg#swis721_th_btthin') format('svg');
                font-weight: normal;
                font-style: normal;
            }
            @font-face {
                font-family: 'Swiss 721 W01 Light';
                src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.eot');
                src: url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.eot?#iefix') format('embedded-opentype'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.woff') format('woff'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.ttf') format('truetype'),
                     url('https://s3.amazonaws.com/smartapp-icons/Partner/fonts/swiss-721-light-webfont.svg#swis721_lt_btlight') format('svg');
                font-weight: normal;
                font-style: normal;
            }
            .container {
                width: 560px;
                padding: 0px;
                /*background: #eee;*/
                text-align: center;
            }
            img {
                vertical-align: middle;
            }
            img:nth-child(2) {
                margin: 0 30px;
            }
            p {
                font-size: 2.2em;
                font-family: 'Swiss 721 W01 Thin';
                text-align: center;
                color: #666666;
                padding: 0 40px;
                margin-bottom: 0;
            }
            span {
                font-family: 'Swiss 721 W01 Light';
            }
        </style>
        </head>
        <body>
            <div class="container">
                <img src=""" + getVendorIcon() + """ alt="Vendor icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/connected-device-icn%402x.png" alt="connected device icon" />
                <img src="https://s3.amazonaws.com/smartapp-icons/Partner/support/st-logo%402x.png" alt="SmartThings logo" />
                <p>We have located your """ + getVendorName() + """ account.</p>
                <p>Tap 'Done' to process your credentials.</p>
			</div>
        </body>
        </html>
        """
	render contentType: 'text/html', data: html
}


String toQueryString(Map m) {
        return m.collect { k, v -> "${k}=${URLEncoder.encode(v.toString())}" }.sort().join("&")
}


def checkToken() {
	log.debug "In checkToken"
    
    def tokenStatus = "bad"
    
    //check existing token by calling user device list
    try {
    	httpPost([ uri		: apiUrl(),
    		  	  path 		: "controls/list",
               	  headers 	: [ 'Authorization' : 'Bearer ' + state.vendorAccessToken ]
		])
		{ response ->
        	log.debug "Response is: ${response.status}"
            if ( response.status == 200 ) {
            	log.debug "The current token is good"
                tokenStatus = "good"
            }
		}
	}
    catch(Exception e) {
    	log.debug "Current access token did not work. Trying refresh Token now."
    }

	if ( tokenStatus == "bad" ) {
        //Let's try the refresh token now
    	log.debug "Trying to refresh tokens"
        
    	def tokenParams = [ client_id		: getClientId(),
        					client_secret	: getClientSecret(),
							grant_type		: "refresh_token",
							refresh_token	: state.vendorRefreshToken ]
        
    	try {
			httpPost(getVendorTokenPath(),toQueryString(tokenParams) ) { response ->
            	debugOut "Successfully refreshed tokens with code: ${response.status}"
            	state.vendorRefreshToken = response.data.refresh_token
            	state.vendorAccessToken = response.data.access_token
            	tokenStatus = "good"
        	}
		}
    	catch(Exception e) {
    		log.debug "Unable to refresh token. Error ${e}"
    	}
	}
    
    if ( tokenStatus == "bad" ) {
    	return "Error: Unable to refresh Token"
    } else {
    	return null //no errors
    }
}

def apiPost(String path, Closure callback)
{
	log.debug "In apiPost with path: $path"
    
    //check to see if our token has expired
    def status = checkToken()
    log.debug "Status of checktoken: ${status}"
    
    if ( status ) {
    	log.debug "Error! Status: ${status}"
        return
    } else {
    	log.debug "Token is good. Call the command"
    }
    
	httpPost([
		uri : apiUrl(),
		path : path,
		headers : [ 'Authorization' : 'Bearer ' + state.vendorAccessToken ]
	])
	{
		response ->
			callback.call(response)
	}
}