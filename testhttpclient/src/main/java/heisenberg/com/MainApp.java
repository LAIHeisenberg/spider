package heisenberg.com;


import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainApp {

    private static final String GET_SONG_URLS = "\"songurl\":\"(http://y\\.qq\\.com/#type=song&id=(\\d+))(\")";
    private static final String GET_ALBUM_IDS = "\"albummid\":\"(\\w+)\".*?\"songurl\":\"http://y\\.qq\\.com/#type=song&id=\\d+\".*?\"vid\"";


    public static void main(String... args) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://soso.music.qq.com/fcgi-bin/search_cp?aggr=0&catZhida=1&lossless=0&sem=1&w=倩女幽魂%20张国荣&n=30&t=0&p=1&searchid=60318421969924549&remoteplace=txt.yqqlist.song&g_tk=1497091545&loginUin=503319878&hostUin=0&format=jsonp&inCharset=GB2312&outCharset=gb2312&notice=0&platform=yqq&jsonpCallback=searchSong1458538748359&needNewCode=0");
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        UrlEncodedFormEntity uefEntity;
        try {
            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            String jsonStr = EntityUtils.toString(entity, "UTF-8");
            getSongURLs(jsonStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getSongURLs(String responseText) {
        List<String> urls = new ArrayList<String>();
        Pattern pattern = Pattern.compile(GET_SONG_URLS);

        Matcher matcher = pattern.matcher(responseText);

        /*while (matcher.find()) {
            System.out.println(matcher.group() + "         " + matcher.group(1) + "         " + matcher.group(2) + "         " + matcher.group(3));
        }*/

        pattern = Pattern.compile(GET_ALBUM_IDS);
        matcher = pattern.matcher(responseText);
        while (matcher.find()) {
            System.out.println(matcher.group(1));
        }

        return urls;
    }

}

