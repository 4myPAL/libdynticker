package mobi.boilr.libdynticker.exchanges;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonNode;

import mobi.boilr.libdynticker.core.Exchange;
import mobi.boilr.libdynticker.core.Pair;

public final class BleuTradeExchange extends Exchange {

	public BleuTradeExchange(long expiredPeriod) {
		super("BleuTrade", expiredPeriod);
	}

	@Override
	protected List<Pair> getPairsFromAPI() throws IOException {
		List<Pair> pairs = new ArrayList<Pair>();
		JsonNode node = readJsonFromUrl("https://bleutrade.com/api/v2/public/getmarkets");
		if(node.get("success").asText().equals("true")) {
			boolean isActive;
			for(JsonNode market : node.get("result")) {
				isActive = market.get("IsActive").asText().equals("true");
				if(isActive)
					pairs.add(new Pair(market.get("MarketCurrency").asText(), market.get("BaseCurrency").asText()));
			}
		}
		else
			throw new IOException(node.get("message").asText());
		return pairs;
	}

	@Override
	protected String getTicker(Pair pair) throws IOException {
		JsonNode node = readJsonFromUrl("https://bleutrade.com/api/v2/public/getticker?market=" +
				pair.getExchange() + "_" + pair.getCoin());
		if(node.get("success").asText().equals("true"))
			return parseTicker(node, pair);
		else
			throw new IOException(node.get("message").asText());
	}

	@Override
	public String parseTicker(JsonNode node, Pair pair) throws IOException {
		return node.get("result").getElements().next().get("Last").asText();
	}
}
