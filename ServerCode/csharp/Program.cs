using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Net;
using System.Xml;
using System.Xml.XPath;
using System.Threading;
using System.Net.Http;

namespace BusTime
{
    class Program
    {
        static string mbta_open_development_API_key = "wX9NwuHnZU2ToO7GmGR9uw";
        static int everySecond = 1000;
        static int everyHalfMinute = everySecond*30;
        static void Main(string[] args)
        {
            // TODO: get my own mbta API key rather than using the public key
            string mbta_API_key = mbta_open_development_API_key;
            // NOTE: I've replaced my stop id with 'XXXXX', you'll need to replace it with your own based on your bus stop id
            string requestURL = "http://realtime.mbta.com/developer/api/v2/predictionsbystop?api_key=" + mbta_API_key + "&stop=XXXXX&format=xml";
            while(true)
            {
                int millisecondsToSleep = everySecond;
                try
                {
                    HttpWebRequest request = WebRequest.Create(requestURL) as HttpWebRequest;
                    HttpWebResponse response = request.GetResponse() as HttpWebResponse;

                    XmlDocument xmlDoc = new XmlDocument();
                    xmlDoc.Load(response.GetResponseStream());
                    XmlNode trip = xmlDoc.SelectSingleNode("//trip");
                    string arrivingTimeText = trip.Attributes.GetNamedItem("pre_dt").InnerText;
                    DateTime prediction = new DateTime(1970,1,1,0,0,0, DateTimeKind.Utc).AddSeconds(Int32.Parse(arrivingTimeText));

                    TimeSpan timespan = prediction - DateTime.UtcNow;
                    int minutes = Convert.ToInt32(timespan.TotalMinutes);
                    int second = timespan.Seconds;

                    // NOTE: I've replaced my access token with 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX', you'll need to use your own here based on your photon chip

                    var updateClient = new HttpClient();
                    var updateRequestContent = new FormUrlEncodedContent(new[] {
                        new KeyValuePair<string, string>("access_token", "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"),
                        new KeyValuePair<string,string>("params",minutes.ToString() + ":" + second.ToString())
                    });

                    // NOTE: I've replaced my device id with 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXX', you'll need to use your own here based on your photon chip

                    HttpResponseMessage updateResponse = updateClient.PostAsync(
                            "https://api.particle.io/v1/devices/XXXXXXXXXXXXXXXXXXXXXXXXXXXXX/TimeToBus",
                            updateRequestContent).Result;

                    millisecondsToSleep = everyHalfMinute;
                }
                catch
                {

                }
                Thread.Sleep(millisecondsToSleep);
            }
        }
    }
}
