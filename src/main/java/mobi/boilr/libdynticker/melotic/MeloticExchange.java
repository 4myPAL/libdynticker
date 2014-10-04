package mobi.boilr.libdynticker.melotic;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

public class MeloticExchange extends Exchange {

	public MeloticExchange(long experiedPeriod) {
		super("Melotic", experiedPeriod);
//		// TODO Attempt to accept cookies. Not working yet.
//		CookieManager cm = new CookieManager();
//		cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
//		CookieHandler.setDefault(cm);
	}

	@Override
	protected List<Pair> getPairsFromAPI() throws JsonProcessingException, MalformedURLException,
	IOException {
		List<Pair> pairs = new ArrayList<Pair>();
		URL url = new URL("https://www.melotic.com/api/markets");
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setRequestProperty("Accept","*/*");
		urlConnection.connect();
		InputStream is = urlConnection.getInputStream();
		JsonNode elements = (new ObjectMapper()).readTree(is);
		Iterator<String> fieldNames = elements.getFieldNames();
		while(fieldNames.hasNext()) {
			String element = fieldNames.next();
			String[] split = element.split("-");
			String coin = split[0].toUpperCase();
			String exchange = split[1].toUpperCase();
			Pair pair = new Pair(coin, exchange);
			pairs.add(pair);
		}
		return pairs;
	}

	@Override
	protected String getTicker(Pair pair) throws JsonProcessingException, MalformedURLException,
			IOException {
		// http://www.melotic.com/api/markets/gold-btc/ticker
		String address = "https://www.melotic.com/api/markets/" + pair.getCoin().toLowerCase() + "-" +
				pair.getExchange().toLowerCase() + "/ticker";		
		URL url = new URL(address);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setRequestProperty("Accept","*/*");
		urlConnection.connect();
		InputStream is = urlConnection.getInputStream();
		JsonNode node = (new ObjectMapper()).readTree(is);
		if(node.has("message"))
			throw new MalformedURLException(node.get("message").getTextValue());
		return parseJSON(node, pair);
	}

	@Override
	public String parseJSON(JsonNode node, Pair pair) {
		return node.get("latest_price").toString();
	}
}