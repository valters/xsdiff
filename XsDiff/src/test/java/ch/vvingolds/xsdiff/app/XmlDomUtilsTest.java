package ch.vvingolds.xsdiff.app;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class XmlDomUtilsTest {

    @Test
    public void shouldRemovePrefix() {
        assertThat( XmlDomUtils.removeNsPrefix( "xs:element" ), is( "element") );
    }

    @Test
    public void shouldHandleEdgeCases() {
        assertThat( XmlDomUtils.removeNsPrefix( "element" ), is( "element") );
        assertThat( XmlDomUtils.removeNsPrefix( ":element" ), is( "element") );
        assertThat( XmlDomUtils.removeNsPrefix( "xs:" ), is( "") );
        assertThat( XmlDomUtils.removeNsPrefix( ":element:" ), is( "element:") );
    }

}
