package commands.search.bing;

import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent;
import config.Config;
import org.json.JSONArray;
import org.json.JSONObject;
import commands.search.Search;
import commands.search.SearchResults;
import ts3bot.TS3Bot;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by David on 9/10/2016.
 */
public class BingSearch extends Search {

    private static final String urlPattern = "https://api.cognitive.microsoft.com/bing/v5.0/search?q=%s&format=JSON&responseFilter=webpages";

    public BingSearch(TextMessageEvent e) {
        super(e);
    }


    @Override
    public SearchResults search(int numResults, String query) {
        SearchResults results = new SearchResults();
                try {
                    final String searchUrl = String.format(urlPattern + "&count=" + numResults, URLEncoder.encode(query, CHARSET));

                    final URL           url        = new URL(searchUrl);
                    final URLConnection connection = url.openConnection();
                    connection.setRequestProperty("Ocp-Apim-Subscription-Key", TS3Bot.config.getBingApiKey());

                    InputStream response = connection.getInputStream();


                    try (Scanner scanner = new Scanner(response)) {

                String rawJson = scanner.useDelimiter("\\A").next();

                JSONObject json    = new JSONObject(rawJson).getJSONObject("webPages");
                JSONArray  jsonArr = json.getJSONArray("value");

                Pattern pattern = Pattern.compile("(?<=(r\\=))(.*?)(?=(\\&p))");

                for (int i = 0; i < jsonArr.length(); i++) {
                    String title       = jsonArr.getJSONObject(i).getString("name");
                    String link        = jsonArr.getJSONObject(i).getString("url");
                    String description = jsonArr.getJSONObject(i).getString("snippet");

                    Matcher matcher = pattern.matcher(link);

                    if (matcher.find()) {
                        link = URLDecoder.decode(matcher.group(), CHARSET);
                    }
                    results.add(new BingSearchResult(link, title, description));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        results.setHeader("Displaying " + results.size() +
                (results.size() == 1 ? " result " : " results ") +
                "for [b]" + query + "[/b].\n");

        return results.size() > 0 ? results : null;
    }

    @Override
    public SearchResults search(String query) {
        return search(3, query);
    }

}