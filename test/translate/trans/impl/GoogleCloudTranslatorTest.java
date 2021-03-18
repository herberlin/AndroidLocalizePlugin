package translate.trans.impl;

import org.junit.Assert;
import org.junit.Test;
import translate.lang.LANG;

import static org.junit.Assert.*;

public class GoogleCloudTranslatorTest {

    String jsonPath = "C:/Users/Hans-Joachim.HERBERT/eclipse/key.boatspeed-translate.json";

    @Test
    public void setFormData() throws Exception {

        GoogleCloudTranslator testObj = new GoogleCloudTranslator(jsonPath);
        testObj.setFormData(LANG.Auto, LANG.German, "Sailboat");
        Assert.assertEquals("Segelboot",testObj.query());
    }
}