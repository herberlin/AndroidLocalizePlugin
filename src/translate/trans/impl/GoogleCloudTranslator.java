package translate.trans.impl;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.common.collect.Lists;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import translate.lang.LANG;
import translate.trans.AbstractTranslator;

import java.io.FileInputStream;
import java.io.IOException;

public class GoogleCloudTranslator extends AbstractTranslator {
    Translate translate = null;

    public GoogleCloudTranslator(String credentialFile) {
        super(null);

        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialFile))
                    .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
            translate = TranslateOptions.newBuilder().setCredentials(credentials).build().getService();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setLangSupport() {
        LanguageHelper.initLanguages(langData);
    }

    private Translation translation;

    @Override
    public void setFormData(LANG from, LANG to, String text) {

        translation = translate.translate(
                text,
                Translate.TranslateOption.targetLanguage(to.getCode()),
                Translate.TranslateOption.model("nmt"));

    }

    @Override
    public String query() throws Exception {
        if (translation != null) {
            return translation.getTranslatedText();
        }
        return null;
    }

    @Override
    public String parses(String text) throws IOException {
        return text;
    }

    @Override
    public void close() {

    }
}
