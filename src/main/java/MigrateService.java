import com.google.gson.Gson;
import com.mysql.cj.jdbc.BlobFromLocator;
import model.Application;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import utilities.DBConnection;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.swing.plaf.IconUIResource;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MigrateService {


    public static void main(String arg[]) {

        System.out.println("Application Migration Started");
        migrateApplicationData();
    }



    public static void migrateApplicationData() {

        System.out.println(System.getProperty("java.class.path"));


        Statement stmt;
        ResultSet results;
//        String sql_select = "select * from AM_APPLICATION_ATTRIBUTES where TENANT_ID = " + tenantID + " AND NAME = 'Registration Access Token';";
        String sql_select = "select *  from " +
                "dbo.AM_APPLICATION_KEY_MAPPING where KEY_MANAGER='2aaae1a8-6bdd-4a14-b513-fa59552a28d6' and CONSUMER_KEY IS NOT NULL and APP_INFO IS NULL;";

        try (Connection conn = DBConnection.createNewDBconnection()) {
            stmt = conn.createStatement();
            results = stmt.executeQuery(sql_select);
            System.out.println("Migrating of Applications");
            int count = 0;
            while (results.next()) {
//                System.out.println("Consumer ID : " + results.getInt(3));
                System.out.println("Consumer ID : " + results.getString("CONSUMER_KEY"));

//                String oktaInstanceUrl = configuration.getParameter(OktaConstants.OKTA_INSTANCE_URL);
//                String apiKey = configuration.getParameter(OktaConstants.REGISTRAION_API_KEY);
//                String registrationEndpoint = oktaInstanceUrl + OktaConstants.CLIENT_ENDPOINT;
                String clientId = "";
                clientId = results.getString("CONSUMER_KEY");
                String oktaInstanceUrl = "https://test.okta.com";
                String apiKey = "api-key";
                String registrationEndpoint = oktaInstanceUrl + "/oauth2/v1/clients";
                if (StringUtils.isNotEmpty(clientId)) {
                    registrationEndpoint += "/" + clientId;
                }

                OAuthApplicationInfo oAuthApplicationInfo=null;

                CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                BufferedReader reader = null;
                try {
                    HttpGet request = new HttpGet(registrationEndpoint);
                    // Set authorization header, with API key.
                    request.addHeader("Authorization", "SSWS " + apiKey);
                    HttpResponse response = httpClient.execute(request);
                    int statusCode = response.getStatusLine().getStatusCode();
                    HttpEntity entity = response.getEntity();
                    if (entity == null) {
                        handleException(String.format("%s %s",
                                "Could not read http entity for response", response));
                    }
                    reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
                    Object responseJSON;

                    if (statusCode == HttpStatus.SC_OK) {
                        JSONParser parser = new JSONParser();
                        responseJSON = parser.parse(reader);
                        //Since /oauth2/v1/clients/{clientId} does not send the client_secret, we do and update request
                        //with the same body we receive from GET request. This returns the client secret without changing
                        //any app related properties. (PUT /oauth2/v1/clients/{clientId})

                        String jsonPayload = ((JSONObject)responseJSON).toJSONString();
//                        if (log.isDebugEnabled()) {
//                            log.debug(String.format("Payload to update an OAuth client : %s for the Consumer Key %s", jsonPayload,
//                                    clientId));
//                        }
                        HttpPut httpPut = new HttpPut(registrationEndpoint);
                        httpPut.setEntity(new StringEntity(jsonPayload, "UTF-8"));
                        httpPut.setHeader("Content-Type", "application/json");
                        // Setting Authorization Header, with API Key.
                        httpPut.setHeader("Authorization", "SSWS " + apiKey);
//                        if (log.isDebugEnabled()) {
//                            log.debug(String.format("Invoking HTTP request to update client in Okta for Consumer Key %s", clientId));
//                        }
                        response = httpClient.execute(httpPut);
                        statusCode = response.getStatusLine().getStatusCode();
                        entity = response.getEntity();
                        if (entity == null) {
                            handleException(String.format("%s %s",
                                    "Could not read http entity for response", response));
                        }
                        reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
                        JSONObject responseObject = getParsedObjectByReader(reader);
                        if (statusCode == HttpStatus.SC_OK) {
                            if (responseObject != null) {
                            oAuthApplicationInfo=createOAuthAppInfoFromResponse(responseObject);
                            } else {
                                handleException("ResponseObject is empty. Can not return oAuthApplicationInfo.");
                            }
                        } else {
                            handleException(String.format("Error occured when updating the Client with Consumer Key %s" +
                                    " : Response: %s", clientId, responseObject.toJSONString()));
                        }
                    } else {
                        System.out.println("error");


                    }

                //updating the database
                System.out.println("\t INFO : Applying the migration to the database");
                PreparedStatement ps = conn.prepareStatement("UPDATE AM_APPLICATION_KEY_MAPPING SET APP_INFO = ? WHERE CONSUMER_KEY = ?");
                String content = new Gson().toJson(oAuthApplicationInfo);
                ps.setBinaryStream(1, new ByteArrayInputStream(content.getBytes()));
                ps.setString(2,clientId);
                ps.executeUpdate();
                System.out.println("\t INFO : Application Info migration completed.");
                System.out.println("________________________________________________________________________________");

            } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }


    } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    private static JSONObject getParsedObjectByReader(BufferedReader reader) throws ParseException, IOException {
        JSONObject parsedObject = null;
        JSONParser parser = new JSONParser();
        if (reader != null) {
            parsedObject = (JSONObject) parser.parse(reader);
        }
        return parsedObject;
    }

    private static OAuthApplicationInfo createOAuthAppInfoFromResponse(Map responseMap) {
        OAuthApplicationInfo appInfo = new OAuthApplicationInfo();
        String clientName = (String) responseMap.get("client_name");

        appInfo.setClientName(clientName);
        appInfo.setClientId((String) responseMap.get("client_id"));
        appInfo.setClientSecret((String) responseMap.get("client_secret"));
        JSONArray callbackUrl = (JSONArray) responseMap.get("redirect_uris");
        if (callbackUrl != null) {
            appInfo.setCallBackURL((String) callbackUrl.toArray()[0]);
        }
        Object clientIdIssuedAt = responseMap.get("client_id_issued_at");
        appInfo.addParameter("client_id_issued_at", clientIdIssuedAt);

        Object clientSecretExpiresAt = responseMap.get("client_secret_expires_at");
        appInfo.addParameter("client_secret_expires_at", clientSecretExpiresAt);

        Object clientUri = responseMap.get("client_uri");
        appInfo.addParameter("client_uri", clientUri);

        Object logoUri = responseMap.get("logo_uri");
        appInfo.addParameter("logo_uri", logoUri);

        Object applicationType = responseMap.get("application_type");
        appInfo.addParameter("application_type", applicationType);

        Object postLogoutRedirectUris = responseMap.get("post_logout_redirect_uris");
        appInfo.addParameter("post_logout_redirect_uris", postLogoutRedirectUris);

        Object responseTypes = responseMap.get("response_types");
        appInfo.addParameter("response_types", responseTypes);

        Object grantTypes = responseMap.get("grant_types");
        appInfo.addParameter("grant_types", String.join(" ", (JSONArray) grantTypes));

        Object tokenEndpointAuthMethod = responseMap.get("token_endpoint_auth_method");
        appInfo.addParameter("token_endpoint_auth_method", tokenEndpointAuthMethod);

        Object initiateLoginUri = responseMap.get("initiate_login_uri");
        appInfo.addParameter("initiate_login_uri", initiateLoginUri);

        appInfo.addParameter("additionalProperties", responseMap.toString());

        System.out.println(appInfo.getClientSecret());

        return appInfo;
    }

    private static void handleException(String msg) throws Exception {
        System.out.println(msg);
        throw new Exception(msg);
    }



}




