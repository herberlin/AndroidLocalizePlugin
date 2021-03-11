package translate.trans.impl;

import org.junit.Assert;
import org.junit.Test;
import translate.lang.LANG;

import static org.junit.Assert.*;

public class GoogleCloudTranslatorTest {

    @Test
    public void setFormData() throws Exception {

        GoogleCloudTranslator testObj = new GoogleCloudTranslator();
        testObj.setFormData(LANG.Auto, LANG.German, "Sailboat");
        Assert.assertEquals("Segelboot",testObj.query());
    }
}