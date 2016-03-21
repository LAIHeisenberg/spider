package demo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class Music163Test {
	public static void main(String[] args) {
		post();
	}

	public static void post() {
		// 创建默认的httpClient实例.
		CloseableHttpClient httpclient = HttpClients.createDefault();
		// 创建httppost
		HttpPost httppost = new HttpPost("http://music.163.com/weapi/cloudsearch/get/web?csrf_token=");
		// 创建参数队列
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams
				.add(new BasicNameValuePair(
						"encSecKey",
						"41050e0eeb381abc22487238253a0159b43e147c179053c55fa97912837e2a3e607d8b7768364e2634bb6310d1db8d09e06de47d8a3489ce1b3db061bd8e99d9193da60d186f063a4303eb050d7a52ab02918875beb9b944a8c2abd7b6da8a82c0952e4597c7f8e6e0834abfa0ef70a4c4023b6125af3c20198168355eb4bb59"));
		formparams
				.add(new BasicNameValuePair(
						"params",
						"mla+XKp64LFFCLY28HeKpDmA9xYqinCmweUK2ueQWM/2CwUylHcsmFHg4MBdNDBobYlFhVGVPGJ4IGEcWrES1biWhkReSR2t8CWDvtJEOOMkHlmBM6J/NjW+QI6i5iwlw7MGNiEIDPto8AW0G+OPRNr+6kCAw1KtTMETJxk6Gvob5GJDRnyjNiIcKu1BzOX941r3kRU2QFutrBt7rOkwMU81MKTyROBY2dweFv7gRq63DAPjWd8/dAQWnCAshfCeQQ48cHP4pLtg6LLyiwf+vuEKEkGUig24W3MTgAEObT2AyaB+A6r9oKNSruj4l9Kw"));
		UrlEncodedFormEntity uefEntity;
		try {
			uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
			httppost.setEntity(uefEntity);

			httppost.setHeader("Host", "music.163.com");
			httppost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0");
			httppost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			httppost.setHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
			httppost.setHeader("Accept-Encoding", "gzip, deflate");
			httppost.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			httppost.setHeader("Referer", "http://music.163.com/search/");
			httppost.setHeader(
					"Cookie",
					"JSESSIONID-WYYY=ada6f601cd576509e40663b17d8a218e71adc8b0f1bcb34f8a24e31aefed6abdcae520f82a9ad9022f16d9fa548d9f5a9c8ea65f8a69e46cbd3b0278cd20374f2ae4345cda2ada4b2b3eb369306ef133618700609964c908f4e5d5f50fb3c84385a04554fc89982603057a4369bbf860e1bb17fd75c3336288fec838ea776b2b8313d125%3A1458203938884; _iuqxldmzr_=25; __utma=94650624.1630450814.1458200401.1458200401.1458200401.1; __utmb=94650624.31.10.1458200401; __utmc=94650624; __utmz=94650624.1458200401.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); visited=true");
			httppost.setHeader("Connection", "keep-alive");
			httppost.setHeader("Pragma", "no-cache");
			httppost.setHeader("Cache-Control", "no-cache");

			System.out.println("executing request " + httppost.getURI());
			CloseableHttpResponse response = httpclient.execute(httppost);
			try {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					String jsonStr = EntityUtils.toString(entity, "UTF-8");
					System.out.println("--------------------------------------");
					System.out.println("Response content: " + jsonStr);
					System.out.println("--------------------------------------");
				}
			} finally {
				response.close();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 关闭连接,释放资源
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
