package translate.trans.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.PluginConfig;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import translate.lang.LANG;
import translate.trans.AbstractTranslator;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class GoogleTranslator extends AbstractTranslator {

    private static final String url = "https://translate.google.cn/translate_a/single";

    public GoogleTranslator() {
        super(url);
    }

    @Override
    public void setLangSupport() {
        LanguageHelper.initLanguages(langData);
    }
    @Override
    public void setFormData(LANG from, LANG to, String text) {
        formData.put("client", "t");
        formData.put("sl", from.getCode());
        formData.put("tl", to.getCode());
        formData.put("hl", "zh-CN");
        formData.put("dt", "t");
        formData.put("ie", "UTF-8");
        formData.put("oe", "UTF-8");
        formData.put("tk", token(text));
        formData.put("q", text);
    }

        @Override
    public String query() throws Exception {
        URIBuilder uri = new URIBuilder(url);
        for (String key : formData.keySet()) {
            String value = formData.get(key);
            uri.addParameter(key, value);
        }
        HttpGet request = new HttpGet(uri.toString());

        RequestConfig.Builder builder = RequestConfig.copy(RequestConfig.DEFAULT)
                .setSocketTimeout(5000)
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000);

        if (PluginConfig.isEnableProxy()) {
            HttpHost proxy = new HttpHost(PluginConfig.getHostName(), PluginConfig.getPortNumber());
            builder.setProxy(proxy);
        }

        RequestConfig config = builder.build();
        request.setConfig(config);
        CloseableHttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();

        String result = EntityUtils.toString(entity, "UTF-8");
        EntityUtils.consume(entity);
        response.getEntity().getContent().close();
        response.close();

        return result;
    }

    @Override
    public String parses(String text) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(text).get(0).get(0).get(0).textValue();
    }

    private String token(String text) {
        String tk = "";
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
        try (InputStream inputStream = getClass().getResourceAsStream("/tk/Google.js")) {
            engine.eval(new InputStreamReader(inputStream));

            if (engine instanceof Invocable) {
                Invocable invoke = (Invocable) engine;
                tk = String.valueOf(invoke.invokeFunction("token", text));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tk;
    }
}
