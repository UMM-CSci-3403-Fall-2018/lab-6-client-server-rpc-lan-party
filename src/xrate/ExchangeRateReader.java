package xrate;

import java.io.*;
import java.net.URL;
import java.util.Properties;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import jdk.internal.util.xml.impl.Input;

import java.io.*;
import java.net.URL;
import java.io.IOException;

/**
 * Provide access to basic currency exchange rate services.
 * 
 * @author Matt Munns and Yutaro
 */
public class ExchangeRateReader {

    private final String BASE_URL;

    private String accessKey;

    /**
     * Construct an exchange rate reader using the given base URL. All requests
     * will then be relative to that URL. If, for example, your source is Xavier
     * Finance, the base URL is http://api.finance.xaviermedia.com/api/ Rates
     * for specific days will be constructed from that URL by appending the
     * year, month, and day; the URL for 25 June 2010, for example, would be
     * http://api.finance.xaviermedia.com/api/2010/06/25.xml
     * 
     * @param baseURL
     *            the base URL for requests
     */
    public ExchangeRateReader(String baseURL) throws IOException {
        BASE_URL = baseURL;

        // Reads the access keys from `etc/access_keys.properties`
        readAccessKeys();
    }

    /**
     * This reads the `fixer_io` access key from `etc/access_keys.properties`
     * and assigns it to the field `accessKey`.
     *
     * @throws IOException if there is a problem reading the properties file
     */
    private void readAccessKeys() throws IOException {
        Properties properties = new Properties();
        FileInputStream in = null;
        try {
            // Don't change this filename unless you know what you're doing.
            // It's crucial that we don't commit the file that contains the
            // (private) access keys. This file is listed in `.gitignore` so
            // it's safe to put keys there as we won't accidentally commit them.
            in = new FileInputStream("etc/access_keys.properties");
        } catch (FileNotFoundException e) {
            /*
             * If this error gets generated, make sure that you have the desired
             * properties file in your project's `etc` directory. You may need
             * to rename the file ending in `.sample` by removing that suffix.
             */
            System.err.println("Couldn't open etc/access_keys.properties; have you renamed the sample file?");
            throw(e);
        }
        properties.load(in);
        // This assumes we're using Fixer.io and that the desired access key is
        // in the properties file in the key labelled `fixer_io`.
        accessKey = properties.getProperty("fixer_io");
    }

    /**
     * Get the exchange rate for the specified currency against the base
     * currency (the Euro) on the specified date.
     * 
     * @param currencyCode
     *            the currency code for the desired currency
     * @param year

     *            the year as a four digit integer
     * @param month
     *            the month as an integer (1=Jan, 12=Dec)
     * @param day
     *            the day of the month as an integer
     * @return the desired exchange rate
     * @throws IOException if there are problems reading from the server
     */
    public float getExchangeRate(String currencyCode, int year, int month, int day) throws IOException {
        String monthString = null;
        String dayString = null;

        // put a zero in front of single digit numbers
        if(month < 10) {
            monthString = "0" + month;
        } else { monthString = "" + month; }

        if(day < 10) {
            dayString = "0" + day;
        } else { dayString = "" + day; }

        String urlString = BASE_URL + year + "-" + monthString + "-" + dayString + "?access_key=" + accessKey;
        URL url = new URL(urlString);
        InputStream inputStream = url.openStream();
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream));

        JsonObject json = new JsonParser().parse(reader).getAsJsonObject();

        // get the list of rates, then get the specific currency from that list, then convert the value to a float
        float rate = json.getAsJsonObject("rates").get(currencyCode).getAsFloat();

        return rate;
    }

    /**
     * Get the exchange rate of the first specified currency against the second
     * on the specified date.
     * 
     * @param fromCurrency
     *            the currency code we're exchanging *from*
     * @param toCurrency
     *            the currency code we're exchanging *to*
     * @param year
     *            the year as a four digit integer
     * @param month
     *            the month as an integer (1=Jan, 12=Dec)
     * @param day
     *            the day of the month as an integer
     * @return the desired exchange rate
     * @throws IOException if there are problems reading from the server
     */
    public float getExchangeRate(
            String fromCurrency, String toCurrency,
            int year, int month, int day) throws IOException {

        // use the method we already have to get each rate
        float fromRate = getExchangeRate(fromCurrency, year, month, day);
        float toRate = getExchangeRate(toCurrency, year, month, day);

        return fromRate / toRate;
    }
}