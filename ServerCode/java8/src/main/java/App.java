import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.time.Period;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.*;
import org.apache.http.impl.client.*;

/**
 * Created by shanemathews on 9/5/16.
 */
public class App {
    private static String mbta_open_development_API_key = "wX9NwuHnZU2ToO7GmGR9uw";
    private static int everySecond = 1000;
    private static int everyHalfMinute = everySecond*30;
    public Long GetSecondsTillBusLeavesStop(String stopId) throws IOException {
        // TODO: get my own mbta API key rather than using the public key
        String mbta_API_key = mbta_open_development_API_key;
        String requestURL = "http://realtime.mbta.com/developer/api/v2/predictionsbystop?api_key=" + mbta_API_key + "&stop=" + stopId + "&format=json";

        URL url = new URL(requestURL);
        try(InputStream is = url.openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            JSONObject obj = new JSONObject(rd.readLine());
            final JSONObject mode = obj.getJSONArray("mode").getJSONObject(0);
            final JSONObject route = mode.getJSONArray("route").getJSONObject(0);
            final JSONObject direction = route.getJSONArray("direction").getJSONObject(0);
            final JSONObject trip = direction.getJSONArray("trip").getJSONObject(0);
            return trip.getLong("pre_away");
        }
    }

    public static void main(String[] args) {

        String stopId;
        String deviceId;
        String accessToken;
        {
            Properties prop = new Properties();
            FileInputStream input = null;
            try {
                input = new FileInputStream("config.properties");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            try {
                prop.load(input);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // set the properties value
            stopId = prop.getProperty("stopId");
            deviceId = prop.getProperty("deviceId");
            accessToken = prop.getProperty("access_token");
        }

        App newApp = new App();
        while(true) {

            long millisecondsToSleep = everyHalfMinute;
            try {
                Long totalSeconds = newApp.GetSecondsTillBusLeavesStop(stopId);

                Long minutes = totalSeconds / 60;
                Long seconds = totalSeconds - (minutes*60);

                try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {

                    // use httpClient (no need to close it explicitly)

                    HttpPost httpPost = new HttpPost("https://api.particle.io/v1/devices/" + deviceId + "/TimeToBus");
                    ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                    postParameters.add(new BasicNameValuePair("access_token", accessToken));
                    postParameters.add(new BasicNameValuePair("params", minutes.toString() + ":" + seconds.toString()));

                    httpPost.setEntity(new UrlEncodedFormEntity(postParameters));

                    HttpResponse response = httpClient.execute(httpPost);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }

            try {
                Thread.sleep(millisecondsToSleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
